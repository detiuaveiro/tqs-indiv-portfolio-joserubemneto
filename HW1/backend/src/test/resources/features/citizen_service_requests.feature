Feature: Citizen Service Requests
  As a citizen
  I want to request waste collection services
  So that I can dispose of large items

  Background:
    Given the application is running

  @smoke
  Scenario: Citizen views the home page
    Given I am on the home page
    Then I should see the home page

  @critical
  Scenario: Citizen creates a new service request successfully
    Given I navigate to the create request page
    When I fill in the service request form with valid data
    And I submit the form
    Then I should see a success message
    And I should see an access token

  @critical
  Scenario: Citizen creates request and retrieves it using token
    Given I navigate to the create request page
    When I fill in the service request form with valid data
    And I submit the form
    And I save the access token
    And I navigate to check request page
    And I enter the saved token
    And I click the search button
    Then I should see the request details
    And the status should be "RECEIVED"
    And I should see the status history

  @critical
  Scenario: Citizen cancels a service request
    Given I navigate to the create request page
    When I fill in the service request form with valid data
    And I submit the form
    And I save the access token
    And I navigate to check request page
    And I enter the saved token
    And I click the search button
    And I click the cancel request button
    And I confirm the cancellation
    Then the status should be "CANCELLED"

  Scenario: Citizen creates request with specific municipality
    Given I navigate to the create request page
    When I fill in the form with municipality "Porto", name "Maria Santos", email "maria@example.com", phone "923456789", address "Rua Porto, 456", description "Large furniture items for disposal", date "7" days ahead, and time slot "AFTERNOON"
    And I submit the form
    Then I should see a success message
    And I should see an access token

  @validation
  Scenario: Citizen tries to create request without required fields
    Given I navigate to the create request page
    When I fill in municipality field with "Lisboa"
    And I leave the "name" field empty
    And I submit the form
    Then I should see a validation error for "name"

  @validation
  Scenario: Citizen tries to create request with invalid email
    Given I navigate to the create request page
    When I fill in the service request form with valid data
    And I enter "invalid-email" in the "email" field
    And I submit the form
    Then I should see a validation error for "email"

  @validation
  Scenario: Citizen tries to create request with invalid phone
    Given I navigate to the create request page
    When I fill in the service request form with valid data
    And I enter "123" in the "phone" field
    And I submit the form
    Then I should see a validation error for "phone"

  Scenario: Citizen checks request with invalid token
    Given I navigate to check request page
    When I enter "invalid-token-123" in the "token" field
    And I click the search button
    Then I should see an error message

  Scenario Outline: Citizen creates requests for different municipalities
    Given I navigate to the create request page
    When I fill in the form with municipality "<municipality>", name "<name>", email "<email>", phone "<phone>", address "<address>", description "<description>", date "5" days ahead, and time slot "<timeSlot>"
    And I submit the form
    Then I should see a success message
    And I save the access token
    And I navigate to check request page
    And I enter the saved token
    And I click the search button
    Then the request should have municipality "<municipality>"
    And the request should have citizen name "<name>"

    Examples:
      | municipality | name           | email              | phone     | address           | description                    | timeSlot  |
      | Lisboa       | João Silva     | joao@example.com   | 912345678 | Rua Lisboa, 123   | Old refrigerator and furniture | MORNING   |
      | Porto        | Maria Santos   | maria@example.com  | 923456789 | Rua Porto, 456    | Washing machine and dryer      | AFTERNOON |
      | Braga        | Pedro Costa    | pedro@example.com  | 934567890 | Rua Braga, 789    | Old mattress and sofa          | EVENING   |

