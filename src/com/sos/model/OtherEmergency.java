package com.sos.model;

public class OtherEmergency extends Emergency {
    public OtherEmergency() { super("OTHER", "Other"); }
    @Override public int getSeverity() { return 2; }
    @Override public String getAdvice() {
        return "Stay calm, stay in a safe place and wait for your contacts to respond.";
    }
    @Override public String getContactInstruction() {
        return "Please call this person as soon as possible.";
    }
}
