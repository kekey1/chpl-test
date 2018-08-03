@Regression
Feature: OCD-2023: Monitor that download files on CHPL download resources page are correct and current
  Verify download files are recent, to ensure file generation didn't fail at any point.

  Background: We need to start with an empty download directory, so that we can be sure we're checking the right file for recency.
    Given I am on download the CHPL resources page on "<env>"
    And the download directory is empty

  Scenario Outline: The download files are "recent"
    Given I am on download the CHPL resources page on "<env>"  
    When I download the "<edition>" "<type>" products file
    Then the downloaded file is no more than "<days>" days old
    Examples:
      |env| edition | type | days |
      |DEV|    2011 | xml  |   92 |
      |DEV|    2014 | xml  |    1 |
      |DEV|    2014 | csv  |    1 |
      |DEV|    2015 | xml  |    1 |
      |DEV|    2015 | csv  |    1 |
      |STG|    2011 | xml  |   92 |
      |STG|    2014 | xml  |    1 |
      |STG|    2014 | csv  |    1 |
      |STG|    2015 | xml  |    1 |
      |STG|    2015 | csv  |    1 |
      |PROD|   2011 | xml  |   92 |
      |PROD|   2014 | xml  |    1 |
      |PROD|   2014 | csv  |    1 |
      |PROD|   2015 | xml  |    1 |
      |PROD|   2015 | csv  |    1 |
       
  Scenario: At any given time, when downloaded, Surveillance Activity file is up-to-date
    When I download the Surveillance Activity file
    Then the downloaded file shows surveillance-all.csv filename

  Scenario: At any given time, when downloaded, Non-Conformities file is up-to-date
    When I download the Non-Conformities file
    Then the downloaded file shows surveillance-with-nonconformities.csv filename
