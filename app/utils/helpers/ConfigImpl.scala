package utils.helpers

import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.booleanProp
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getDurationProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getStringListProperty
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.intProp
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.longProp
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.stringProp
import uk.gov.dvla.vehicles.presentation.common.services.SEND.EmailConfiguration
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.emailservice.From

class ConfigImpl extends Config {

  val assetsUrl: Option[String] = getOptionalProperty[String]("assets.url")

  val isCsrfPreventionEnabled = getProperty[Boolean]("csrf.prevention")

  // Micro-service config
  val vehicleAndKeeperLookupMicroServiceBaseUrl: String = getProperty[String]("vehicleAndKeeperLookupMicroServiceUrlBase")
  val vrmAssignEligibilityMicroServiceUrlBase: String = getProperty[String]("vrmAssignEligibilityMicroServiceUrlBase")
  val vrmAssignFulfilMicroServiceUrlBase: String = getProperty[String]("vrmAssignFulfilMicroServiceUrlBase")
  val paymentSolveMicroServiceUrlBase: String = getProperty[String]("paymentSolveMicroServiceUrlBase")
  val paymentSolveMsRequestTimeout: Int = getProperty[Int]("paymentSolve.ms.requesttimeout")

  // Ordnance survey config
  val ordnanceSurveyMicroServiceUrl: String = getProperty[String]("ordnancesurvey.ms.url")
  val ordnanceSurveyRequestTimeout: Int = getProperty[Int]("ordnancesurvey.requestTimeout")
  val ordnanceSurveyUseUprn: Boolean = getProperty[Boolean]("ordnancesurvey.useUprn")

  val vehicleAndKeeperLookupRequestTimeout: Int = getProperty[Int]("vehicleAndKeeperLookup.requesttimeout")
  val vrmAssignEligibilityRequestTimeout: Int = getProperty[Int]("vrmAssignEligibility.requestTimeout")
  val vrmAssignFulfilRequestTimeout: Int = getProperty[Int]("vrmAssignFulfil.requestTimeout")

  // Prototype message in html
  val isPrototypeBannerVisible: Boolean = getProperty[Boolean]("prototype.disclaimer")

  // Prototype survey URL
  val prototypeSurveyUrl: String = getOptionalProperty[String]("survey.url").getOrElse("")
  val prototypeSurveyPrepositionInterval: Long = getDurationProperty("survey.interval")

  // Google analytics
  val googleAnalyticsTrackingId: Option[String] = getOptionalProperty[String]("googleAnalytics.id.assign")

  // Progress step indicator
  val isProgressBarEnabled: Boolean = getProperty[Boolean]("progressBar.enabled")

  // Rabbit-MQ
  val rabbitmqHost = getProperty[String]("rabbitmq.host")
  val rabbitmqPort = getProperty[Int]("rabbitmq.port")
  val rabbitmqQueue = getProperty[String]("rabbitmq.queue")
  val rabbitmqUsername = getProperty[String]("rabbitmq.username")
  val rabbitmqPassword = getProperty[String]("rabbitmq.password")
  val rabbitmqVirtualHost = getProperty[String]("rabbitmq.virtualHost")

  // Payment Service
  val renewalFee: String = getProperty[String]("assign.renewalFee.price")
  val renewalFeeAbolitionDate: String = getProperty[String]("assign.renewalFee.abolitionDate")

  // Email Service
  val emailWhitelist: Option[List[String]] = getOptionalProperty[String]("email.whitelist").map(_.split(",").toList)
  //getProperty[("email.whitelist", "").split(",")
  val emailSenderAddress: String = getProperty[String]("email.senderAddress")
  override val emailConfiguration: EmailConfiguration = EmailConfiguration(
    From(getProperty[String]("email.senderAddress"), "DO-NOT-REPLY"),
    From(getProperty[String]("email.feedbackAddress"), "Feedback"),
    getStringListProperty("email.whitelist")
  )

  // Cookie flags
  val encryptCookies = getProperty[Boolean]("encryptCookies")
  val secureCookies = getProperty[Boolean]("secureCookies")
  val cookieMaxAge = getProperty[Int]("application.cookieMaxAge")
  val storeBusinessDetailsMaxAge = getProperty[Int]("storeBusinessDetails.cookieMaxAge")

  // Audit microservice
  val auditMicroServiceUrlBase: String = getProperty[String]("auditMicroServiceUrlBase")
  val auditMsRequestTimeout: Int = getProperty[Int]("audit.requesttimeout")

  // Email microservice
  val emailServiceMicroServiceUrlBase: String = getProperty[String]("emailServiceMicroServiceUrlBase")
  val emailServiceMsRequestTimeout: Int = getProperty[Int]("emailService.ms.requesttimeout")

  // Web headers
  val applicationCode: String = getProperty[String]("webHeader.applicationCode")
  val vssServiceTypeCode: String = getProperty[String]("webHeader.vssServiceTypeCode")
  val dmsServiceTypeCode: String = getProperty[String]("webHeader.dmsServiceTypeCode")
  val orgBusinessUnit: String = getProperty[String]("webHeader.orgBusinessUnit")
  val channelCode: String = getProperty[String]("webHeader.channelCode")
  val contactId: Long = getProperty[Long]("webHeader.contactId")

  override val opening: Int = getOptionalProperty[Int]("openingTime").getOrElse(8)
  override val closing: Int = getOptionalProperty[Int]("closingTime").getOrElse(18)
  override val closingWarnPeriodMins: Int = getOptionalProperty[Int]("closingWarnPeriodMins").getOrElse(15)
}