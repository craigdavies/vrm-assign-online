package views.vrm_assign

import models.CacheKeyPrefix

object SetupBusinessDetails {

  final val BusinessEmailMaxLength = 254
  final val BusinessEmailMinLength = 2
  final val BusinessNameId = "business-name"
  final val BusinessContactId = "contact-name"
  final val BusinessEmailId = "contact-email"
  final val BusinessPostcodeId = "business-postcode"
  final val SetupBusinessDetailsCacheKey = s"${CacheKeyPrefix}setup-business-details"
  final val SubmitId = "submit"
  final val ExitId = "exit"
}