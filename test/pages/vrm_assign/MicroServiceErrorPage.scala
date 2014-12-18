package pages.vrm_assign

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import views.vrm_assign.MicroserviceError
import MicroserviceError.{ExitId, TryAgainId}
import pages.ApplicationContext.applicationContext
import org.openqa.selenium.WebDriver

object MicroServiceErrorPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/micro-service-error"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title = "We are sorry"

  def tryAgain(implicit driver: WebDriver): Element = find(id(TryAgainId)).get

  def exit(implicit driver: WebDriver): Element = find(id(ExitId)).get
}
