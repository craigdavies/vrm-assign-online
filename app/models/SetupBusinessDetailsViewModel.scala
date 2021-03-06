package models

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class SetupBusinessDetailsViewModel(registrationNumber: String,
                                               vehicleMake: Option[String],
                                               vehicleModel: Option[String],
                                               title: Option[String],
                                               firstName: Option[String],
                                               lastName: Option[String],
                                               address: Option[AddressModel])

object SetupBusinessDetailsViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): SetupBusinessDetailsViewModel =
    SetupBusinessDetailsViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperDetails.registrationNumber),
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      title = vehicleAndKeeperDetails.title,
      firstName = vehicleAndKeeperDetails.firstName,
      lastName = vehicleAndKeeperDetails.lastName,
      address = vehicleAndKeeperDetails.address
    )
}