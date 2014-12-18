package pages.vrm_assign

import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.{Element, Page, WebBrowserDSL, WebDriverFactory}
import pages.ApplicationContext.applicationContext
import views.vrm_assign.BeforeYouStart
import BeforeYouStart.NextId
import org.openqa.selenium.WebDriver

object BeforeYouStartPage extends Page with WebBrowserDSL {

  def address = s"$applicationContext/before-you-start"
  def url = WebDriverFactory.testUrl + address.substring(1)
  final override val title: String = "Put a registration number on a vehicle"
  final val titleCy: String = "Cael gwared cerbyd i mewn i'r fasnach foduron"

  def startNow(implicit driver: WebDriver): Element = find(id(NextId)).get
}
