package controllers

import audit1.AuditMessage
import com.google.inject.Inject
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.CaptureCertificateDetailsViewModel
import models.VehicleAndKeeperDetailsModel
import models.VehicleAndKeeperLookupFormModel
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.joda.time.Period
import play.api.Logger
import play.api.data.FormError
import play.api.data.{Form => PlayForm}
import play.api.libs.json.Writes
import play.api.mvc.Result
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichForm
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CacheKey
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebEndUserDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.common.VssWebHeaderDto
import utils.helpers.Config
import views.vrm_assign.CaptureCertificateDetails._
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.VehicleLookup._
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import webserviceclients.vrmretentioneligibility.VrmAssignEligibilityRequest
import webserviceclients.vrmretentioneligibility.VrmAssignEligibilityService

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.control.NonFatal

final class CaptureCertificateDetails @Inject()(
                                                 val bruteForceService: BruteForcePreventionService,
                                                 eligibilityService: VrmAssignEligibilityService,
                                                 auditService1: audit1.AuditService,
                                                 auditService2: audit2.AuditService,
                                                 dateService: DateService
                                                 )
                                               (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                                config: Config) extends Controller {

  type Form = CaptureCertificateDetailsFormModel

  private[controllers] val form = PlayForm(
    CaptureCertificateDetailsFormModel.Form.Mapping
  )

  def present = Action {
    implicit request =>
      request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
        case Some(vehicleAndKeeperDetails) =>
          val viewModel = CaptureCertificateDetailsViewModel(vehicleAndKeeperDetails)
          Ok(views.html.vrm_assign.capture_certificate_details(form.fill(), viewModel))
        case _ => Redirect(routes.VehicleLookup.present())
      }
  }

  def submit = Action.async {
    implicit request =>
      form.bindFromRequest.fold(
        invalidForm => {
          request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
            case Some(vehicleAndKeeperDetails) =>
              val captureCertificateDetailsViewModel = CaptureCertificateDetailsViewModel(vehicleAndKeeperDetails)
              Future.successful {
                BadRequest(views.html.vrm_assign.capture_certificate_details(formWithReplacedErrors(invalidForm),
                  captureCertificateDetailsViewModel))
              }
            case _ =>
              Future.successful {
                Redirect(routes.MicroServiceError.present())
              } // TODO is this correct
          }
        },
        validForm => {
          bruteForceAndLookup(
            validForm.prVrm,
            validForm)
        }
      )
  }

  def bruteForceAndLookup(prVrm: String, form: Form)
                         (implicit request: Request[_], toJson: Writes[Form], cacheKey: CacheKey[Form]): Future[Result] =
    bruteForceService.isVrmLookupPermitted(prVrm).flatMap { bruteForcePreventionModel =>
      val resultFuture = if (bruteForcePreventionModel.permitted) {
        // TODO use a match
        val vehicleAndKeeperLookupFormModel = request.cookies.getModel[VehicleAndKeeperLookupFormModel].get
        val vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel].get
        val transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId)
        checkVrmEligibility(form, vehicleAndKeeperLookupFormModel, vehicleAndKeeperDetailsModel, transactionId)
      }
      else Future.successful {
        val anonRegistrationNumber = LogFormats.anonymize(prVrm)
        Logger.warn(s"BruteForceService locked out vrm: $anonRegistrationNumber")
        Redirect(routes.VrmLocked.present())
      }

      resultFuture.map { result =>
        result.withCookie(bruteForcePreventionModel)
      }
    } recover {
      case exception: Throwable =>
        Logger.error(
          s"Exception thrown by BruteForceService so for safety we won't let anyone through. " +
            s"Exception ${exception.getStackTraceString}"
        )
        Redirect(routes.MicroServiceError.present())
    } map { result =>
      result.withCookie(form)
    }

  def exit = Action {
    implicit request =>
      auditService1.send(AuditMessage.from(
        pageMovement = AuditMessage.CaptureCertificateDetailsToExit,
        transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel]))
      auditService2.send(AuditRequest.from(
        pageMovement = AuditMessage.CaptureCertificateDetailsToExit,
        transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel]))
      Redirect(routes.LeaveFeedback.present()).discardingCookies(removeCookiesOnExit)
  }

  private def formWithReplacedErrors(form: PlayForm[CaptureCertificateDetailsFormModel])(implicit request: Request[_]) = {
    val certificateTimeWithSummary = FormError(
      key = CertificateTimeId,
      messages = Seq("error.validCertificateTime", "last error message"),
      args = Seq.empty
    )

    val replacedErrors = (form /: List(
      (CertificateDocumentCountId, "error.validCertificateDocumentCount"),
      (CertificateDateId, "error.validCertificateDate"),
      //      (CertificateTimeId, "error.validCertificateTime"),
      (CertificateRegistrationMarkId, "error.validCertificateRegistrationMark"),
      (PrVrmId, "error.validPrVrm"))) {
      (form, error) =>
        form.replaceError(error._1, FormError(
          key = error._1,
          message = error._2,
          args = Seq.empty
        ))
    }.
      replaceError(
        CertificateTimeId,
        certificateTimeWithSummary
      ).
      distinctErrors

    replacedErrors
  }

  /**
   * Call the eligibility service to determine if the VRM is valid for assignment
   */
  private def checkVrmEligibility(captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                                  vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                  vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                                  transactionId: String)
                                 (implicit request: Request[_]): Future[Result] = {

    def microServiceErrorResult(message: String) = {
      Logger.error(message)
      auditService1.send(AuditMessage.from(
        pageMovement = AuditMessage.CaptureCertificateDetailsToMicroServiceError,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology
      ))
      auditService2.send(AuditRequest.from(
        pageMovement = AuditMessage.CaptureCertificateDetailsToMicroServiceError,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology
      ))
      Redirect(routes.MicroServiceError.present())
    }

    def eligibilitySuccess(certificateExpiryDate: DateTime) = {
      val redirectLocation = {
        auditService1.send(AuditMessage.from(
          pageMovement = AuditMessage.CaptureCertificateDetailsToConfirm,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel)))
        auditService2.send(AuditRequest.from(
          pageMovement = AuditMessage.CaptureCertificateDetailsToConfirm,
          transactionId = transactionId,
          timestamp = dateService.dateTimeISOChronology,
          vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel)))
        routes.Confirm.present()
      }

      // calculate number of years owed if any
      val outstandingDates = calculateYearsOwed(certificateExpiryDate)

      bruteForceService.reset(captureCertificateDetailsFormModel.prVrm).onComplete {
        case scala.util.Success(httpCode) => Logger.debug(s"Brute force reset was called - it returned httpCode: $httpCode")
        case Failure(t) => Logger.error(s"Brute force reset failed: ${t.getStackTraceString}")
      }

      Redirect(redirectLocation)
        .withCookie(CaptureCertificateDetailsModel.from(captureCertificateDetailsFormModel.prVrm, Some(certificateExpiryDate), outstandingDates.toList, (outstandingDates.size * config.renewalFee.toInt)))
        .withCookie(captureCertificateDetailsFormModel)
    }

    def eligibilityFailure(responseCode: String, certificateExpiryDate: Option[DateTime]) = {
      Logger.debug(s"VrmAssignEligibility encountered a problem with request" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.referenceNumber)}" +
        s" ${LogFormats.anonymize(vehicleAndKeeperLookupFormModel.registrationNumber)}, redirect to VehicleLookupFailure")

      auditService1.send(AuditMessage.from(
        pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
        rejectionCode = Some(responseCode)))
      auditService2.send(AuditRequest.from(
        pageMovement = AuditMessage.VehicleLookupToVehicleLookupFailure,
        transactionId = transactionId,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = Some(vehicleAndKeeperDetailsModel),
        rejectionCode = Some(responseCode)))

      // calculate number of years owed if any
      // may not have an expiry date so check before calling function
      val outstandingDates: ListBuffer[String] = {
        certificateExpiryDate match {
          case Some(expiryDate) if (responseCode contains ("vrm_assign_eligibility_direct_to_paper")) => calculateYearsOwed(expiryDate)
          case _ => new ListBuffer[String]
        }
      }

      Redirect(routes.VehicleLookupFailure.present()).
        withCookie(key = VehicleAndKeeperLookupResponseCodeCacheKey, value = responseCode.split(" - ")(1)).
        withCookie(CaptureCertificateDetailsModel.from(captureCertificateDetailsFormModel.prVrm, certificateExpiryDate, outstandingDates.toList, (outstandingDates.size * config.renewalFee.toInt)))
    }

    def calculateYearsOwed(certificateExpiryDate: DateTime): ListBuffer[String] = {
      // calculate number of years owed
      var outstandingDates = new ListBuffer[String]
      var yearsOwedCount = 0
      var nextRenewalDate = certificateExpiryDate.plus(Period.years(1))
      val fmt = DateTimeFormat.forPattern("dd/MM/YYYY")
      val abolitionDate = fmt.parseDateTime(config.renewalFeeAbolitionDate)
      while (nextRenewalDate.isBefore(abolitionDate)) {
        yearsOwedCount += 1
        outstandingDates += (fmt.print(nextRenewalDate.minus(Period.years(1)).plus(Period.days(1))) + "  -  "
          + fmt.print(nextRenewalDate) + "   £" + (config.renewalFee.toInt / 100.0) + "0")
        nextRenewalDate = nextRenewalDate.plus(Period.years(1))
      }
      outstandingDates
    }

    val trackingId = request.cookies.trackingId()

    val eligibilityRequest = VrmAssignEligibilityRequest(
      buildWebHeader(trackingId),
      currentVehicleRegistrationMark = vehicleAndKeeperLookupFormModel.registrationNumber,
      certificateDate = captureCertificateDetailsFormModel.certificateDate,
      certificateTime = captureCertificateDetailsFormModel.certificateTime,
      certificateDocumentCount = captureCertificateDetailsFormModel.certificateDocumentCount,
      certificateRegistrationMark = captureCertificateDetailsFormModel.certificateRegistrationMark,
      replacementVehicleRegistrationMark = captureCertificateDetailsFormModel.prVrm,
      v5DocumentReference = vehicleAndKeeperLookupFormModel.referenceNumber,
      transactionTimestamp = dateService.now.toDateTime
    )

    eligibilityService.invoke(eligibilityRequest, trackingId).map {
      response =>
        response.responseCode match {
          case Some(responseCode) =>
            eligibilityFailure(responseCode, response.certificateExpiryDate)
          case None =>
            // Happy path when there is no response code therefore no problem.
            response.certificateExpiryDate match {
              case Some(certificateExpiryDate) => eligibilitySuccess(certificateExpiryDate)
              case _ => microServiceErrorResult(message = "No lastDate found")
            }
        }
    }.recover {
      case NonFatal(e) =>
        microServiceErrorResult(s"Vrm Assign Eligibility web service call failed. Exception " + e.toString)
    }
  }

  private def buildWebHeader(trackingId: String): VssWebHeaderDto = {
    VssWebHeaderDto(transactionId = trackingId,
      originDateTime = new DateTime,
      applicationCode = config.applicationCode,
      serviceTypeCode = config.vssServiceTypeCode,
      buildEndUser())
  }

  private def buildEndUser(): VssWebEndUserDto = {
    VssWebEndUserDto(endUserId = config.orgBusinessUnit, orgBusUnit = config.orgBusinessUnit)
  }
}