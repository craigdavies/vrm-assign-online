package controllers

import audit1.AuditMessage
import composition.WithApplication
import composition.audit1.AuditLocalServiceDoesNothingBinding
import composition.webserviceclients.audit2.AuditServiceDoesNothing
import helpers.UnitSpec
import helpers.common.CookieHelper._
import helpers.vrm_assign.CookieFactoryForUnitSpecs._
import helpers.vrm_assign.CookieFactoryForUnitSpecs.businessDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.enterAddressManually
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperDetailsModel
import helpers.vrm_assign.CookieFactoryForUnitSpecs.vehicleAndKeeperLookupFormModel
import org.mockito.Mockito._
import pages.vrm_assign.{LeaveFeedbackPage, VehicleLookupPage}
import pages.vrm_assign.BusinessChooseYourAddressPage
import pages.vrm_assign.EnterAddressManuallyPage
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import views.vrm_assign.ConfirmBusiness._
import views.vrm_assign.VehicleLookup._
import views.vrm_assign.VehicleLookup.UserType_Business
import webserviceclients.audit2.AuditRequest
import webserviceclients.fakes.AddressLookupServiceConstants._
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._

final class ConfirmBusinessUnitSpec extends UnitSpec {

  "present" should {

    "display the page when required cookies are cached" in new WithApplication {
      whenReady(present, timeout) { r =>
        r.header.status should equal(OK)
      }
    }

    "redirect to VehicleLookup when required cookies do not exist" in new WithApplication {
      val request = FakeRequest()
      val result = confirmBusiness.present(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
      }
    }

    "display a summary of previously entered user data" in new WithApplication {
      val content = contentAsString(present)
      content should include(BusinessAddressLine1Valid)
      content should include(BusinessAddressLine2Valid)
      content should include(BusinessAddressPostTownValid)
      content should include(RegistrationNumberWithSpaceValid)
      content should include(VehicleMakeValid.get)
      content should include(VehicleModelValid.get)
    }
  }

  "submit" should {

    "write StoreBusinessDetails cookie when user type is Business and consent is true" in new WithApplication {
      val auditLocalService1 = new AuditLocalServiceDoesNothingBinding
      val auditService2 = new AuditServiceDoesNothing

      val injector = testInjector(
        auditLocalService1,
        auditService2
      )

      val confirmBusiness = injector.getInstance(classOf[ConfirmBusiness])
      val dateService = injector.getInstance(classOf[DateService])

      val data = Seq(("transactionId", "ABC123123123123"),
        ("timestamp", dateService.dateTimeISOChronology),
        ("currentVrm", "AB12AWR"),
        ("make", "Alfa Romeo"),
        ("model", "Alfasud ti"),
        ("keeperName", "MR DAVID JONES"),
        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA"),
        ("businessName", "example trader contact"),
        ("businessAddress", "example trader name, business line1 stub, business line2 stub, business postTown stub, QQ99QQ"),
        ("businessEmail", "business.example@test.com"))
      val auditMessage = new AuditMessage(AuditMessage.ConfirmBusinessToCaptureCertificateDetails, AuditMessage.AuditServiceType, data: _*)
      val auditRequest = new AuditRequest(AuditMessage.ConfirmBusinessToCaptureCertificateDetails, AuditMessage.AuditServiceType, data)
      val request = buildRequest(storeDetailsConsent = true).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          transactionId()
        )
      val result = confirmBusiness.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(StoreBusinessDetailsCacheKey)
        verify(auditLocalService1.stub).send(auditMessage)
        verify(auditService2.stub).send(auditRequest)
      }
    }

    //      "write StoreBusinessDetails cookie with maxAge 7 days" in new WithApplication {
    //        val expected = 7.days.toSeconds.toInt
    //        val request = buildRequest(storeDetailsConsent = true).
    //          withCookies(
    //            vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
    //            vehicleAndKeeperDetailsModel(),
    //            businessDetailsModel(),
    //            transactionId(),
    //            storeBusinessDetailsConsent()
    //          )
    //        val result = confirmWithCookieFlags.submit(request)
    //        whenReady(result) { r =>
    //          val cookies = fetchCookiesFromHeaders(r)
    //          cookies.map(_.name) should contain (StoreBusinessDetailsCacheKey)
    //          cookies.find(cookie => cookie.name == StoreBusinessDetailsCacheKey).get.maxAge should equal(Some(expected))
    //        }
    //      }

    "write StoreBusinessDetails cookie when user type is Business and consent is false" in new WithApplication {
      val request = buildRequest(storeDetailsConsent = false).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          transactionId()
        )
      val result = confirmBusiness.submit(request)
      whenReady(result) { r =>
        val cookies = fetchCookiesFromHeaders(r)
        cookies.map(_.name) should contain(StoreBusinessDetailsCacheKey)
      }
    }
  }

  "back" should {
    "redirect to EnterAddressManually page when EnterAddressManually cookie exists" in new WithApplication {
      val request = buildRequest(storeDetailsConsent = false).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          enterAddressManually()
        )
      val result = confirmBusiness.back(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(EnterAddressManuallyPage.address))
      }
    }

    "redirect to BusinessChooseYourAddress page when EnterAddressManually cookie does not exist" in new WithApplication {
      val request = buildRequest(storeDetailsConsent = false).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel()
        )
      val result = confirmBusiness.back(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(BusinessChooseYourAddressPage.address))
      }
    }
  }

  "exit" should {

    "redirect to mock feedback page" in new WithApplication {
      val request = buildRequest(storeDetailsConsent = false).
        withCookies(
          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
          vehicleAndKeeperDetailsModel(),
          businessDetailsModel(),
          transactionId()
        )
      val result = confirmBusiness.exit(request)
      whenReady(result) { r =>
        r.header.headers.get(LOCATION) should equal(Some(LeaveFeedbackPage.address))
      }
    }
  }

  private def buildRequest(storeDetailsConsent: Boolean = false) = {
    FakeRequest().withFormUrlEncodedBody(
      StoreDetailsConsentId -> storeDetailsConsent.toString
    )
  }

  private def confirmBusiness = testInjector().getInstance(classOf[ConfirmBusiness])

  private def present = {
    val request = FakeRequest().
      withCookies(
        vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
        vehicleAndKeeperDetailsModel(),
        businessDetailsModel()
      )
    confirmBusiness.present(request)
  }
}