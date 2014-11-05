package models

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel


final case class ConfirmViewModel(registrationNumber: String,
                                  replacementRegistration: String,
                                  vehicleMake: Option[String],
                                  vehicleModel: Option[String],
                                  keeperTitle: Option[String],
                                  keeperFirstName: Option[String],
                                  keeperLastName: Option[String],
                                  keeperAddress: Option[AddressModel])

object ConfirmViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel): ConfirmViewModel =
    ConfirmViewModel(
      registrationNumber = vehicleAndKeeperDetails.registrationNumber,
      replacementRegistration = "AMV1",
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      keeperTitle = vehicleAndKeeperDetails.title,
      keeperFirstName = vehicleAndKeeperDetails.firstName,
      keeperLastName = vehicleAndKeeperDetails.lastName,
      keeperAddress = vehicleAndKeeperDetails.address
    )
}