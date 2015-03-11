@HappyPath
Feature: Assignment of Vehicle

  Background:
    Given that I have started the PR Assign Service

  Scenario Outline: Keeper Acting and Fees Due
    When I enter data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> for a vehicle that is eligible for retention
    And I indicate that the keeper is acting
    And I enter certificate <CertificateIdBox1>,<CertificateIdBox2>,<CertificateIdBox3>,<CertificateIdBox4> and <RegistrationNumber>
    Then the confirm details page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode  | CertificateIdBox1 | CertificateIdBox2 | CertificateIdBox3 | CertificateIdBox4 | RegistrationNumber |
    | "DD22"                    | "11111111111" | "SA11AA"  | "1"               | "23456"           | "891234"          | "ABC123"          | "ABC123"           |
    | "S11"                     | "11111111111" | "SA1"     | "1"               | "23456"           | "891234"          | "ABC123"          | "ABC123"           |
    | "S13"                     | "11111111111" | ""        | "1"               | "23456"           | "891234"          | "ABC123"          | "ABC123"           |

  Scenario Outline: Invalid Data in Vehicle Registration Number, Doc Ref ID and Postcode
    When I enter invalid data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> fields
    Then the error messages for invalid data in the Vehicle Registration Number, Doc Ref ID and Postcode fields are displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode  |
    | "1XCG456"                 | "abgdrt12345" | "SA000AS" |

  Scenario Outline: Vehicle Not Found
    When I enter data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> that does not match a valid vehicle record
    Then the vrm not found page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "C1"                      | "11111111111" | "SA11AA" |

  Scenario Outline: Doc Ref Mismatch
    When I enter data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> that does not match a valid vehicle record
    Then the doc ref mismatch page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "A1"                      | "22222222222" | "AA11AA" |

  Scenario Outline: Brute Force Lockout
    When I enter data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode>  that does not match a valid vehicle record three times in a row
    Then the brute force lock out page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "ST05YYC"                 | "11111111111" | "SA11AA" |

  Scenario Outline: Direct to Paper Channel
    When I enter data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> for a vehicle that has a marker set
    Then the direct to paper channel page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "D1"                      | "11111111111" | "SA11AA" |

  Scenario Outline: Vehicle not Eligible
    When I enter data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> for a vehicle that is not eligible for retention
    Then the vehicle not eligible page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "E1"                      | "11111111111" | "SA11AA" |

  Scenario Outline:Trader Acting (no details stored)
    When I enter data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> for a vehicle that is eligible for retention and I indicate that the keeper is not acting and I have not previously chosen to store my details
    Then the supply business details page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "ABC1"                    | "11111111111" | "SA11AA" |

  Scenario Outline: Trader Acting (details stored)
    When I enter data in the <VehicleRegistrationNumber>,<DocRefID> and <Postcode> for a vehicle that is eligible for retention and I indicate that the keeper is not acting and I have previously chosen to store my details and the cookie is still fresh less than seven days old)
    Then the confirm business details page is displayed
  Examples:
    | VehicleRegistrationNumber | DocRefID      | Postcode |
    | "ABC1"                    | "11111111111" | "SA11AA" |