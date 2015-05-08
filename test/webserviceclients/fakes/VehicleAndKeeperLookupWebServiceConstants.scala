package webserviceclients.fakes

import play.api.http.Status.OK
import play.api.http.Status.SERVICE_UNAVAILABLE
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperDetailsDto
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupErrorMessage
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.vehicleandkeeperlookup.VehicleAndKeeperLookupResponseV2
import views.vrm_assign.VehicleLookup.UserType_Keeper
import webserviceclients.fakes.AddressLookupServiceConstants._

object VehicleAndKeeperLookupWebServiceConstants {

  final val RegistrationNumberValid = "AB12AWR"
  final val RegistrationNumberWithSpaceValid = "AB12 AWR"
  final val ReferenceNumberValid = "12345678910"
  final val TransactionIdValid = "ABC123123123123"
  final val PaymentTransNoValid = "123456"

  def VehicleMakeValid = Some("Alfa Romeo")

  def VehicleModelValid = Some("Alfasud ti")

  final val KeeperNameValid = "Keeper Name"
  final val KeeperUprnValid = 10123456789L
  final val ConsentValid = "true"
  final val KeeperConsentValid = UserType_Keeper
  final val BusinessConsentValid = "Business"
  final val KeeperPostcodeValid = PostcodeValid
  final val KeeperPostcodeValidForMicroService = "SA11AA"

  def KeeperTitleValid = Some("MR")

  def KeeperLastNameValid = Some("JONES")

  def KeeperFirstNameValid = Some("DAVID")

  def KeeperAddressLine1Valid = Some("1 High Street")

  def KeeperAddressLine2Valid = Some("Skewen")

  def KeeperAddressLine3Valid = None

  def KeeperAddressLine4Valid = None

  def KeeperPostTownValid = Some("Swansea")

  def KeeperPostCodeValid = Some("SA11AA")

  final val RecordMismatch = VehicleAndKeeperLookupErrorMessage(code = "vehicle_and_keeper_lookup_document_record_mismatch", message = "200")

  private def vehicleAndKeeperDetails = VehicleAndKeeperDetailsDto(registrationNumber = RegistrationNumberValid,
    vehicleMake = VehicleMakeValid,
    vehicleModel = VehicleModelValid,
    keeperTitle = KeeperTitleValid,
    keeperFirstName = KeeperFirstNameValid,
    keeperLastName = KeeperLastNameValid,
    keeperAddressLine1 = KeeperAddressLine1Valid,
    keeperAddressLine2 = KeeperAddressLine2Valid,
    keeperAddressLine3 = KeeperAddressLine3Valid,
    keeperAddressLine4 = KeeperAddressLine4Valid,
    keeperPostTown = KeeperPostTownValid,
    keeperPostcode = KeeperPostCodeValid,
    disposeFlag = None,
    keeperEndDate = None,
    keeperChangeDate = None,
    suppressedV5Flag = None
  )

  def vehicleAndKeeperDetailsResponseSuccess: (Int, Option[VehicleAndKeeperLookupResponseV2]) = {
    (OK, Some(VehicleAndKeeperLookupResponseV2(responseCode = None, vehicleAndKeeperDetailsDto = Some(vehicleAndKeeperDetails))))
  }

  def vehicleAndKeeperDetailsResponseVRMNotFound: (Int, Option[VehicleAndKeeperLookupResponseV2]) = {
    (OK, Some(VehicleAndKeeperLookupResponseV2(responseCode = Some(VehicleAndKeeperLookupErrorMessage(code = "vehicle_lookup_vrm_not_found", message = "200")), vehicleAndKeeperDetailsDto = None)))
  }

  def vehicleAndKeeperDetailsResponseDocRefNumberNotLatest: (Int, Option[VehicleAndKeeperLookupResponseV2]) = {
    (OK, Some(VehicleAndKeeperLookupResponseV2(
      responseCode = Some(RecordMismatch),
      vehicleAndKeeperDetailsDto = None
    )))
  }

  def vehicleAndKeeperDetailsResponseNotFoundResponseCode: (Int, Option[VehicleAndKeeperLookupResponseV2]) = {
    (OK, Some(VehicleAndKeeperLookupResponseV2(responseCode = None, vehicleAndKeeperDetailsDto = None)))
  }

  def vehicleAndKeeperDetailsServerDown: (Int, Option[VehicleAndKeeperLookupResponseV2]) = {
    (SERVICE_UNAVAILABLE, None)
  }

  def vehicleAndKeeperDetailsNoResponse: (Int, Option[VehicleAndKeeperLookupResponseV2]) = {
    (OK, None)
  }
}
