package pages.vrm_assign

import helpers.webbrowser.Page
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory

object PaymentCallbackPage extends Page {

  def address = s"$applicationContext/payment/callback"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Payment processing"
}
