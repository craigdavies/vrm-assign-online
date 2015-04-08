package personalizedAssignment.stepDefs

import cucumber.api.java.After
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import cucumber.api.scala.EN
import cucumber.api.scala.ScalaDsl
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.PatienceConfig
import org.scalatest.selenium.WebBrowser._
import pages._
import pages.vrm_assign._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver
import views.vrm_assign.Fulfil.FulfilCacheKey
import views.vrm_assign.Payment.PaymentDetailsCacheKey
import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupFormModelCacheKey
import views.vrm_assign.VehicleLookup.VehicleAndKeeperLookupResponseCodeCacheKey

import scala.concurrent.duration.DurationInt

final class NavigationStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  private val timeout = PatienceConfig(timeout = 5.seconds)
  private val beforeYouStart = new BeforeYouStartPageSteps()(webDriver, timeout)
  private val vehicleLookup = new VehicleLookupPageSteps()(webDriver, timeout)
  private val captureCertificateDetails = new CaptureCertificateDetailsPageSteps()(webDriver, timeout)
  private val confirm = new ConfirmPageSteps()(webDriver, timeout)
  private val payment = new PaymentPageSteps()(webDriver, timeout)
  private val paymentPreventBack = new PaymentPreventBackPageSteps()(webDriver, timeout)
  private val setupBusinessDetails = new SetupBusinessDetailsPageSteps()(webDriver, timeout)
  private val businessChooseYourAddress = new BusinessChooseYourAddressPageSteps()(webDriver, timeout)
  private val confirmBusiness = new ConfirmBusinessPageSteps()(webDriver, timeout)
  private val success = new SuccessPageSteps()(webDriver, timeout)
  private val enterAddressManually = new EnterAddressManuallyPageSteps()(webDriver, timeout)

  @Given( """^that I am on the "(.*?)" page$""")
  def `that I am on the <origin> page`(origin: String) {
    origin match {
      case "vehicle-lookup" =>
        // Starting the service takes you to this page
        vehicleLookup.`is displayed`
      case "setup-business-details" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`is displayed`
      case "business-choose-your-address" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
      case "enter-address-manually" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`is displayed`
      case "confirm-business" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`is displayed`
      case "confirm-business (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`is displayed`
      case "capture-certificate-details (keeper acting)" =>
        vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`is displayed`
      case "capture-certificate-details (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`happy path`
        captureCertificateDetails.`is displayed`
      case "capture-certificate-details (business acting) (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`happy path`
        captureCertificateDetails.`is displayed`
      case "confirm" =>
        vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`happy path`
        confirm.`is displayed`
      case "confirm (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`happy path`
        captureCertificateDetails.`happy path`
        confirm.`is displayed`
      case "confirm (business acting) (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`happy path`
        captureCertificateDetails.`happy path`
        confirm.`is displayed`
      case "payment (keeper acting)" =>
        vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
        payment.`is displayed`
      case "payment (business acting)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`choose address from the drop-down`
        confirmBusiness.`happy path`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
        payment.`is displayed`
      case "payment (business acting) (entered address manually)" =>
        vehicleLookup.`happy path for business`
        setupBusinessDetails.`happy path`
        businessChooseYourAddress.`click manual address entry`
        enterAddressManually.`happy path`
        confirmBusiness.`happy path`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
        payment.`is displayed`
      case "success" => vehicleLookup.`happy path for keeper`
        captureCertificateDetails.`happy path`
        confirm.`happy path`
        payment.`happy path`
        success.`is displayed`
      case e => throw new RuntimeException(s"unknown 'origin' value: $e")
    }
  }

  @When( """^I enter the url for the "(.*?)" page$""")
  def `I enter the url for the <target> page`(target: String) {
    target match {
      case "before-you-start" => go to BeforeYouStartPage
      case "vehicle-lookup" => go to VehicleLookupPage
      case "setup-business-details" => go to SetupBusinessDetailsPage
      case "business-choose-your-address" => go to BusinessChooseYourAddressPage
      case "enter-address-manually" => go to EnterAddressManuallyPage
      case "confirm-business" => go to ConfirmBusinessPage
      case "capture-certificate-details" => go to CaptureCertificateDetailsPage
      case "confirm" => go to ConfirmPage
      case "payment" => go to PaymentPage
      case "success" => go to SuccessPage
      case e => throw new RuntimeException(s"unknown 'target' value: $e")
    }
  }

  @When( """^I press the browser's back button$""")
  def `I press the browser's back button`() {
    goBack()
  }

  @Then( """^I am redirected to the "(.*?)" page$""")
  def `I am taken to the <expected> page`(expected: String) {
    expected match {
      case "before-you-start" => beforeYouStart.`is displayed`
      case "vehicle-lookup" => vehicleLookup.`is displayed`
      case "setup-business-details" => setupBusinessDetails.`is displayed`
      case "business-choose-your-address" => businessChooseYourAddress.`is displayed`
      case "enter-address-manually" => enterAddressManually.`is displayed`
      case "capture-certificate-details" => captureCertificateDetails.`is displayed`
      case "confirm-business" => confirmBusiness.`is displayed`
      case "confirm" => confirm.`is displayed`
      case "payment" => payment.`is displayed`
      case "payment-prevent-back" => paymentPreventBack.`is displayed`
      case "success" => success.`is displayed`
      case e => throw new RuntimeException(s"unknown 'expected' value: $e")
    }
  }

  @Then("^the \"(.*?)\" form is \"(.*?)\" with the values I previously entered$")
  def `the <expected> form is <filled> with the values I previously entered`(expected: String, filled: String) {
    filled match {
      case "filled" => `the <expected> form is filled with the values I previously entered`(expected)
      case "not filled" => `the <expected> form is not filled with the values I previously entered`(expected)
      case e => throw new RuntimeException(s"unknown 'filled' value")
    }
  }

  @Then( """^the "(.*?)" form is filled with the values I previously entered$""")
  def `the <expected> form is filled with the values I previously entered`(expected: String) {
    expected match {
      case "before-you-start" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "vehicle-lookup" => vehicleLookup.`form is filled with the values I previously entered`()
      case "setup-business-details" => setupBusinessDetails.`form is filled with the values I previously entered`
      case "business-choose-your-address" => businessChooseYourAddress.`form is filled with the values I previously entered`
      case "enter-address-manually" => enterAddressManually.`form is filled with the values I previously entered`
      case "confirm-business" => confirmBusiness.`form is filled with the values I previously entered`()
      case "capture-certificate-details (business acting)" => captureCertificateDetails.`form is filled with the values I previously entered`()
      case "capture-certificate-details" => captureCertificateDetails.`form is filled with the values I previously entered`()
      case "confirm" => confirm.`form is filled with the values I previously entered`()
      case "confirm (business acting)" => confirm.`form is filled with the values I previously entered`()
      case "payment" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "payment-prevent-back" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "success" => ???
      case e => throw new RuntimeException(s"unknown 'expected' value")
    }
  }

  @Then( """^the "(.*?)" form is not filled with the values I previously entered$""")
  def `the <expected> form is not filled with the values I previously entered`(expected: String) {
    expected match {
      case "before-you-start" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "vehicle-lookup" => vehicleLookup.`form is not filled`()
      case "setup-business-details" => setupBusinessDetails.`form is not filled`
      case "business-choose-your-address" => businessChooseYourAddress.`form is not filled`
      case "enter-address-manually" => enterAddressManually.`form is not filled`
      case "confirm-business" => confirmBusiness.`form is not filled`()
      case "capture-certificate-details (business acting)" => captureCertificateDetails.`form is not filled`()
      case "capture-certificate-details" => captureCertificateDetails.`form is not filled`()
      case "confirm" => confirm.`form is not filled`()
      case "confirm (business acting)" => confirm.`form is not filled`()
      case "payment" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "payment-prevent-back" => throw new RuntimeException(s"this page cannot be 'filled' as it has no fields")
      case "success" => ???
      case e => throw new RuntimeException(s"unknown 'expected' value")
    }
  }

  @Then( """^the payment, retain and both vehicle-and-keeper cookies are "(.*?)"$""")
  def `the cookies are <wiped>`(wiped: String) {
    wiped match {
      case "wiped" =>
        webDriver.manage().getCookieNamed(FulfilCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(PaymentDetailsCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(VehicleAndKeeperLookupFormModelCacheKey) should equal(null)
        webDriver.manage().getCookieNamed(VehicleAndKeeperLookupResponseCodeCacheKey) should equal(null)
      case "not wiped" => println("not wiped")
      case "-" => println("not created in the first place")

      case e => throw new RuntimeException(s"unknown 'wiped' value: $e")
    }
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
