package com.sos.model;

public class NaturalDisasterEmergency extends Emergency {
    public NaturalDisasterEmergency() { super("NATURAL_DISASTER", "Natural Disaster"); }
    @Override public int getSeverity() { return 5; }
    @Override public String getAdvice() {
        return "Move to open safe ground, stay away from buildings and electric poles, follow official instructions.";
    }
    @Override public String getContactInstruction() {
        return "Please check on this person and inform the local authorities.";
    }
}
