@Regression
Feature: OCD-2023: Monitor that download files on CHPL download resources page are correct and current
  Verify download files are recent, to ensure file generation didn't fail at any point.

  Background: We need to start with an empty download directory, so that we can be sure we're checking the right file for recency.
    Given I am on download the CHPL resources page on "<env>"
    And the download directory is empty

  Scenario Outline: The download files are "recent"
    Given I am on download the CHPL resources page on "<env>"
    When I download the "<edition>" "<type>" products file
    Then the download file is no more than "<days>" days old
    And the download file is at least "<size>" "<units>" in size
    And the download file has at least "<items>" items
    And the download file has a listing with database id "<id>" and product number "<chplProductNumber>"
    Examples:
      | env  | edition | type | days | size | units | items |  id  |        chplProductNumber          |
      | DEV  |    2011 | xml  |   92 |  148 | MB    |  3885 | 1    |                        CHP-006555 |
      | DEV  |    2014 | xml  |    1 |  450 | MB    |  5190 | 4074 |                        CHP-022844 |
      | DEV  |    2014 | csv  |    1 | 2900 | KB    |  5190 |      |                                   |
      | DEV  |    2015 | xml  |    1 |  150 | MB    |   590 | 9261 | 15.02.02.3007.A056.01.00.0.180214 |
      | DEV  |    2015 | csv  |    1 |  350 | KB    |   590 |      |                                   |
      | STG  |    2011 | xml  |   92 |  148 | MB    |  3885 | 1    |                        CHP-006555 |
      | STG  |    2014 | xml  |    1 |  450 | MB    |  5190 | 4074 |                        CHP-022844 |
      | STG  |    2014 | csv  |    1 | 2900 | KB    |  5190 |      |                                   |
      | STG  |    2015 | xml  |    1 |  150 | MB    |   590 | 9261 | 15.02.02.3007.A056.01.00.0.180214 |
      | STG  |    2015 | csv  |    1 |  350 | KB    |   590 |      |                                   |
      | PROD |    2011 | xml  |   92 |  148 | MB    |  3885 | 1    |                        CHP-006555 |
      | PROD |    2014 | xml  |    1 |  450 | MB    |  5190 | 4074 |                        CHP-022844 |
      | PROD |    2014 | csv  |    1 | 2900 | KB    |  5190 |      |                                   |
      | PROD |    2015 | xml  |    1 |  150 | MB    |   590 | 9261 | 15.02.02.3007.A056.01.00.0.180214 |
      | PROD |    2015 | csv  |    1 |  350 | KB    |   590 |      |                                   |

  Scenario: At any given time, when downloaded, Surveillance Activity file is up-to-date
    When I download the Surveillance Activity file
    Then the downloaded file shows surveillance-all.csv filename

  Scenario: At any given time, when downloaded, Non-Conformities file is up-to-date
    When I download the Non-Conformities file
    Then the downloaded file shows surveillance-with-nonconformities.csv filename
