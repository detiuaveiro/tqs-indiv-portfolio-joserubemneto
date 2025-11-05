package com.zeremonos.wastecollection.model;

public enum TimeSlot {
    MORNING("08:00 - 12:00"),
    AFTERNOON("12:00 - 18:00"),
    EVENING("18:00 - 21:00");

    private final String description;

    TimeSlot(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

