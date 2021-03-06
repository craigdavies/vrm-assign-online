package email

import com.google.inject.Inject
import models.BusinessDetailsModel
import models.CaptureCertificateDetailsFormModel
import models.CaptureCertificateDetailsModel
import models.ConfirmFormModel
import models.FulfilModel
import models.VehicleAndKeeperLookupFormModel
import org.apache.commons.codec.binary.Base64
import pdf.PdfService
import play.api.Logger
import play.api.Play
import play.api.Play.current
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.Attachment
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From
import utils.helpers.Config
import views.html.vrm_assign.email_with_html
import views.html.vrm_assign.email_without_html
import webserviceclients.emailservice.EmailService
import webserviceclients.emailservice.EmailServiceSendRequest

final class AssignEmailServiceImpl @Inject()(emailService: EmailService,
                                             dateService: DateService,
                                             pdfService: PdfService,
                                             config: Config) extends AssignEmailService {

  private val from = From(email = config.emailSenderAddress, name = "DO NOT REPLY")
  private val govUkUrl = Some("public/images/gov-uk-email.jpg")

  def emailRequest(emailAddress: String,
                   vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                   captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                   captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                   vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                   transactionTimestamp: String,
                   transactionId: String,
                   confirmFormModel: Option[ConfirmFormModel],
                   businessDetailsModel: Option[BusinessDetailsModel],
                   isKeeper: Boolean,
                   trackingId: String): Option[EmailServiceSendRequest] = {
    val inputEmailAddressDomain = emailAddress.substring(emailAddress.indexOf("@"))

    if ((!config.emailWhitelist.isDefined) ||
      (config.emailWhitelist.get contains inputEmailAddressDomain.toLowerCase) &&
        inputEmailAddressDomain != "test.com") {

      val keeperName = Seq(vehicleAndKeeperDetailsModel.title,
        vehicleAndKeeperDetailsModel.firstName,
        vehicleAndKeeperDetailsModel.lastName
      ).flatten.mkString(" ")

      val pdf = pdfService.create(
        transactionId,
        keeperName,
        vehicleAndKeeperDetailsModel.address,
        vehicleAndKeeperLookupFormModel.replacementVRN.replace(" ", "")
      )
      val plainTextMessage = populateEmailWithoutHtml(
        vehicleAndKeeperDetailsModel,
        captureCertificateDetailsFormModel,
        captureCertificateDetailsModel,
        vehicleAndKeeperLookupFormModel,
        transactionTimestamp,
        transactionId,
        confirmFormModel,
        businessDetailsModel,
        isKeeper
      )
      val message = htmlMessage(
        vehicleAndKeeperDetailsModel,
        captureCertificateDetailsFormModel,
        captureCertificateDetailsModel,
        vehicleAndKeeperLookupFormModel,
        transactionTimestamp,
        transactionId,
        confirmFormModel,
        businessDetailsModel,
        isKeeper
      ).toString()
      //          var subject = captureCertificateDetailsFormModel.prVrm.replace(" ", "") +
      val subject = vehicleAndKeeperLookupFormModel.replacementVRN.replace(" ", "") +
        " " + Messages("email.email_service_impl.subject") +
        " " + vehicleAndKeeperDetailsModel.registrationNumber.replace(" ", "")

      val attachment: Option[Attachment] = {
        isKeeper match {
          case false => Some(
            new Attachment(
              Base64.encodeBase64URLSafeString(pdf),
              "application/pdf",
              "eV948.pdf",
              "Replacement registration number letter of authorisation"
            )
          )
          case true => None
        }
      } // US1589: Do not send keeper a pdf

      Some(new EmailServiceSendRequest(
        plainTextMessage,
        message,
        attachment,
        from,
        subject,
        Option(List(emailAddress)),
        None
      ))
    } else {
      Logger.error("Email not sent as not in whitelist")
      None
    }
  }

  override def sendEmail(emailAddress: String,
                         vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                         captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                         captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                         vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                         transactionTimestamp: String,
                         transactionId: String,
                         confirmFormModel: Option[ConfirmFormModel],
                         businessDetailsModel: Option[BusinessDetailsModel],
                         isKeeper: Boolean,
                         trackingId: String) {
    Future {
      emailRequest(
        emailAddress,
        vehicleAndKeeperDetailsModel,
        captureCertificateDetailsFormModel,
        captureCertificateDetailsModel,
        vehicleAndKeeperLookupFormModel,
        transactionTimestamp,
        transactionId,
        confirmFormModel,
        businessDetailsModel,
        isKeeper,
        trackingId
      ).map { emailServiceSendRequest =>
        Logger.info(s"About to send email to $emailAddress - trackingId $trackingId")
        if (emailServiceSendRequest.attachment.isDefined) {
          Logger.info("Sending with attachment")
        }

        emailService.invoke(emailServiceSendRequest, trackingId).map {
          response =>
            if (isKeeper) Logger.info(s"Keeper email sent - trackingId $trackingId")
            else Logger.info(s"Non-keeper email sent - trackingId $trackingId")
        }.recover {
          case NonFatal(e) =>
            Logger.error(s"Email Service web service call failed. Exception " + e.toString)
        }
      }
    }
  }

  override def htmlMessage(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                           captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                           captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                           vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                           transactionTimestamp: String,
                           transactionId: String,
                           confirmFormModel: Option[ConfirmFormModel],
                           businessDetailsModel: Option[BusinessDetailsModel],
                           isKeeper: Boolean): HtmlFormat.Appendable = {

    val govUkContentId = govUkUrl match {
      case Some(filename) =>
        Play.resource(name = filename) match {
          case Some(resource) =>
            val imageInFile = resource.openStream()
            val imageData = org.apache.commons.io.IOUtils.toByteArray(imageInFile)
            "data:image/jpeg;base64," + Base64.encodeBase64String(imageData)
          case _ => ""
        }
      case _ => ""
    }

    email_with_html(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber.trim,
      retentionCertId = captureCertificateDetailsFormModel.certificateDocumentCount + " " +
      captureCertificateDetailsFormModel.certificateDate + " " +
      captureCertificateDetailsFormModel.certificateTime + " " +
      captureCertificateDetailsFormModel.certificateRegistrationMark,
      transactionId = transactionId,
      transactionTimestamp = transactionTimestamp,
      keeperName = formatName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatAddress(vehicleAndKeeperDetailsModel),
      amount = (config.renewalFee.toDouble / 100.0).toString,
      replacementVRM = vehicleAndKeeperLookupFormModel.replacementVRN,
      outstandingFees = captureCertificateDetailsModel.outstandingFees / 100,
      outstandingDates = captureCertificateDetailsModel.outstandingDates,
      govUkContentId = govUkContentId,
      keeperEmail = if (confirmFormModel.isDefined) confirmFormModel.get.keeperEmail else None,
      businessDetailsModel = businessDetailsModel,
      businessAddress = formatAddress(businessDetailsModel),
      isKeeper
    )
  }

  private def populateEmailWithoutHtml(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel,
                                       captureCertificateDetailsFormModel: CaptureCertificateDetailsFormModel,
                                       captureCertificateDetailsModel: CaptureCertificateDetailsModel,
                                       vehicleAndKeeperLookupFormModel: VehicleAndKeeperLookupFormModel,
                                       transactionTimestamp: String,
                                       transactionId: String,
                                       confirmFormModel: Option[ConfirmFormModel],
                                       businessDetailsModel: Option[BusinessDetailsModel],
                                       isKeeper: Boolean): String = {
    email_without_html(
      vrm = vehicleAndKeeperDetailsModel.registrationNumber.trim,
      retentionCertId = captureCertificateDetailsFormModel.certificateDocumentCount + " " +
      captureCertificateDetailsFormModel.certificateDate + " " +
      captureCertificateDetailsFormModel.certificateTime + " " +
      captureCertificateDetailsFormModel.certificateRegistrationMark,
      transactionId = transactionId,
      transactionTimestamp = transactionTimestamp,
      keeperName = formatName(vehicleAndKeeperDetailsModel),
      keeperAddress = formatAddress(vehicleAndKeeperDetailsModel),
      amount = (config.renewalFee.toDouble / 100.0).toString,
//      replacementVRM = captureCertificateDetailsFormModel.prVrm,
      replacementVRM = vehicleAndKeeperLookupFormModel.replacementVRN,
      outstandingFees = captureCertificateDetailsModel.outstandingFees / 100,
      outstandingDates = captureCertificateDetailsModel.outstandingDates,
      keeperEmail = if (confirmFormModel.isDefined) confirmFormModel.get.keeperEmail else None,
      businessDetailsModel = businessDetailsModel,
      businessAddress = formatAddress(businessDetailsModel),
      isKeeper
    ).toString()
  }

  private def formatName(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    Seq(vehicleAndKeeperDetailsModel.title, vehicleAndKeeperDetailsModel.firstName, vehicleAndKeeperDetailsModel.lastName).
      flatten.
      mkString(" ")
  }

  private def formatAddress(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel): String = {
    vehicleAndKeeperDetailsModel.address match {
      case Some(adressModel) => adressModel.address.mkString(", ")
      case None => ""
    }
  }

  private def formatAddress(businessDetailsModel: Option[BusinessDetailsModel]): String = {
    businessDetailsModel match {
      case Some(details) => details.address.address.mkString(", ")
      case None => ""
    }
  }
}