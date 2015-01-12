package utils.helpers

import play.api.Play
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties._
import scala.concurrent.duration.DurationInt

class Config {

  val isCsrfPreventionEnabled = getProperty("csrf.prevention", default = true)

  // Micro-service config // TODO take defaults off the timeouts
  val vehicleAndKeeperLookupMicroServiceBaseUrl: String = getProperty("vehicleAndKeeperLookupMicroServiceUrlBase", "NOT FOUND")
  val vrmAssignEligibilityMicroServiceUrlBase: String = getProperty("vrmAssignEligibilityMicroServiceUrlBase", "NOT FOUND")
  val vrmAssignFulfilMicroServiceUrlBase: String = getProperty("vrmAssignFulfilMicroServiceUrlBase", "NOT FOUND")
  val paymentSolveMicroServiceUrlBase: String = getProperty("paymentSolveMicroServiceUrlBase", "NOT FOUND")
  val paymentSolveMsRequestTimeout: Int = getProperty("paymentSolve.ms.requesttimeout", 5.seconds.toMillis.toInt)

  // Ordnance survey config
  val ordnanceSurveyMicroServiceUrl: String = getProperty("ordnancesurvey.ms.url", "NOT FOUND")
  val ordnanceSurveyRequestTimeout: Int = getProperty("ordnancesurvey.requesttimeout", 5.seconds.toMillis.toInt)
  val ordnanceSurveyUseUprn: Boolean = getProperty("ordnancesurvey.useUprn", default = false)

  val vehicleAndKeeperLookupRequestTimeout: Int = getProperty("vehicleAndKeeperLookup.requesttimeout", 30.seconds.toMillis.toInt)
  val vrmAssignEligibilityRequestTimeout: Int = getProperty("vrmAssignEligibility.requesttimeout", 30.seconds.toMillis.toInt)
  val vrmAssignFulfilRequestTimeout: Int = getProperty("vrmAssignFulfil.requesttimeout", 30.seconds.toMillis.toInt)

  // Prototype message in html
  val isPrototypeBannerVisible: Boolean = getProperty("prototype.disclaimer", default = true)

  // Prototype survey URL
  val prototypeSurveyUrl: String = getProperty("survey.url", "")
  val prototypeSurveyPrepositionInterval: Long = getDurationProperty("survey.interval", 7.days.toMillis)

  // Google analytics
  val googleAnalyticsTrackingId: String = getProperty("googleAnalytics.id.assign", "NOT FOUND")

  // Progress step indicator
  val isProgressBarEnabled: Boolean = getProperty("progressBar.enabled", default = true)

  // Audit Service
  val auditServiceUseRabbit = getProperty("auditService.useRabbit", default = false)

  // Rabbit-MQ
  val rabbitmqHost = getProperty("rabbitmq.host", "NOT FOUND")
  val rabbitmqPort = getProperty("rabbitmq.port", 0)
  val rabbitmqQueue = getProperty("rabbitmq.queue", "NOT FOUND")

  // Payment Service
  val renewalFee: String = getProperty("assign.renewalFee", "NOT FOUND")

  // Email Service
  val emailSmtpHost: String = getProperty("smtp.host", "")
  val emailSmtpPort: Int = getProperty("smtp.port", 25)
  val emailSmtpSsl: Boolean = getProperty("smtp.ssl", default = false)
  val emailSmtpTls: Boolean = getProperty("smtp.tls", default = true)
  val emailSmtpUser: String = getProperty("smtp.user", "")
  val emailSmtpPassword: String = getProperty("smtp.password", "")
  val emailWhitelist: Array[String] = getProperty("email.whitelist", "").split(",")
  val emailSenderAddress: String = getProperty("email.senderAddress", "")

  // Cookie flags
  val secureCookies = getProperty("secureCookies", default = true)
  val cookieMaxAge = getProperty("application.cookieMaxAge", 30.minutes.toSeconds.toInt)
  val storeBusinessDetailsMaxAge = getProperty("storeBusinessDetails.cookieMaxAge", 7.days.toSeconds.toInt)
}