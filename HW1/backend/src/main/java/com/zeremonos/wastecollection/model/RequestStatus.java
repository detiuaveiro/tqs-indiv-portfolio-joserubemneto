package com.zeremonos.wastecollection.model;

public enum RequestStatus {
    RECEIVED("Request received and pending assignment"),
    ASSIGNED("Request assigned to collection team"),
    IN_PROGRESS("Collection in progress"),
    COMPLETED("Collection completed successfully"),
    CANCELLED("Request cancelled by user or system");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

