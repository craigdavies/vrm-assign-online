package controllers

import audit._
import com.google.inject.Inject
import composition.RefererFromHeader
import models._
import org.apache.commons.codec.binary.Base64
import play.api.Logger
import play.api.mvc.{Action, Controller, Request, Result}
import uk.gov.dvla.vehicles.presentation.common.LogFormats
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import utils.helpers.Config
import webserviceclients.paymentsolve.{PaymentSolveCancelRequest, PaymentSolveGetRequest, PaymentSolveBeginRequest, PaymentSolveService}
import views.vrm_assign.Confirm._
import views.vrm_assign.Payment._
import views.vrm_assign.VehicleLookup._
import views.vrm_assign.RelatedCacheKeys._
import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext.Implicits.global

final class Payment @Inject()(paymentSolveService: PaymentSolveService,
                              dateService: DateService,
                              refererFromHeader: RefererFromHeader,
                              auditService: AuditService)
                             (implicit clientSideSessionFactory: ClientSideSessionFactory,
                              config: Config) extends Controller {

  def begin = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {
//      request.cookies.getModel[CaptureCertificateDetailsModel]) match { // TODO check for fulfill
//      case (_, _, Some(retainModel)) =>
//        Future.successful {
//          Redirect(routes.PaymentPreventBack.present())
//        }
      case (None, _) =>
        Future.successful {
          paymentFailure("missing TransactionIdCacheKey cookie")
        }
      case (_, None) =>
        Future.successful {
          paymentFailure("missing VehicleAndKeeperLookupFormModel cookie")
        }
      case (Some(transactionId), Some(vehiclesLookupForm)) =>
        println("********************  BEGIN WEB PAYMENT  ********************")
       callBeginWebPaymentService(transactionId, vehiclesLookupForm.registrationNumber)
      case _ => {
        Future.successful {
          paymentFailure("Payment failed matching cookies")
        }

      }
    }
  }

  // The token is checked in the common project, we do nothing with it here.
  def callback(token: String) = Action { implicit request =>
    Ok(views.html.vrm_assign.payment_callback_interstitial())
  }

  def getWebPayment = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
      case (Some(transactionId), Some(paymentDetails)) =>
        callGetWebPaymentService(transactionId, paymentDetails.trxRef.get)
      case _ => Future.successful {
        paymentFailure("Payment getWebPayment missing TransactionIdCacheKey or PaymentTransactionReferenceCacheKey cookie")
      }
    }
  }

  def cancel = Action.async { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey), request.cookies.getModel[PaymentModel]) match {
      case (Some(transactionId), Some(paymentDetails)) =>
        callCancelWebPaymentService(transactionId, paymentDetails.trxRef.get)
      case _ => Future.successful {
        paymentFailure("Payment cancel missing TransactionIdCacheKey or PaymentTransactionReferenceCacheKey cookie")
      }
    }
  }

  private def paymentFailure(message: String)(implicit request: Request[_]) = {
    Logger.error(message)

    auditService.send(AuditMessage.from(
      pageMovement = AuditMessage.PaymentToPaymentFailure,
      transactionId = request.cookies.getString(TransactionIdCacheKey).getOrElse(""),
      timestamp = dateService.dateTimeISOChronology,
      vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
      keeperEmail = request.cookies.getString(KeeperEmailCacheKey),
      businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
      paymentModel = request.cookies.getModel[PaymentModel],
      rejectionCode = Some(message)))

    Redirect(routes.PaymentFailure.present())
  }

  private def callBeginWebPaymentService(transactionId: String, vrm: String)
                                        (implicit request: Request[_],
                                         token: uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.CsrfPreventionToken): Future[Result] = {
    refererFromHeader.fetch match {
      case Some(referrer) =>
        val tokenBase64URLSafe = Base64.encodeBase64URLSafeString(token.value.getBytes)
        val paymentCallback = referrer.split(routes.Confirm.present().url)(0) + routes.Payment.callback(tokenBase64URLSafe).url
        val transNo = request.cookies.getString(PaymentTransNoCacheKey).get
        val outstandingFees = request.cookies.getModel[CaptureCertificateDetailsModel].get.outstandingFees
        val paymentSolveBeginRequest = PaymentSolveBeginRequest(
          transactionId = transactionId,
          transNo = transNo,
          vrm = vrm,
          purchaseAmount = outstandingFees,
          paymentCallback = paymentCallback
        )
        val trackingId = request.cookies.trackingId()

        paymentSolveService.invoke(paymentSolveBeginRequest, trackingId).map { response =>
          if (response.status == Payment.CardDetailsStatus) {
            Ok(views.html.vrm_assign.payment(paymentRedirectUrl = response.redirectUrl.get))
              //              Redirect(response.redirectUrl.get)
              .withCookie(PaymentModel.from(response.trxRef.get))
              .withCookie(REFERER, routes.Payment.begin().url) // The POST from payment service will not contain a REFERER in the header, so use a cookie.
          } else {
            paymentFailure(s"The begin web request to Solve was not validated. Payment Solve encountered a problem with request ${LogFormats.anonymize(vrm)}, redirect to PaymentFailure")
          }
        }.recover {
          case NonFatal(e) =>
            paymentFailure(message = s"Payment Solve web service call with paymentSolveBeginRequest failed. Exception " + e.toString)
        }
      case _ => Future.successful(paymentFailure(message = "Payment callBeginWebPaymentService no referer"))
    }
  }

  private def callGetWebPaymentService(transactionId: String, trxRef: String)
                                      (implicit request: Request[_]): Future[Result] = {

    def paymentNotAuthorised = {
      Logger.debug(s"Payment not authorised for ${LogFormats.anonymize(trxRef)}, redirect to PaymentNotAuthorised")

      var paymentModel = request.cookies.getModel[PaymentModel].get

      auditService.send(AuditMessage.from(
        pageMovement = AuditMessage.PaymentToPaymentNotAuthorised,
        transactionId = request.cookies.getString(TransactionIdCacheKey).get,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        keeperEmail = request.cookies.getString(KeeperEmailCacheKey),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel],
        paymentModel = Some(paymentModel)))

      Redirect(routes.PaymentNotAuthorised.present())
        .withCookie(paymentModel)
    }

    val transNo = request.cookies.getString(PaymentTransNoCacheKey).get

    val paymentSolveGetRequest = PaymentSolveGetRequest(
      transNo = transNo,
      trxRef = trxRef
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveGetRequest, trackingId).map { response =>
      if (response.status == Payment.AuthorisedStatus) {

        var paymentModel = request.cookies.getModel[PaymentModel].get

        paymentModel.authCode = response.authcode
        paymentModel.maskedPAN = response.maskedPAN
        paymentModel.cardType = response.cardType
        paymentModel.merchantId = response.merchantTransactionId
        paymentModel.paymentType = response.paymentType
        paymentModel.totalAmountPaid = response.purchaseAmount
        paymentModel.paymentStatus = Some(Payment.AuthorisedStatus)

        Redirect(routes.Fulfil.fulfil())
          .discardingCookie(REFERER) // Not used again.
          .withCookie(paymentModel)
      } else {
        Logger.debug("The payment was not authorised.")
        paymentNotAuthorised
      }
    }.recover {
      case NonFatal(e) =>
        paymentFailure(message = "Payment Solve web service call with paymentSolveGetRequest failed: " + e.toString)
    }
  }

  private def callCancelWebPaymentService(transactionId: String, trxRef: String)
                                         (implicit request: Request[_]): Future[Result] = {

    val transNo = request.cookies.getString(PaymentTransNoCacheKey).get

    val paymentSolveCancelRequest = PaymentSolveCancelRequest(
      transNo = transNo,
      trxRef = trxRef
    )
    val trackingId = request.cookies.trackingId()

    paymentSolveService.invoke(paymentSolveCancelRequest, trackingId).map { response =>
      if (response.response == Payment.CancelledStatus) {
        Logger.error("The get web request to Solve was not validated.")
      }

      auditService.send(AuditMessage.from(
        pageMovement = AuditMessage.PaymentToExit,
        transactionId = request.cookies.getString(TransactionIdCacheKey).get,
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel],
        keeperEmail = request.cookies.getString(KeeperEmailCacheKey),
        businessDetailsModel = request.cookies.getModel[BusinessDetailsModel]))

      redirectToLeaveFeedback
    }.recover {
      case NonFatal(e) =>
        Logger.error(s"Payment Solve web service call with paymentSolveCancelRequest failed. Exception " + e.toString)
        redirectToLeaveFeedback
    }
  }

  private def redirectToLeaveFeedback(implicit request: Request[_]) = {
    Redirect(routes.LeaveFeedback.present()).
      discardingCookies(removeCookiesOnExit)
  }
}

object Payment {

  final val CardDetailsStatus = "CARD_DETAILS"
  final val AuthorisedStatus = "AUTHORISED"
  final val CancelledStatus = "CANCELLED"
  final val SettledStatus = "SETTLED"
}