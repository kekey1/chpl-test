@Regression
Feature: Verify endpoints return correct response
  OCD-2398- Verify the "basic information" product endpoint reports correct addl s/w code in CHPL ID
  OCD-2557- Verify cache_status endpoint reports status of all cache
  OCD-2696 Re-initialize caches from startup after some action happens that clears them

  Scenario Outline: Certified products basic information endpoint returns correct CHPL ID
    Then the certified_product basic and details endpoints for "<DB_ID>" have the same CHPL ID: "<CHPL_ID>"
    Examples:
      | DB_ID | CHPL_ID                           |
      |  8252 | 15.04.04.2945.Ligh.21.00.1.161229 |

  Scenario: Verify cache_status endpoint returns status of "okay" for "all caches are populated"
    Then the cache_status endpoint returns status "OK" for all caches are populated status
    
  Scenario: Verify cache is re-initialized after an activity
    Given I'm logged in as "ROLE_ADMIN"
    And I navigate to ONC ACB Management page
    And I access "ICSA Labs" ACB details
    And I open the ACB edit form
    When I edit ACB name and save    
    Then the cache_status endpoint returns status "OK" for all caches are populated status
