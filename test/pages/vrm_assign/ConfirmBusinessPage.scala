package pages.vrm_assign

import helpers.webbrowser.Page
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{find, id}
import pages.ApplicationContext.applicationContext
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory
import views.vrm_assign.ConfirmBusiness.ConfirmId
import views.vrm_assign.ConfirmBusiness.ExitId

object ConfirmBusinessPage extends Page {

  def address = s"$applicationContext/confirm-business"

  override lazy val url = WebDriverFactory.testUrl + address.substring(1)

  final override val title: String = "Confirm your business details"

  def confirm(implicit driver: WebDriver) = find(id(ConfirmId)).get

  def exit(implicit driver: WebDriver) = find(id(ExitId)).get
}
