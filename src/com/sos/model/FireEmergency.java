package com.sos.model;

public class FireEmergency extends Emergency {
    public FireEmergency() { super("FIRE", "Fire"); }
    @Override public int getSeverity() { return 5; }
    @Override public String getAdvice() {
        return "Leave the building at once, stay low under smoke, do not use the lift, call the fire brigade.";
    }
    @Override public String getContactInstruction() {
        return "Please call the fire brigade and do not enter the building.";
    }
}
