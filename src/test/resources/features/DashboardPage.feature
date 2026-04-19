#Author: alokbarmanqa@gmail.com
Feature: Dashboard Page Features

  @regression @dashboardPage1
  @dataFile:env/${env}/data/loginData.json
  Scenario: DashboardPage: Verify dashboard page header
    Given I launch the application
    When user login with "${username}" and "${password}"
    Then I should see the dashboard
    Then user verifies page header as Dashboard

  @regression @dashboardPage2
  @dataFile:env/${env}/data/loginData.json
  Scenario: DashboardPage: Verify Side Panel Options in dashboard page
    Given I launch the application
    When user login with "${username}" and "${password}"
    Then user verifies page header as Dashboard
    Then user verifies OrangeHRM image is display in left side panel in dashboard page
    Then user verifies Search box is display in left side panel in dashboard page
    Then user verifies side panel options in dashboard page as below
      | Admin  |
      | PIM  |
      | Leave  |
      | Time  |
      | Recruitment |
      | My Info  |
      | Performance  |
      | Dashboard  |
      | Directory  |
      | Maintenance  |
      | Buzz  |
