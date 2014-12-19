package pages

import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.selenium.WebBrowser._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import pages.vrm_assign.VehicleLookupPage._

class VehicleLookup_PageSteps(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  def `is displayed` = {
    currentUrl should equal(url)
    this
  }

  def enter(registrationNumber: String, docRefNumber: String, postcode: String) = {
    vehicleRegistrationNumber.value = registrationNumber
    documentReferenceNumber.value = docRefNumber
    keeperPostcode.value = postcode
    this
  }

  def `keeper is acting` = {
    click on currentKeeperYes
    this
  }

  def `keeper is not acting` = {
    click on currentKeeperNo
    this
  }

  def `find vehicle` = {
    click on findVehicleDetails
    this
  }

  def `has error messages` = {
    pageSource contains "Vehicle registration number - Must be valid format"
    pageSource contains "Document reference number - Document reference number must be an 11-digit number"
    this
  }


}