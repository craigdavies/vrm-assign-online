package controllers

import com.google.inject.Inject
import play.api.mvc.{Action, Controller}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.ClientSideSessionFactory
import utils.helpers.Config

final class PaymentPreventBack @Inject()()(implicit clientSideSessionFactory: ClientSideSessionFactory,
                                           config: Config) extends Controller {

  def present = Action { implicit request =>
    Ok(views.html.vrm_assign.payment_prevent_back())
  }

  def returnToSuccess = Action { implicit request =>
//    Redirect(routes.SuccessPayment.present()) // TODO
    Redirect(routes.LeaveFeedback.present())
  }
}
