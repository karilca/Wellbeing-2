package com.guardian.circle.data.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfile {

    private final String fullName;
    private final String bloodType;
    private final List<String> allergies;
    private final List<String> medicalHistory;
    private final List<String> currentMedications;
    private final String primaryContactName;
    private final String primaryContactPhone;

    public UserProfile(
            String fullName,
            String bloodType,
            List<String> allergies,
            List<String> medicalHistory,
            List<String> currentMedications,
            String primaryContactName,
            String primaryContactPhone
    ) {
        this.fullName = fullName;
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.medicalHistory = medicalHistory;
        this.currentMedications = currentMedications;
        this.primaryContactName = primaryContactName;
        this.primaryContactPhone = primaryContactPhone;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("full_name", fullName);
        payload.put("blood_type", bloodType);
        payload.put("allergies", allergies);
        payload.put("medical_history", medicalHistory);
        payload.put("current_medications", currentMedications);

        Map<String, Object> primaryContact = new HashMap<>();
        primaryContact.put("name", primaryContactName);
        primaryContact.put("phone", primaryContactPhone);
        payload.put("primary_contact", primaryContact);
        return payload;
    }
}
