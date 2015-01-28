package composition

import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import email.{EmailServiceImpl, EmailService}
import pdf.{PdfServiceImpl, PdfService}
import play.api.{Logger, LoggerLike}
import uk.gov.dvla.vehicles.presentation.common.ConfigProperties.getOptionalProperty
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{AesEncryption, CookieEncryption, CookieNameHashGenerator, Sha1HashGenerator, _}
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.services.{DateService, DateServiceImpl}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients._
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.{AddressLookupServiceImpl, WebServiceImpl}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.{AddressLookupService, AddressLookupWebService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl, BruteForcePreventionWebService}
import utils.helpers.{ConfigImpl, Config, AssignCookieFlags}
import _root_.webserviceclients.paymentsolve.{PaymentSolveServiceImpl, PaymentSolveService, PaymentSolveWebServiceImpl, PaymentSolveWebService}
import _root_.webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperLookupService, VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupWebService, VehicleAndKeeperLookupWebServiceImpl}
import _root_.webserviceclients.vrmassignfulfil.{VrmAssignFulfilWebService, VrmAssignFulfilWebServiceImpl, VrmAssignFulfilServiceImpl, VrmAssignFulfilService}
import _root_.webserviceclients.vrmretentioneligibility._

/**
 * Provides real implementations of traits
 * Note the use of sse-guice, which is a library that makes the Guice internal DSL more scala friendly
 * eg we can write this:
 * bind[Service].to[ServiceImpl].in[Singleton]
 * instead of this:
 * bind(classOf[Service]).to(classOf[ServiceImpl]).in(classOf[Singleton])
 *
 * Look in build.scala for where we import the sse-guice library
 */
final class DevModule extends ScalaModule {

  def configure() {

    bind[Config].to[ConfigImpl].asEagerSingleton()

    bind[AddressLookupService].to[AddressLookupServiceImpl].asEagerSingleton()
    bind[AddressLookupWebService].to[WebServiceImpl].asEagerSingleton()
    bind[VehicleAndKeeperLookupWebService].to[VehicleAndKeeperLookupWebServiceImpl].asEagerSingleton()
    bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()
    bind[DateService].to[DateServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[AssignCookieFlags].asEagerSingleton()
    bind[VrmAssignEligibilityWebService].to[VrmAssignEligibilityWebServiceImpl].asEagerSingleton()
    bind[VrmAssignEligibilityService].to[VrmAssignEligibilityServiceImpl].asEagerSingleton()
    bind[VrmAssignFulfilWebService].to[VrmAssignFulfilWebServiceImpl].asEagerSingleton()
    bind[VrmAssignFulfilService].to[VrmAssignFulfilServiceImpl].asEagerSingleton()
    bind[PaymentSolveWebService].to[PaymentSolveWebServiceImpl].asEagerSingleton()
    bind[PaymentSolveService].to[PaymentSolveServiceImpl].asEagerSingleton()

    bindSessionFactory()

    bind[BruteForcePreventionWebService].to[bruteforceprevention.WebServiceImpl].asEagerSingleton()
    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))
    bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
    bind[audit1.AuditService].to[audit1.AuditLocalServiceImpl].asEagerSingleton()
    bind[RefererFromHeader].to[RefererFromHeaderImpl].asEagerSingleton()
    bind[_root_.webserviceclients.audit2.AuditMicroService].to[_root_.webserviceclients.audit2.AuditMicroServiceImpl].asEagerSingleton()
  }

  protected def bindSessionFactory(): Unit = {
    if (getOptionalProperty[Boolean]("encryptCookies").getOrElse(true)) {
      bind[CookieEncryption].toInstance(new AesEncryption with CookieEncryption)
      bind[CookieNameHashGenerator].toInstance(new Sha1HashGenerator with CookieNameHashGenerator)
      bind[ClientSideSessionFactory].to[EncryptedClientSideSessionFactory].asEagerSingleton()
    } else
      bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()
  }
}