Feature: The "Via CEP" API should work as expected

  Scenario Outline: Check valid zip code (CEP)
    Given the user insert a valid zip code like <zip_code>
    When the service is consulted using the zip code
    Then the zip code (CEP), street address, complement, neighborhood, locality, UF and IBGE is returned
    Examples:
      | zip_code  |
      | 91060900  |
      | 91060-900 |

  Scenario Outline: Check nonexistent zip code (CEP)
    Given the user insert a zip code that does not exist at the Post Office database like <zip_code>
    When the service is consulted using the zip code
    Then the correct error message is raised
    Examples:
      | zip_code |
      | 00000000 |
      | 99999999 |

  Scenario Outline: Check zip code (CEP) with invalid format
    Given the user insert a zip code with invalid format like <zip_code>
    When the service is consulted using the zip code
    Then the API raises an HTTP error code 400
    Examples:
      | zip_code  |
      | 910609001 |
      | 9106090   |
      | 9106090A  |
      | 91060_900 |
      | 910-60900 |
      | 91060 900 |

  Scenario Outline: Check zip code (CEP) given address
    Given the user insert the UF as <uf>, the city as <city>, and the street address as <street_address>
    When the service is consulted using the address
    Then a list of zip codes that matches the parameters are returned
    Examples:
      | uf | city     | street_address |
      | RS | Gravatai | Barroso        |
      | BA | Salvador | Mesquita       |
      | SP | Santos   | Pinto          |



