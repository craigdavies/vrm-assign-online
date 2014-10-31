package controllers

import composition.{TestDateService, TestAuditService}
import helpers.vrm_assign.CookieFactoryForUnitSpecs._
import helpers.{UnitSpec, WithApplication}
import pages.vrm_assign.{LeaveFeedbackPage, VehicleLookupPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, OK, contentAsString, defaultAwaitTimeout}
import webserviceclients.fakes.AddressLookupServiceConstants._
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import views.vrm_assign.ConfirmBusiness._
import views.vrm_assign.VehicleLookup._
import helpers.common.CookieHelper._
import com.tzavellas.sse.guice.ScalaModule
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.CookieFlags
import utils.helpers.CookieFlagsassign
import scala.concurrent.duration.DurationInt
import audit.{AuditMessage, AuditService}
import org.mockito.Mockito._
import scala.Some
import uk.gov.dvla.auditing.Message
import uk.gov.dvla.vehicles.presentation.common.services.DateService


final class ConfirmBusinessUnitSpec extends UnitSpec {

//  "present" should {
//
//    "display the page when required cookies are cached" in new WithApplication {
//      whenReady(present, timeout) { r =>
//        r.header.status should equal(OK)
//      }
//    }
//
//    "redirect to VehicleLookup when required cookies do not exist" in new WithApplication {
//      val request = FakeRequest()
//      val result = confirmBusiness.present(request)
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(VehicleLookupPage.address))
//      }
//    }
//
//    "display a summary of previously entered user data" in new WithApplication {
//      val content = contentAsString(present)
//      content should include(BusinessAddressLine1Valid)
//      content should include(BusinessAddressLine2Valid)
//      content should include(BusinessAddressPostTownValid)
//      content should include(RegistrationNumberValid)
//      content should include(VehicleMakeValid.get)
//      content should include(VehicleModelValid.get)
//    }
//  }
//
//  "submit" should {
//
//    "write StoreBusinessDetails cookie when user type is Business and consent is true" in new WithApplication {
//      val mockAuditService = mock[AuditService]
//
//      val injector = testInjector(
//        new TestAuditService(mockAuditService),
//        new TestDateService)
//
//      val confirmBusiness = injector.getInstance(classOf[ConfirmBusiness])
//      val dateService = injector.getInstance(classOf[DateService])
//
//      val data = Seq(("transactionId", "ABC123123123123"),
//        ("timestamp", dateService.dateTimeISOChronology),
//        ("replacementVrm", "SA11AA"),
//        ("currentVrm", "AB12AWR"),
//        ("make", "Alfa Romeo"),
//        ("model", "Alfasud ti"),
//        ("keeperName","Mr David Jones"),
//        ("keeperAddress", "1 HIGH STREET, SKEWEN, POSTTOWN STUB, SA11AA"),
//        ("businessName", "example trader contact"),
//        ("businessAddress", "example trader name, business line1 stub, business line2 stub, business postTown stub, QQ99QQ"),
//        ("businessEmail", "business.example@email.com"))
//      val auditMessage = new AuditMessage(AuditMessage.ConfirmBusinessToConfirm, AuditMessage.PersonalisedRegServiceType, data: _*)
//      val request = buildRequest(storeDetailsConsent = true).
//        withCookies(
//          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
//          vehicleAndKeeperDetailsModel(),
//          businessDetailsModel(),
//          transactionId()
//        )
//      val result = confirmBusiness.submit(request)
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.map(_.name) should contain(StoreBusinessDetailsCacheKey)
//        verify(mockAuditService).send(auditMessage)
//      }
//    }
//
//    "write StoreBusinessDetails cookie with maxAge 7 days" in new WithApplication {
//      val expected = 7.days.toSeconds.toInt
//      val request = buildRequest(storeDetailsConsent = true).
//        withCookies(
//          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
//          vehicleAndKeeperDetailsModel(),
//          businessDetailsModel(),
//          transactionId(),
//          storeBusinessDetailsConsent()
//        )
//      val result = confirmWithCookieFlags.submit(request)
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.map(_.name) should contain (StoreBusinessDetailsCacheKey)
//        cookies.find(cookie => cookie.name == StoreBusinessDetailsCacheKey).get.maxAge should equal(Some(expected))
//      }
//    }
//
//    "write StoreBusinessDetails cookie when user type is Business and consent is false" in new WithApplication {
//      val request = buildRequest(storeDetailsConsent = false).
//        withCookies(
//          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
//          vehicleAndKeeperDetailsModel(),
//          businessDetailsModel(),
//          transactionId()
//        )
//      val result = confirmBusiness.submit(request)
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.map(_.name) should contain (StoreBusinessDetailsCacheKey)
//      }
//    }
//  }
//
//
//  "exit" should {
//
//    "redirect to mock feedback page" in new WithApplication {
//      val request = buildRequest(storeDetailsConsent = false).
//        withCookies(
//          vehicleAndKeeperLookupFormModel(keeperConsent = UserType_Business),
//          vehicleAndKeeperDetailsModel(),
//          businessDetailsModel(),
//          transactionId()
//        )
//      val result = confirmBusiness.exit(request)
//      whenReady(result) { r =>
//        r.header.headers.get(LOCATION) should equal(Some(MockFeedbackPage.address))
//      }
//    }
//  }
//
//
//  private def buildRequest(storeDetailsConsent: Boolean = false) = {
//    FakeRequest().withFormUrlEncodedBody(
//      StoreDetailsConsentId -> storeDetailsConsent.toString
//    )
//  }
//
//  private def confirmBusiness = testInjector(new TestAuditService).getInstance(classOf[ConfirmBusiness])
//
//  private def present = {
//    val request = FakeRequest().
//      withCookies(
//        vehicleAndKeeperLookupFormModel(keeperConsent = BusinessConsentValid),
//        vehicleAndKeeperDetailsModel(),
//        businessDetailsModel()
//      )
//    confirmBusiness.present(request)
//  }
//
//  private def confirmWithCookieFlags = {
//    testInjector(new TestAuditService,
//      new ScalaModule() {
//      override def configure(): Unit = {
//        bind[CookieFlags].to[CookieFlagsassign].asEagerSingleton()
//      }
//    }).getInstance(classOf[ConfirmBusiness])
//  }
}