#Author: alokbarmanqa@gmail.com
Feature: Login Page Features

  @regression @scenario1
  @dataFile:env/${env}/data/loginData.json
  Scenario: LoginPage: Successful login using Json Plain Data
    Given I launch the application
    When user login with "${username}" and "${password}"
    Then I should see the dashboard
    Then user verifies page header as Dashboard

  @regression @smoke @scenario2
  @dataFile:env/${env}/data/loginDataMap.json
  Scenario: LoginPage: Successful login using Json Map Data
    Given I launch the application
    When user login with "${loginDataAdmin}" into application
    Then I should see the dashboard

  @regression @sanity @scenario3
  Scenario: LoginPage: Successful login using hardcoded data
    Given I launch the application
    When user login with "Admin" and "admin123"
    Then I should see the dashboard
    