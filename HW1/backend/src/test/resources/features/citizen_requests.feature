# language: en
Feature: Citizen Request Waste Collection
  As a citizen
  I want to request waste collection
  So that my large items can be picked up

  Background:
    Given the application is running
    And I am on the home page

  Scenario: Create a valid waste collection request
    Given I navigate to the create request page
    When I fill in the request form with valid data:
      | field              | value                                    |
      | municipality       | Lisboa                                   |
      | citizenName        | João Silva                               |
      | citizenEmail       | joao.test@example.com                    |
      | citizenPhone       | 912345678                                |
      | pickupAddress      | Rua Test, 123, Lisboa                    |
      | itemDescription    | Old refrigerator and washing machine     |
      | preferredTimeSlot  | MORNING                                  |
    And I select a date 5 days from now
    And I submit the request form
    Then I should see a success message
    And I should receive a unique access token
    And the request status should be "RECEIVED"

  Scenario: Reject request with missing required fields
    Given I navigate to the create request page
    When I submit the request form without filling required fields
    Then I should see validation errors for required fields

  Scenario: Reject request with invalid email
    Given I navigate to the create request page
    When I fill in the request form with an invalid email "invalid-email"
    And I fill in all other required fields
    And I submit the request form
    Then I should see an email validation error

  Scenario: Reject request with invalid phone number
    Given I navigate to the create request page
    When I fill in the request form with an invalid phone "123"
    And I fill in all other required fields
    And I submit the request form
    Then I should see a phone validation error

  Scenario: Check request status with valid token
    Given I have created a request and saved the token
    When I navigate to the check request page
    And I enter the saved token
    And I click search
    Then I should see the request details
    And the status should be displayed
    And I should see the status history

  Scenario: Check request with invalid token
    Given I navigate to the check request page
    When I enter an invalid token "invalid-token-12345"
    And I click search
    Then I should see a "not found" error message

  Scenario: Cancel a pending request
    Given I have created a request and saved the token
    When I navigate to the check request page
    And I enter the saved token
    And I click search
    And I click the cancel button
    And I confirm the cancellation
    Then the request status should be "CANCELLED"
    And the cancel button should not be visible

  Scenario: View all available municipalities
    Given I navigate to the create request page
    When I click on the municipality dropdown
    Then I should see a list of Portuguese municipalities
    And the list should include "Lisboa"
    And the list should include "Porto"

