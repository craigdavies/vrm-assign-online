package composition.vehicleandkeeperlookup

import com.tzavellas.sse.guice.ScalaModule
import composition.vehicleandkeeperlookup.TestVehicleAndKeeperLookupWebService.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import webserviceclients.fakes.VehicleAndKeeperLookupWebServiceConstants._
import webserviceclients.fakes._
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperDetailsResponse, VehicleAndKeeperLookupWebService}

import scala.concurrent.Future

class TestVehicleAndKeeperLookupWebService(
                                            vehicleAndKeeperLookupWebService: VehicleAndKeeperLookupWebService = mock(classOf[VehicleAndKeeperLookupWebService]), // This can be passed in so the calls to the mock can be verified
                                            statusAndResponse: (Int, Option[VehicleAndKeeperDetailsResponse]) = vehicleAndKeeperDetailsResponseSuccess
                                            ) extends ScalaModule with MockitoSugar {

  def configure() = {
    when(vehicleAndKeeperLookupWebService.invoke(any[VehicleAndKeeperDetailsRequest], any[String])).thenReturn(Future.successful(createResponse(statusAndResponse)))
    bind[VehicleAndKeeperLookupWebService].toInstance(vehicleAndKeeperLookupWebService)
  }
}

object TestVehicleAndKeeperLookupWebService {

  def createResponse(response: (Int, Option[VehicleAndKeeperDetailsResponse])) = {
    val (status, dto) = response
    val asJson = Json.toJson(dto)
    new FakeResponse(status = status, fakeJson = Some(asJson))
  }
}