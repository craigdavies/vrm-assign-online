package pages

import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.selenium.WebBrowser._
import org.scalatest.selenium.WebBrowser.click
import pages.vrm_assign.BusinessChooseYourAddressPage
import pages.vrm_assign.BusinessChooseYourAddressPage._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

final class BusinessChooseYourAddress_PageSteps(implicit webDriver: WebBrowserDriver, timeout: PatienceConfig) extends ScalaDsl with EN with Matchers {

  def `choose address from the drop-down` = {
    `is displayed`
    BusinessChooseYourAddressPage.chooseAddress.value = "0"
    click on BusinessChooseYourAddressPage.select
    this
  }

  def `click manual address entry` = {
    `is displayed`
    click on BusinessChooseYourAddressPage.manualAddress
    this
  }

  def `is displayed` = {
    eventually {
      currentUrl should equal(url)
    }
    this
  }

  def `form is filled with the values I previously entered` = {
    BusinessChooseYourAddressPage.chooseAddress.value should equal("0")
    this
  }

  def `form is not filled` = {
    BusinessChooseYourAddressPage.chooseAddress.value should equal("")
    this
  }
}
