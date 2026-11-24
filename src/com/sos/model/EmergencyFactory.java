package com.sos.model;

import com.sos.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.List;

/** Creates the right Emergency subclass from a text key. */
public class EmergencyFactory {
    private EmergencyFactory() { }

    public static Emergency create(String key) throws InvalidInputException {
        if (key == null) throw new InvalidInputException("Emergency category is required");
        switch (key.trim().toUpperCase()) {
            case "MEDICAL":          return new MedicalEmergency();
            case "FIRE":             return new FireEmergency();
            case "ACCIDENT":         return new AccidentEmergency();
            case "CRIME":            return new CrimeEmergency();
            case "WOMEN_SAFETY":     return new WomenSafetyEmergency();
            case "NATURAL_DISASTER": return new NaturalDisasterEmergency();
            case "OTHER":            return new OtherEmergency();
            default: throw new InvalidInputException("Invalid emergency category");
        }
    }

    /** One object of every type (used to fill the dropdown in the browser). */
    public static List<Emergency> all() {
        List<Emergency> list = new ArrayList<>();
        list.add(new MedicalEmergency());
        list.add(new FireEmergency());
        list.add(new AccidentEmergency());
        list.add(new CrimeEmergency());
        list.add(new WomenSafetyEmergency());
        list.add(new NaturalDisasterEmergency());
        list.add(new OtherEmergency());
        return list;
    }
}
