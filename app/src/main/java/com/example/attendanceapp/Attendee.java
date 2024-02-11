package com.example.attendanceapp;

public class Attendee {
    private String name;
    private boolean present;

    public Attendee(String name, boolean present) {
        this.name = name;
        this.present = present;
    }

    public String getName() {
        return name;
    }

    public boolean isPresent() {
        return present;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}

