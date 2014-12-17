package controllers

import composition.{TestDateService, TestAuditService, TestConfig}
import helpers.JsonUtils.deserializeJsonToModel
import helpers.common.CookieHelper.fetchCookiesFromHeaders
import helpers.vrm_assign.CookieFactoryForUnitSpecs.{captureCertificateDetailsFormModel, captureCertificateDetailsModel, vehicleAndKeeperDetailsModel, vehicleAndKeeperLookupFormModel}
import helpers.{UnitSpec, WithApplication}
import models._
import pages.vrm_assign.{SuccessPage, PaymentPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.{LOCATION, _}
import views.vrm_assign.Confirm.{ConfirmCacheKey, KeeperEmailId}
import views.vrm_assign.Confirm.KeeperEmailCacheKey
import webserviceclients.fakes.ConfirmFormConstants.KeeperEmailValid

final class ConfirmUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      whenReady(present) { r =>
        r.header.status should equal(OK)
      }
    }
  }

//  "submit" should {
//
//    "redirect to next page when the form is completed successfully" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = confirm.submit(request)
//      whenReady(result) {
//        r =>
//          r.header.headers.get(LOCATION) should equal(Some(SuccessPage.address))
//          val cookies = fetchCookiesFromHeaders(r)
//          val cookieName = KeeperEmailCacheKey
//          cookies.find(_.name == cookieName) match {
//            case Some(cookie) =>
//              val json = cookie.value
//              val model = deserializeJsonToModel[ConfirmFormModel](json)
//              model.keeperEmail should equal(Some(KeeperEmailValid))
//            case None => fail(s"$cookieName cookie not found")
//          }
//      }
//    }
//
//    "write cookie when the form is completed successfully" in new WithApplication {
//      val request = buildCorrectlyPopulatedRequest()
//      val result = confirm.submit(request)
//      whenReady(result) { r =>
//        val cookies = fetchCookiesFromHeaders(r)
//        cookies.map(_.name) should contain(KeeperEmailCacheKey)
//      }
//    }
//  }

  private lazy val confirm = testInjector().getInstance(classOf[Confirm])
  private lazy val present = {
    val request = FakeRequest().
      withCookies(vehicleAndKeeperDetailsModel()).
      withCookies(vehicleAndKeeperLookupFormModel()).
      withCookies(captureCertificateDetailsFormModel()).
      withCookies(captureCertificateDetailsModel())
      confirm.present(request)
  }

  private def setUpBusinessDetailsPrototypeNotVisible() = {
    testInjector(
      new TestConfig(isPrototypeBannerVisible = false),
      new TestAuditService(),
      new TestDateService()
    ).
      getInstance(classOf[Confirm])
  }

  private def buildCorrectlyPopulatedRequest(keeperEmail: String = KeeperEmailValid) = {
    FakeRequest().withFormUrlEncodedBody(
      KeeperEmailId -> keeperEmail)
  }
}