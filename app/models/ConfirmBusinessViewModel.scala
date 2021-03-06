package models

import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel
import uk.gov.dvla.vehicles.presentation.common.views.constraints.RegistrationNumber.formatVrm

final case class ConfirmBusinessViewModel(registrationNumber: String,
                                          vehicleMake: Option[String],
                                          vehicleModel: Option[String],
                                          keeperTitle: Option[String],
                                          keeperFirstName: Option[String],
                                          keeperLastName: Option[String],
                                          keeperAddress: Option[AddressModel],
                                          businessName: Option[String],
                                          businessContact: Option[String],
                                          businessEmail: Option[String],
                                          businessAddress: Option[AddressModel])

object ConfirmBusinessViewModel {

  def apply(vehicleAndKeeperDetails: VehicleAndKeeperDetailsModel,
            businessDetailsOpt: Option[BusinessDetailsModel]): ConfirmBusinessViewModel =
    ConfirmBusinessViewModel(
      registrationNumber = formatVrm(vehicleAndKeeperDetails.registrationNumber),
      vehicleMake = vehicleAndKeeperDetails.make,
      vehicleModel = vehicleAndKeeperDetails.model,
      keeperTitle = vehicleAndKeeperDetails.title,
      keeperFirstName = vehicleAndKeeperDetails.firstName,
      keeperLastName = vehicleAndKeeperDetails.lastName,
      keeperAddress = vehicleAndKeeperDetails.address,
      businessName = businessDetailsOpt.map(_.name),
      businessContact = businessDetailsOpt.map(_.contact),
      businessEmail = businessDetailsOpt.map(_.email),
      businessAddress = businessDetailsOpt.map(_.address)
    )
}