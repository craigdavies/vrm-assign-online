package composition.webserviceclients.emailservice

import com.tzavellas.sse.guice.ScalaModule
import composition.webserviceclients.emailservice.TestEmailServiceWebServiceBinding.createResponse
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, when}
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import webserviceclients.emailservice.EmailServiceSendRequest
import webserviceclients.emailservice.EmailServiceSendResponse
import webserviceclients.emailservice.EmailServiceWebService
import webserviceclients.fakes.EmailServiceWebServiceConstants.emailServiceSendResponseSuccess
import webserviceclients.fakes.FakeResponse

final class TestEmailServiceWebServiceBinding(emailServiceWebService: EmailServiceWebService = mock(classOf[EmailServiceWebService]), // This can be passed in so the calls to the mock can be verified
                                              statusAndResponse: (Int, Option[EmailServiceSendResponse]) = emailServiceSendResponseSuccess
                                               ) extends ScalaModule with MockitoSugar {

  val stub = {
    when(emailServiceWebService.invoke(any[EmailServiceSendRequest], any[String])).thenReturn(Future.successful(createResponse(statusAndResponse)))
    emailServiceWebService
  }

  def configure() = bind[EmailServiceWebService].toInstance(stub)
}

object TestEmailServiceWebServiceBinding {

  def createResponse(response: (Int, Option[EmailServiceSendResponse])) = {
    val (status, respOpt) = response
    new FakeResponse(status = status)
  }
}