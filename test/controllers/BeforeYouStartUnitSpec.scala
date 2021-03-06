package controllers

import composition.TestConfig
import composition.WithApplication
import controllers.Common.PrototypeHtml
import helpers.UnitSpec
import pages.vrm_assign.BeforeYouStartPage
import play.api.test.FakeRequest
import play.api.test.Helpers.OK
import play.api.test.Helpers.contentAsString
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.Helpers.status

final class BeforeYouStartUnitSpec extends UnitSpec {

  "present" should {

    "display the page" in new WithApplication {
      val result = beforeYouStart.present(FakeRequest())
      status(result) should equal(OK)
      contentAsString(result) should include(BeforeYouStartPage.title)
    }

    "display prototype message when config set to true" in new WithApplication {
      val result = beforeYouStart.present(FakeRequest())
      contentAsString(result) should include(PrototypeHtml)
    }

    "not display prototype message when config set to false" in new WithApplication {
      val request = FakeRequest()
      val result = beforeYouStartPrototypeNotVisible.present(request)
      contentAsString(result) should not include PrototypeHtml
    }
  }

  private def beforeYouStartPrototypeNotVisible = {
    testInjector(new TestConfig(isPrototypeBannerVisible = false)).
      getInstance(classOf[BeforeYouStart])
  }

  private def beforeYouStart = testInjector().getInstance(classOf[BeforeYouStart])
}