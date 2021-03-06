package webserviceclients.audit2

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import composition.TestConfig
import composition.WithApplication
import helpers.UnitSpec
import helpers.WireMockFixture
import play.api.libs.json.Json

final class AuditMicroServiceImplSpec extends UnitSpec with WireMockFixture {

  "invoke" should {

    "send the serialised json request" in new WithApplication {
      val resultFuture = auditMicroService.invoke(request)
      whenReady(resultFuture, timeout) { result =>
        wireMock.verifyThat(1, postRequestedFor(
          urlEqualTo(s"/audit/v1")
        ).
          withRequestBody(equalTo(Json.toJson(request).toString())))
      }
    }
  }

  private def auditMicroService = new AuditMicroServiceImpl(
    new TestConfig(auditMicroServiceUrlBase = s"http://localhost:$wireMockPort").stub
  )

  private def request = {
    val data: Seq[(String, Any)] = Seq(("stub-key", "stub-value"))
    AuditRequest(
      name = "transaction id",
      serviceType = "trans no",
      data = data
    )
  }
}