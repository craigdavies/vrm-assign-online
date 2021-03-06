package webserviceclients.audit2

import com.google.inject.Inject
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.libs.ws.WSResponse
import scala.concurrent.Future
import utils.helpers.Config

final class AuditMicroServiceImpl @Inject()(config: Config) extends AuditMicroService {

  override def invoke(request: AuditRequest): Future[WSResponse] = {
    val endPoint: String = s"${config.auditMicroServiceUrlBase}/audit/v1"
    val requestAsJson = Json.toJson(request)

    WS.url(endPoint).
      withRequestTimeout(config.auditMsRequestTimeout). // Timeout is in milliseconds
      post(requestAsJson)
  }
}