package composition.webserviceclients.paymentsolve

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.paymentsolve.TestPaymentWebServiceBinding.getResponseWithValidDefaults
import composition.webserviceclients.paymentsolve.TestPaymentWebServiceBinding.invalidResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.OK
import webserviceclients.fakes.FakeResponse
import webserviceclients.paymentsolve._

import scala.concurrent.Future

final class NotValidatedAuthorised extends ScalaModule with MockitoSugar {

  val stub = {
    val webService = mock[PaymentSolveWebService]
    when(webService.invoke(request = any[PaymentSolveGetRequest], tracking = any[String])).
      thenReturn(Future.successful(new FakeResponse(status = OK, fakeJson = getResponseWithValidDefaults(response = invalidResponse))))
    webService
  }

  def configure() = bind[PaymentSolveWebService].toInstance(stub)
}
