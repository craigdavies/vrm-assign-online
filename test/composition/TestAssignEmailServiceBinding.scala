package composition

import com.tzavellas.sse.guice.ScalaModule
import email.AssignEmailService
import models._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito.when
import org.mockito.Matchers.any
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

final class TestAssignEmailServiceBinding extends ScalaModule with MockitoSugar {

  val stub = mock[AssignEmailService]
  when(stub.emailRequest(
    any[String],
    any[VehicleAndKeeperDetailsModel],
    any[CaptureCertificateDetailsFormModel],
    any[CaptureCertificateDetailsModel],
    any[VehicleAndKeeperLookupFormModel],
    any[String],
    any[String],
    any[Option[ConfirmFormModel]],
    any[Option[BusinessDetailsModel]],
    any[Boolean],
    any[String]
  )).thenReturn(None)

  def configure() = {
    bind[AssignEmailService].toInstance(stub)
  }
}