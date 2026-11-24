package com.sos.model;

public class MedicalEmergency extends Emergency {
    public MedicalEmergency() { super("MEDICAL", "Medical Emergency"); }
    @Override public int getSeverity() { return 5; }
    @Override public String getAdvice() {
        return "Stay calm, do not move the patient, call an ambulance and keep the person awake.";
    }
    @Override public String getContactInstruction() {
        return "Please call an ambulance and reach this location as soon as you can.";
    }
}
