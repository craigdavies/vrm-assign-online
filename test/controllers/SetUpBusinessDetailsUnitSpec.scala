package controllers

import composition.TestConfig
import composition.WithApplication
import controllers.Common.PrototypeHtml
import helpers.JsonUtils.deserializeJsonToModel
import helpers.UnitSpec
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_assign.CookieFactoryForUnitSpecs.setupBusinessDetails
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import models.SetupBusinessDetailsFormModel
import pages.vrm_assign.{ConfirmBusinessPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, LOCATION, OK, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.mappings.BusinessName
import views.vrm_assign.SetupBusinessDetails.BusinessContactId
import views.vrm_assign.SetupBusinessDetails.BusinessEmailId
import views.vrm_assign.SetupBusinessDetails.BusinessNameId
import views.vrm_assign.SetupBusinessDetails.SetupBusinessDetailsCacheKey
import webserviceclients.fakes.AddressLookupServiceConstants.PostcodeValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessContactValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessEmailValid
import webserviceclients.fakes.AddressLookupServiceConstants.TraderBusinessNameValid
import uk.gov.dvla.vehicles.presentation.common.mappings.Email.{EmailId, EmailVerifyId}

class SetUpBusinessDetailsUnitSpec extends UnitSpec {

  "present" should {
    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }

    "display populated fields when cookie exists" in new WithApplication {
      val request = FakeRequest().
        withCookies(
          setupBusinessDetails(),
          vehicleAndKeeperDetailsModel()
        )
      val result = setUpBusinessDetails().present(request)
      val content = contentAsString(result)
      content should include(TraderBusinessNameValid)
      content should include(PostcodeValid)
    }

    "display empty fields when setupBusinessDetails cookie does not exist" in new WithApplication {
      val content = contentAsString(present)
      content should not include TraderBusinessNameValid
      content should not include PostcodeValid
    }

    "display prototype message when config set to true" in new WithApplication {
      contentAsString(present) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = setUpBusinessDetailsPrototypeNotVisible().present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  "submit" should {
    "redirect to VehicleLookup page if required cookies do not exist" in new WithApplication {
      val request = FakeRequest()
      val result = setUpBusinessDetails().submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "redirect to next page when the form is completed successfully" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = setUpBusinessDetails().submit(request)
      whenReady(result) {
        r =>
          r.header.headers.get(LOCATION) should equal(Some(ConfirmBusinessPage.address))
          val cookies = fetchCookiesFromHeaders(r)
          val cookieName = SetupBusinessDetailsCacheKey
          cookies.find(_.name == cookieName) match {
            case Some(cookie) =>
              val json = cookie.value
              val model = deserializeJsonToModel[SetupBusinessDetailsFormModel](json)
              model.name should equal(TraderBusinessNameValid.toUpperCase)
              model.address.postCode should equal(PostcodeValid.toUpperCase)
            case None => fail(s"$cookieName cookie not found")
          }
      }
    }

    "return a bad request if no details are entered" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(dealerName = "", dealerPostcode = "").
        withCookies(vehicleAndKeeperDetailsModel())
      val result = setUpBusinessDetails().submit(request)
      whenReady(result) { r =>
        r.header.status should equal(BAD_REQUEST)
      }
    }

    "replace max length error message for traderBusinessName with standard error message (US158)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(dealerName = "a" * (BusinessName.MaxLength + 1)).
        withCookies(vehicleAndKeeperDetailsModel())
      val result = setUpBusinessDetails().submit(request)
      val content = contentAsString(result)
      val count = "Must be between two and 58 characters and only contain valid characters".
        r.findAllIn(content).length
      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of
      // the page and once above the field.
    }

    "replace required and min length error messages for traderBusinessName with standard error message (US158)" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest(dealerName = "").
        withCookies(vehicleAndKeeperDetailsModel())
      val result = setUpBusinessDetails().submit(request)
      val content = contentAsString(result)
      val count = "Must be between two and 58 characters and only contain valid characters".
        r.findAllIn(content).length
      count should equal(2) // The same message is displayed in 2 places - once in the validation-summary at the top of
      // the page and once above the field.
    }

    "write cookie when the form is completed successfully" in new WithApplication {
      val request = buildCorrectlyPopulatedRequest()
      val result = setUpBusinessDetails().submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(SetupBusinessDetailsCacheKey)
      }
    }
  }

  private def setUpBusinessDetails() = testInjector().getInstance(classOf[SetUpBusinessDetails])

  private def present = {
    val request = FakeRequest().
      withCookies(vehicleAndKeeperDetailsModel())
    setUpBusinessDetails().present(request)
  }

  private def setUpBusinessDetailsPrototypeNotVisible() = {
    testInjector(new TestConfig(isPrototypeBannerVisible = false)).
      getInstance(classOf[SetUpBusinessDetails])
  }

  private def buildCorrectlyPopulatedRequest(dealerName: String = TraderBusinessNameValid,
                                             dealerContact: String = TraderBusinessContactValid,
                                             dealerEmail: String = TraderBusinessEmailValid,
                                             dealerPostcode: String = PostcodeValid) = {
    FakeRequest().withFormUrlEncodedBody(
      BusinessNameId -> dealerName,
      BusinessContactId -> dealerContact,
      s"$BusinessEmailId.$EmailId" -> dealerEmail,
      s"$BusinessEmailId.$EmailVerifyId" -> dealerEmail,
    // TODO: ian use ids here
      "business-postcode.address-line-1" -> "Test line 1",
      "business-postcode.post-town" -> "Test town",
      "business-postcode.post-code" -> dealerPostcode)
  }
}
