package webserviceclients.fakes

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import webserviceclients.fakes.AddressLookupWebServiceConstants.traderUprnValid
import webserviceclients.fakes.AddressLookupWebServiceConstants.traderUprnValid2

object AddressLookupServiceConstants {

  final val TraderBusinessNameValid = "example trader name"
  final val TraderBusinessContactValid = "example trader contact"
  final val TraderBusinessEmailValid = "business.example@test.com"
  final val KeeperEmailValid = Some("keeper.example@test.com")
  final val GranteeConsentValid = "true"
  final val StoreBusinessDetailslValid = "true"
  final val PostcodeInvalid = "xx99xx"
  final val PostcodeValid = "QQ99QQ"
  final val BusinessAddressLine1Valid = "business line1 stub"
  final val BusinessAddressLine2Valid = "business line2 stub"
  final val BusinessAddressLine3Valid: String = ""
  final val BusinessAddressPostTownValid = "business postTown stub"
  final val SearchPostcodeValid: String = "AA11AA"
  final val AddressListSelectValid: String = "1"

  def addressWithUprn = AddressModel(
    uprn = Some(traderUprnValid),
    address = Seq(BusinessAddressLine1Valid, BusinessAddressLine2Valid, BusinessAddressPostTownValid, PostcodeValid)
  )

  def addressWithoutUprn = addressWithUprn.copy(uprn = None)

  final val BuildingNameOrNumberValid = "1234"
  final val Line2Valid = "line2 stub"
  final val Line3Valid = "line3 stub"
  final val PostTownValid = "postTown stub"
  final val PostcodeValidWithSpace = "QQ9 9QQ"
  final val PostcodeNoResults = "SA99 1DD"

  def fetchedAddresses = Seq(
    traderUprnValid.toString -> addressWithUprn.address.mkString(", "),
    traderUprnValid2.toString -> addressWithUprn.address.mkString(", ")
  )
}