package composition.webserviceclients.bruteforceprevention

import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants
import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants.VrmThrows
import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants.responseFirstAttempt
import _root_.webserviceclients.fakes.BruteForcePreventionWebServiceConstants.responseSecondAttempt
import _root_.webserviceclients.fakes.FakeResponse
import _root_.webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants.RegistrationNumberValid
import com.tzavellas.sse.guice.ScalaModule
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import play.api.http.Status.FORBIDDEN
import play.api.http.Status.OK
import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.BruteForcePreventionWebService

final class TestBruteForcePreventionWebServiceBinding(permitted: Boolean = true) extends ScalaModule with MockitoSugar {

  val stub = {
    val bruteForceStatus = if (permitted) OK else FORBIDDEN
    val bruteForcePreventionWebService = mock[BruteForcePreventionWebService]

    when(bruteForcePreventionWebService.callBruteForce(RegistrationNumberValid)).
      thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus, fakeJson = responseFirstAttempt)))

    when(bruteForcePreventionWebService.callBruteForce(BruteForcePreventionWebServiceConstants.VrmAttempt2)).
      thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus, fakeJson = responseSecondAttempt)))

    when(bruteForcePreventionWebService.callBruteForce(BruteForcePreventionWebServiceConstants.VrmLocked)).
      thenReturn(Future.successful(new FakeResponse(status = bruteForceStatus)))

    when(bruteForcePreventionWebService.callBruteForce(VrmThrows)).
      thenReturn(responseThrows)

    when(bruteForcePreventionWebService.reset(any[String])).
      thenReturn(Future.successful(new FakeResponse(status = play.api.http.Status.OK)))

    bruteForcePreventionWebService
  }

  def configure() = bind[BruteForcePreventionWebService].toInstance(stub)

  private def responseThrows: Future[WSResponse] =
    Future.failed(new RuntimeException("This error is generated deliberately by a stub for BruteForcePreventionWebService"))
}
