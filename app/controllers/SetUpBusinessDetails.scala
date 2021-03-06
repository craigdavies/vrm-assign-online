package controllers

import com.google.inject.Inject
import models.BusinessDetailsModel
import models.CacheKeyPrefix
import models.FulfilModel
import models.SetupBusinessDetailsFormModel
import models.SetupBusinessDetailsViewModel
import play.api.data.Form
import play.api.data.FormError
import play.api.mvc.{Action, Controller, Request}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClearTextClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichCookies
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichForm
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.RichResult
import uk.gov.dvla.vehicles.presentation.common.model.{Address, AddressModel, VehicleAndKeeperDetailsModel}
import uk.gov.dvla.vehicles.presentation.common.views.helpers.FormExtensions.formBinding
import utils.helpers.Config
import views.vrm_assign.RelatedCacheKeys.removeCookiesOnExit
import views.vrm_assign.SetupBusinessDetails.{BusinessAddressId, BusinessContactId, BusinessEmailId, BusinessNameId}
import views.vrm_assign.VehicleLookup.TransactionIdCacheKey
import webserviceclients.audit2
import webserviceclients.audit2.AuditRequest
import views.vrm_assign.ConfirmBusiness.StoreBusinessDetailsCacheKey

final class SetUpBusinessDetails @Inject()(auditService2: audit2.AuditService)
                                          (implicit clientSideSessionFactory: ClientSideSessionFactory,
                                             config: Config,
                                             dateService: uk.gov.dvla.vehicles.presentation.common.services.DateService
                                          ) extends Controller {

  private[controllers] val form = Form(
    SetupBusinessDetailsFormModel.Form.Mapping
  )

  def present = Action { implicit request =>
    (request.cookies.getModel[VehicleAndKeeperDetailsModel],
      request.cookies.getModel[FulfilModel]) match {
      case (Some(vehicleAndKeeperDetails), None) =>
        val viewModel = SetupBusinessDetailsViewModel(vehicleAndKeeperDetails)
        Ok(views.html.vrm_assign.setup_business_details(form.fill(), viewModel))
      case _ => Redirect(routes.VehicleLookup.present())
    }
  }

  def submit = Action { implicit request =>
    form.bindFromRequest.fold(
      invalidForm => {
        request.cookies.getModel[VehicleAndKeeperDetailsModel] match {
          case Some(vehicleAndKeeperDetails) =>
            val setupBusinessDetailsViewModel = SetupBusinessDetailsViewModel(vehicleAndKeeperDetails)
            BadRequest(views.html.vrm_assign.setup_business_details(formWithReplacedErrors(invalidForm),
              setupBusinessDetailsViewModel))
          case _ =>
            Redirect(routes.VehicleLookup.present())
        }
      },
      validForm =>
        Redirect(routes.ConfirmBusiness.present()).withCookie(validForm)
          .withCookie(createBusinessDetailsModel(businessName = validForm.name,
            businessContact = validForm.contact,
            businessEmail = validForm.email,
            address = validForm.address)
          )
         .withCookie(StoreBusinessDetailsCacheKey, validForm.address.searchFields.remember.toString)
    )
  }

  def exit = Action {
    implicit request =>
      auditService2.send(AuditRequest.from(
        pageMovement = AuditRequest.CaptureActorToExit,
        transactionId = request.cookies.getString(TransactionIdCacheKey)
          .getOrElse(ClearTextClientSideSessionFactory.DefaultTrackingId),
        timestamp = dateService.dateTimeISOChronology,
        vehicleAndKeeperDetailsModel = request.cookies.getModel[VehicleAndKeeperDetailsModel]))
      Redirect(routes.LeaveFeedback.present()).discardingCookies(removeCookiesOnExit)
  }

  private def formWithReplacedErrors(form: Form[SetupBusinessDetailsFormModel])(implicit request: Request[_]) =
    (form /: List(
      (BusinessNameId, "error.validBusinessName"),
      (BusinessContactId, "error.validBusinessContact"),
      (BusinessEmailId, "error.validEmail"),
      (BusinessAddressId, "error.restricted.validPostcode"))) { (form, error) =>
      form.replaceError(error._1, FormError(
        key = error._1,
        message = error._2,
        args = Seq.empty
      ))
    }.distinctErrors

  private def createBusinessDetailsModel(businessName: String,
                                         businessContact: String,
                                         businessEmail: String,
                                         address: Address): BusinessDetailsModel = {
    BusinessDetailsModel(name = businessName,
      contact = businessContact,
      email = businessEmail,
      address = new AddressModel(address = convertAddressToSeq(address)).formatPostcode)
  }

  // TODO: consider putting this on the Address object
  private def convertAddressToSeq(address: Address): Seq[String] = {
    Seq(address.streetAddress1,
      address.streetAddress2.getOrElse(""),
      address.streetAddress3.getOrElse(""),
      address.postTown,
      address.postCode
    ).filter(_ != "")
  }
}
