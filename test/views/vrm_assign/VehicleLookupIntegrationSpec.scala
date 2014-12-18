package views.vrm_assign

import helpers.UiSpec
import helpers.tags.UiTag
import helpers.vrm_assign.CookieFactoryForUISpecs
import composition.TestHarness
import org.openqa.selenium.{By, WebDriver, WebElement}
import pages.common.ErrorPanel
import pages.vrm_assign.VehicleLookupPage.happyPath
import pages.vrm_assign.{BeforeYouStartPage, LeaveFeedbackPage, SetupBusinessDetailsPage, VehicleLookupPage}

final class VehicleLookupIntegrationSpec extends UiSpec with TestHarness {

//  "go to page" should {
//
//    "display the page" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      go to VehicleLookupPage
//
//      page.url should equal(VehicleLookupPage.url)
//    }
//
//    "contain the hidden csrfToken field" taggedAs UiTag in new WebBrowser {
//      go to VehicleLookupPage
//      val csrf: WebElement = webDriver.findElement(By.name(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName))
//      csrf.getAttribute("type") should equal("hidden")
//      csrf.getAttribute("name") should equal(uk.gov.dvla.vehicles.presentation.common.filters.CsrfPreventionAction.TokenName)
//      csrf.getAttribute("value").size > 0 should equal(true)
//    }
//  }
//
//  "findVehicleDetails button" should {
//
//    "redirect to ConfirmPage when valid submission and current keeper" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      happyPath(isCurrentKeeper = true)
//
//      page.url should equal(MockFeedbackPage.url)
//    }
//
//    "redirect to SetupBusinessDetailsPage when valid submission and not current keeper" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      happyPath(isCurrentKeeper = false)
//
//      page.url should equal(SetupBusinessDetailsPage.url)
//    }
//
//    "display one validation error message when no referenceNumber is entered" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      happyPath(referenceNumber = "")
//
//      ErrorPanel.numberOfErrors should equal(1)
//    }
//
//    "display one validation error message when no registrationNumber is entered" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      happyPath(registrationNumber = "")
//
//      ErrorPanel.numberOfErrors should equal(1)
//    }
//
//    "display one validation error message when a registrationNumber is entered containing one character" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      happyPath(registrationNumber = "a")
//
//      ErrorPanel.numberOfErrors should equal(1)
//    }
//
//    "display one validation error message when a registrationNumber is entered containing special characters" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      happyPath(registrationNumber = "$^")
//
//      ErrorPanel.numberOfErrors should equal(1)
//    }
//
//    "display two validation error messages when no vehicle details are entered but consent is given" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      happyPath(referenceNumber = "", registrationNumber = "")
//
//      ErrorPanel.numberOfErrors should equal(2)
//    }
//
//    "display one validation error message when only a valid referenceNumber is entered and consent is given" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      happyPath(registrationNumber = "")
//
//      ErrorPanel.numberOfErrors should equal(1)
//    }
//
//    "display one validation error message when only a valid registrationNumber is entered and consent is given" taggedAs UiTag in new WebBrowser {
//      go to BeforeYouStartPage
//
//      happyPath(referenceNumber = "")
//
//      ErrorPanel.numberOfErrors should equal(1)
//    }
//
//    // TODO need to revisit after store business consent check box change
//    //    "redirect to vrm locked when too many attempting to lookup a locked vrm" taggedAs UiTag in new WebBrowser {
//    //      go to BeforeYouStartPage
//    //
//    //      cacheSetup
//    //
//    //      tryLockedVrm()
//    //
//    //      page.url should equal(VrmLockedPage.url)
//    //    }
//  }

  private def cacheSetup()(implicit webDriver: WebDriver) =
    CookieFactoryForUISpecs.
      bruteForcePreventionViewModel(permitted = false, attempts = 3).vehicleAndKeeperDetailsModel().vehicleAndKeeperLookupFormModel()
}