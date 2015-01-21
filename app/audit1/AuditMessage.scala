package audit1

import models._
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.auditing.Message


case class AuditMessage(override val name: String, override val serviceType: String, override val data: (String, Any)*)
  extends Message(name, serviceType, data :_*)


object KeeperNameOptString {

  def from(vehicleAndKeeperDetailsModel: VehicleAndKeeperDetailsModel) = {

    // flatten and then iterate
    val keeperNameList = List(vehicleAndKeeperDetailsModel.title,
      vehicleAndKeeperDetailsModel.firstName,
      vehicleAndKeeperDetailsModel.lastName).flatten

    if (keeperNameList.size > 0) {
      var nameString = keeperNameList(0)
      for (nameItem <- keeperNameList.drop(1)) {
        nameString += (" " + nameItem)
      }
      Some(nameString)
    } else {
      None
    }
  }
}

object KeeperAddressOptString {

  def from(addressModel: Option[AddressModel]) = {

    addressModel match {
      case Some(address) =>
        if (address.address.size > 0) {
          var addressString = address.address(0)
          for (addressLine <- address.address.drop(1)) {
            addressString += (", " + addressLine)
          }
          Some(addressString)
        } else {
          None
        }
      case _ =>
        None
    }
  }
}

object BusinessAddressOptString {

  def from(businessDetailsModel: BusinessDetailsModel) = {

    var addressString = businessDetailsModel.name

    if (businessDetailsModel.address.address.size > 0) {
      for (addressLine <- businessDetailsModel.address.address) {
        addressString += (", " + addressLine)
      }
    }
    Some(addressString)
  }
}

object BusinessDetailsModelOptSeq {

  def from(businessDetailsModel: Option[BusinessDetailsModel]) = {
    businessDetailsModel match {
      case Some(businessDetailsModel) => {
        val businessNameOpt = Some(("businessName", businessDetailsModel.contact))
        val businessAddressOpt = BusinessAddressOptString.from(businessDetailsModel).map(
          businessAddress => ("businessAddress", businessAddress))
        val businessEmailOpt = Some(("businessEmail", businessDetailsModel.email))
        Seq(businessNameOpt, businessAddressOpt, businessEmailOpt)
      }
      case _ => Seq.empty
    }
  }
}

object VehicleAndKeeperDetailsModelOptSeq {

  def from(vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel]) = {
    vehicleAndKeeperDetailsModel match {
      case Some(vehicleAndKeeperDetailsModel) => {
        val currentVrmOpt = Some(("currentVrm", vehicleAndKeeperDetailsModel.registrationNumber))
        val makeOpt = vehicleAndKeeperDetailsModel.make.map(make => ("make", make))
        val modelOpt = vehicleAndKeeperDetailsModel.model.map(model => ("model", model))
        val keeperNameOpt = KeeperNameOptString.from(vehicleAndKeeperDetailsModel).map(
          keeperName => ("keeperName", keeperName))
        val keeperAddressOpt = KeeperAddressOptString.from(vehicleAndKeeperDetailsModel.address).map(
          keeperAddress => ("keeperAddress", keeperAddress))
        Seq(currentVrmOpt, makeOpt, modelOpt, keeperNameOpt, keeperAddressOpt)
      }
      case _ => Seq.empty
    }
  }
}


object CaptureCertificateDetailsFormModelOptSeq {

  def from(captureCertificateDetailsFormModel: Option[CaptureCertificateDetailsFormModel]) = {
    captureCertificateDetailsFormModel match {
      case Some(captureCertificateDetailsFormModel) => {
        val certificateDocumentCountOpt = Some(("certificateDocumentCount", captureCertificateDetailsFormModel.certificateDocumentCount))
        val certificateDateOpt = Some(("certificateDate", captureCertificateDetailsFormModel.certificateDate))
        val certificateTimeOpt = Some(("certificateTime", captureCertificateDetailsFormModel.certificateTime))
        val certificateRegistrationMarkOpt = Some(("certificateRegistrationMark", captureCertificateDetailsFormModel.certificateRegistrationMark))
        val prVrmOpt = Some(("prVrm", captureCertificateDetailsFormModel.prVrm))
        Seq(certificateDocumentCountOpt, certificateDateOpt, certificateTimeOpt, certificateRegistrationMarkOpt, prVrmOpt)
      }
      case _ => Seq.empty
    }
  }
}

object PaymentModelOptSeq {

