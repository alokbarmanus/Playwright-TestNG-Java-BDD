Feature: Login

  @regression
  @dataFile:env/${env}/data/data.json
  Scenario: Successful login
    Given I launch the application
    #When I enter username "Admin"
    When user login with '${username}' and '${password}'
    Then I should see the dashboard

  @smoke
  @dataFile:env/${env}/data/datamap.json
  Scenario: Successful login
    Given I launch the application
    #When I enter username "Admin"
    When user login with "Admin" and "admin123"
    When user enter address info using '${address}' in the form
    Then I should see the dashboard

  @sanity
  Scenario: Successful login
    Given I launch the application
    When I enter username "Admin"
    Then I should see the dashboard