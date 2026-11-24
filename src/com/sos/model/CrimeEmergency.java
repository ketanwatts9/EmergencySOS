package com.sos.model;

public class CrimeEmergency extends Emergency {
    public CrimeEmergency() { super("CRIME", "Theft / Crime"); }
    @Override public int getSeverity() { return 3; }
    @Override public String getAdvice() {
        return "Go to a safe public place, do not confront the person, note details and contact the police.";
    }
    @Override public String getContactInstruction() {
        return "Please contact the police and try to reach this person.";
    }
}
