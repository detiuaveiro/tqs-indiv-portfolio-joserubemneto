Feature: Staff Dashboard Management
  As a staff member
  I want to manage service requests
  So that I can efficiently process citizen requests

  Background:
    Given the application is running

  @smoke
  Scenario: Staff member views the dashboard
    Given I am on the staff dashboard
    Then I should see the staff dashboard
    And I should see statistics cards
    And I should see a list of service requests

  @critical
  Scenario: Staff member updates request status
    Given I am on the staff dashboard
    And there are 1 service requests in the system
    When I click the "Update Status" button on the first request
    Then I should see the update status modal
    And the modal should show current status
    And the modal should have a status dropdown
    And the modal should have a notes field

  @critical
  Scenario: Staff member changes request status to ASSIGNED
    Given I am on the staff dashboard
    When I click the "Update Status" button on the first request
    And I select new status "ASSIGNED"
    And I enter notes "Assigned to collection team Alpha"
    And I click the update button in the modal
    Then the modal should close
    And I should see a success notification
    And the request status should be updated to "ASSIGNED"

  Scenario: Staff member filters requests by municipality
    Given I am on the staff dashboard
    When I filter requests by municipality "Lisboa"
    Then I should see only requests from "Lisboa"

  Scenario: Staff member filters requests by status
    Given I am on the staff dashboard
    When I filter requests by status "RECEIVED"
    Then I should see only requests with status "RECEIVED"

  Scenario: Staff member applies multiple filters
    Given I am on the staff dashboard
    When I filter requests by municipality "Porto"
    And I filter requests by status "ASSIGNED"
    Then I should see only requests from "Porto"
    And I should see only requests with status "ASSIGNED"

  @workflow
  Scenario: Complete workflow - from RECEIVED to COMPLETED
    Given I am on the staff dashboard
    When I click the "Update Status" button on the first request
    And I select new status "ASSIGNED"
    And I enter notes "Assigned to team"
    And I click the update button in the modal
    And I click the "Update Status" button on the first request
    And I select new status "IN_PROGRESS"
    And I enter notes "Team started collection"
    And I click the update button in the modal
    And I click the "Update Status" button on the first request
    And I select new status "COMPLETED"
    And I enter notes "Collection completed successfully"
    And I click the update button in the modal
    Then the request status should be updated to "COMPLETED"

  Scenario: Staff member views dashboard statistics
    Given I am on the staff dashboard
    When I view the dashboard statistics
    Then I should see statistics cards

  Scenario Outline: Staff updates requests with different statuses
    Given I am on the staff dashboard
    When I click the "Update Status" button on the first request
    And I select new status "<newStatus>"
    And I enter notes "<notes>"
    And I click the update button in the modal
    Then the request status should be updated to "<newStatus>"

    Examples:
      | newStatus   | notes                              |
      | ASSIGNED    | Assigned to collection team        |
      | IN_PROGRESS | Collection team en route           |
      | COMPLETED   | Items collected successfully       |

  @validation
  Scenario: Staff member updates status without notes
    Given I am on the staff dashboard
    When I click the "Update Status" button on the first request
    And I select new status "ASSIGNED"
    And I click the update button in the modal
    Then the modal should close
    And the request status should be updated to "ASSIGNED"

  Scenario: Staff member refreshes dashboard
    Given I am on the staff dashboard
    When I refresh the page
    Then I should see the staff dashboard
    And I should see a list of service requests

