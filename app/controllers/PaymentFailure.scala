package controllers

import com.google.inject.Inject
import models.{VehicleAndKeeperDetailsModel, VehicleAndKeeperLookupFormModel, VehicleLookupFailureViewModel}
import play.api.mvc._
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieImplicits.{RichCookies, RichResult}
import utils.helpers.Config
import views.vrm_assign.VehicleLookup._

final class PaymentFailure @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                       config: Config) extends Controller {

  def present = Action { implicit request =>
    (request.cookies.getString(TransactionIdCacheKey),
      request.cookies.getModel[VehicleAndKeeperLookupFormModel]) match {
      case (Some(transactionId), Some(vehicleAndKeeperLookupForm)) =>
        val vehicleAndKeeperDetails = request.cookies.getModel[VehicleAndKeeperDetailsModel]
        displayPaymentFailure(transactionId, vehicleAndKeeperLookupForm, vehicleAndKeeperDetails)
      case _ => Redirect(routes.BeforeYouStart.present())
    }
  }

  def submit = Action { implicit request =>
    request.cookies.getModel[VehicleAndKeeperLookupFormModel] match {
      case (Some(vehicleAndKeeperLookupFormModel)) =>
        Redirect(routes.VehicleLookup.present())
//          .discardingCookie(RetainCacheKey) TODO
      case _ =>
        Redirect(routes.BeforeYouStart.present())
    }
  }

  private def displayPaymentFailure(transactionId: String,
                                    vehicleAndKeeperLookupForm: VehicleAndKeeperLookupFormModel,
                                    vehicleAndKeeperDetails: Option[VehicleAndKeeperDetailsModel]
                                     )(implicit request: Request[AnyContent]) = {
    val viewModel = vehicleAndKeeperDetails match {
      case Some(details) => VehicleLookupFailureViewModel(details)
      case None => VehicleLookupFailureViewModel(vehicleAndKeeperLookupForm)
    }

    Ok(views.html.vrm_assign.payment_failure(
      transactionId = transactionId,
      vehicleLookupFailureViewModel = viewModel,
      data = vehicleAndKeeperLookupForm)
    )
  }
}