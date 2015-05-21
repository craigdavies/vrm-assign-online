package views.vrm_assign

import composition.TestHarness
import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.{click, currentUrl, go}
import pages.vrm_assign.BeforeYouStartPage
import pages.vrm_assign.BeforeYouStartPage.startNow
import pages.vrm_assign.VehicleLookupPage
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebDriverFactory

class BeforeYouStartIntegrationSpec extends UiSpec with TestHarness {

  "go to page" should {
    "display the page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      currentUrl should equal(BeforeYouStartPage.url)
    }

    "remove redundant cookies (needed for when a user exits the service and comes back)" taggedAs UiTag in
      new WebBrowserForSelenium(webDriver = WebDriverFactory.defaultBrowserPhantomJs) {
      def cacheSetup()(implicit webDriver: WebDriver) =
        CookieFactoryForUISpecs.vehicleAndKeeperDetailsModel()
          .setupBusinessDetails()
//          .businessChooseYourAddress()
//          .enterAddressManually()
          .businessDetails()

      go to BeforeYouStartPage
      cacheSetup()
      go to BeforeYouStartPage

      // Verify the cookies identified by the full set of cache keys have been removed
      RelatedCacheKeys.AssignSet.foreach(cacheKey => webDriver.manage().getCookieNamed(cacheKey) should equal(null))
    }
  }

  "startNow button" should {
    "go to next page" taggedAs UiTag in new WebBrowserForSelenium {
      go to BeforeYouStartPage
      click on startNow
      currentUrl should equal(VehicleLookupPage.url)
    }
  }
}