  def from(paymentModelOpt: Option[PaymentModel]) = {
    paymentModelOpt match {
      case Some(paymentModel) =>
        val paymentTrxRefOpt = paymentModel.trxRef.map(trxRef => ("paymentTrxRef", trxRef))
        val paymentStatusOpt = paymentModel.paymentStatus.map(paymentStatus => ("paymentStatus", paymentStatus))
        val paymentMaskedPanOpt = paymentModel.maskedPAN.map(maskedPan => ("paymentMaskedPan", maskedPan))
        val paymentAuthCodeOpt = paymentModel.authCode.map(authCode => ("paymentAuthCode", authCode))
        val paymentMerchantIdOpt = paymentModel.merchantId.map(merchantId => ("paymentMerchantId", merchantId))
        val paymentTypeOpt = paymentModel.paymentType.map(paymentType => ("paymentType", paymentType))
        val paymentCardTypeOpt = paymentModel.cardType.map(cardType => ("cardType", cardType))
        val paymentTotalAmountPaidOpt = paymentModel.totalAmountPaid.map(
          totalAmountPaid => ("paymentTotalAmountPaid", totalAmountPaid / 100.0))
        Seq(paymentTrxRefOpt, paymentStatusOpt, paymentMaskedPanOpt, paymentAuthCodeOpt, paymentMerchantIdOpt,
          paymentTypeOpt, paymentCardTypeOpt, paymentTotalAmountPaidOpt)
      case _ => Seq.empty
    }
  }
}

object AuditMessage {

  // service types
  final val AuditServiceType = "PR Assign"

  // page movement names
  final val VehicleLookupToCaptureCertificateDetails = "VehicleLookupToCaptureCertificateDetails"
  final val VehicleLookupToConfirmBusiness = "VehicleLookupToConfirmBusiness"
  final val VehicleLookupToCaptureActor = "VehicleLookupToCaptureActor"
  final val VehicleLookupToVehicleLookupFailure = "VehicleLookupToVehicleLookupFailure"
  final val VehicleLookupToExit = "VehicleLookupToExit"
  final val VehicleLookupToMicroServiceError = "VehicleLookupToMicroServiceError"

  final val CaptureActorToConfirmBusiness = "CaptureActorToConfirmBusiness"
  final val CaptureActorToExit = "CaptureActorToExit"

  final val ConfirmBusinessToCaptureCertificateDetails = "ConfirmBusinessToCaptureCertificateDetails"
  final val ConfirmBusinessToExit = "ConfirmBusinessToExit"

  final val CaptureCertificateDetailsToConfirm = "CaptureCertificateDetailsToConfirm"
  final val CaptureCertificateDetailsToMicroServiceError = "CaptureCertificateDetailsToMicroServiceError"
  final val CaptureCertificateDetailsToExit = "CaptureCertificateDetailsToExit"

  final val ConfirmToPayment = "ConfirmToPayment"
  final val ConfirmToSuccess = "ConfirmToSuccess"
  final val ConfirmToExit = "ConfirmToExit"

  final val PaymentToSuccess = "PaymentToSuccess"
  final val PaymentToPaymentNotAuthorised = "PaymentToPaymentNotAuthorised"
  final val PaymentToPaymentFailure = "PaymentToPaymentFailure"
  final val PaymentToExit = "PaymentToExit"
  final val PaymentToMicroServiceError = "PaymentToMicroServiceError"

  def from(pageMovement: String,
           transactionId: String,
           timestamp: String,
           vehicleAndKeeperDetailsModel: Option[VehicleAndKeeperDetailsModel] = None,
           captureCertificateDetailFormsModel: Option[CaptureCertificateDetailsFormModel] = None,
           keeperEmail: Option[String] = None,
           businessDetailsModel: Option[BusinessDetailsModel] = None,
           paymentModel: Option[PaymentModel] = None,
           rejectionCode: Option[String] = None) = {

    val data: Seq[(String, Any)] = {
      val transactionIdOpt = Some(("transactionId", transactionId))
      val timestampOpt = Some(("timestamp", timestamp))
      val vehicleAndKeeperDetailsModelOptSeq = VehicleAndKeeperDetailsModelOptSeq.from(vehicleAndKeeperDetailsModel)
      val captureCertificateDetailsFormModelOpt = CaptureCertificateDetailsFormModelOptSeq.from(captureCertificateDetailFormsModel)
      val businessDetailsModelOptSeq = BusinessDetailsModelOptSeq.from(businessDetailsModel)
      val keeperEmailOpt = keeperEmail.map(keeperEmail => ("keeperEmail", keeperEmail))
      val paymentModelOptSeq = PaymentModelOptSeq.from(paymentModel)
      val rejectionCodeOpt = rejectionCode.map(rejectionCode => ("rejectionCode", rejectionCode))

      (Seq(
        transactionIdOpt,
        timestampOpt,
        keeperEmailOpt,
        rejectionCodeOpt
      ) ++ vehicleAndKeeperDetailsModelOptSeq ++ businessDetailsModelOptSeq
        ++ captureCertificateDetailsFormModelOpt ++ paymentModelOptSeq).flatten
    }
    AuditMessage(pageMovement, AuditServiceType, data: _*)
  }
}