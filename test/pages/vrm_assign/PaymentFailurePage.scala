package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser._
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.PaymentFailure._

object PaymentFailurePage extends Page {

  def address = s"$applicationContext/payment-failure"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Payment Failure"

  def tryAgain(implicit driver: WebDriver) = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
