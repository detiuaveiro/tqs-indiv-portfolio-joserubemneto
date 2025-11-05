# language: en
Feature: Staff Manage Service Requests
  As a staff member
  I want to manage service requests
  So that I can process waste collection efficiently

  Background:
    Given the application is running
    And there are existing service requests in the system

  Scenario: View all service requests
    Given I navigate to the staff dashboard
    Then I should see the statistics dashboard
    And I should see a list of service requests
    And each request should display key information

  Scenario: Filter requests by municipality
    Given I navigate to the staff dashboard
    When I select "Lisboa" from the municipality filter
    Then I should only see requests for "Lisboa"
    And the statistics should update accordingly

  Scenario: Filter requests by status
    Given I navigate to the staff dashboard
    When I select "RECEIVED" from the status filter
    Then I should only see requests with status "RECEIVED"

  Scenario: Update request from RECEIVED to ASSIGNED
    Given I navigate to the staff dashboard
    And there is a request with status "RECEIVED"
    When I click update status on that request
    And I select "Assign to Team" as the new status
    And I enter notes "Assigned to Team A - Lisboa area"
    And I confirm the status update
    Then the request status should be updated to "ASSIGNED"
    And the status history should include the new entry

  Scenario: Complete full workflow from RECEIVED to COMPLETED
    Given I navigate to the staff dashboard
    And there is a request with status "RECEIVED"
    When I update the status to "ASSIGNED" with notes "Assigned"
    And I update the status to "IN_PROGRESS" with notes "Collection started"
    And I update the status to "COMPLETED" with notes "Collection finished"
    Then the final status should be "COMPLETED"
    And the status history should show all 4 states

  Scenario: Cannot update completed request
    Given I navigate to the staff dashboard
    And there is a request with status "COMPLETED"
    Then the update status button should not be available

  Scenario: Reject invalid status transition
    Given I navigate to the staff dashboard
    And there is a request with status "RECEIVED"
    When I try to update the status directly to "COMPLETED"
    Then I should not see that option available
    And only valid transitions should be shown

  Scenario: Reopen cancelled request
    Given I navigate to the staff dashboard
    And there is a request with status "CANCELLED"
    When I click update status on that request
    And I select "Reopen Request" as the new status
    And I enter notes "Reopened at citizen request"
    And I confirm the status update
    Then the request status should be updated to "RECEIVED"

  Scenario: View statistics dashboard
    Given I navigate to the staff dashboard
    Then I should see the total requests count
    And I should see counts for each status
    And the counts should be color-coded
    And the counts should match the filtered requests

  Scenario: Apply multiple filters simultaneously
    Given I navigate to the staff dashboard
    When I select "Lisboa" from the municipality filter
    And I select "RECEIVED" from the status filter
    Then I should only see "RECEIVED" requests for "Lisboa"
    And the statistics should reflect the filtered data

