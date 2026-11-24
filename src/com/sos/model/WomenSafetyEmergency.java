package com.sos.model;

public class WomenSafetyEmergency extends Emergency {
    public WomenSafetyEmergency() { super("WOMEN_SAFETY", "Women Safety"); }
    @Override public int getSeverity() { return 4; }
    @Override public String getAdvice() {
        return "Move to a crowded, well-lit place, share your live location with someone you trust.";
    }
    @Override public String getContactInstruction() {
        return "Please call this person immediately and inform the police if there is no response.";
    }
}
