package com.sos.model;

public class AccidentEmergency extends Emergency {
    public AccidentEmergency() { super("ACCIDENT", "Road Accident"); }
    @Override public int getSeverity() { return 4; }
    @Override public String getAdvice() {
        return "Move to a safe place, switch on hazard lights and do not move injured people unless necessary.";
    }
    @Override public String getContactInstruction() {
        return "Please call for help and reach this location safely.";
    }
}
