Feature: CSV Import
  In order to analyze data
  As a user should be able to import data
  So that answers can be deducted.

  Scenario: Missing file
    Given A file "abc.csv"
    And the file does not exist
    When I load the file
    Then I get an error message
    And the error is "File not found."

# TODO is this a valid scenario?
#  Scenario: Existing Non-CSV
#    Given A file "ExistingNonCsv.csv"
#    And the file exists
#    And the file is an invalid csv
#    When I load the file
#    Then I get an error message
#    And the error is "Not a valid format.".

  Scenario: Existing CSV file
    Given A file "doesExistAndIsCSV.csv"
    And the file exists
    And the file is a valid CSV
    When I load the file
    Then the application populates the database
    And I get a success message, "File successfully imported."
    And the application returns me to the main menu.

