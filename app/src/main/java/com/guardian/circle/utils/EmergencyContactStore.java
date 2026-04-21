package com.guardian.circle.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.guardian.circle.data.FirestoreRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class EmergencyContactStore {

    public enum SaveResult {
        SAVED,
        DUPLICATE,
        INVALID
    }

    private static final String PREF_EMERGENCY_CONTACT_NUMBERS = "emergency_contact_numbers";
    private static final Pattern CROATIAN_PHONE_PATTERN = Pattern.compile("^\\+385\\d{8,9}$");

    private EmergencyContactStore() {
    }

    @NonNull
    public static List<String> getSavedPhoneNumbers(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                FirestoreRepository.PREFS_NAME,
                Context.MODE_PRIVATE
        );

        Set<String> deduplicatedPhoneNumbers = new LinkedHashSet<>();
        String serializedNumbers = preferences.getString(PREF_EMERGENCY_CONTACT_NUMBERS, "");
        if (!TextUtils.isEmpty(serializedNumbers)) {
            String[] chunks = serializedNumbers.split("\n");
            for (String chunk : chunks) {
                String normalizedPhoneNumber = normalizePhoneNumber(chunk);
                if (!TextUtils.isEmpty(normalizedPhoneNumber)) {
                    deduplicatedPhoneNumbers.add(normalizedPhoneNumber);
                }
            }
        }

        if (deduplicatedPhoneNumbers.isEmpty()) {
            String legacyPhoneNumber = normalizePhoneNumber(
                    preferences.getString(FirestoreRepository.PREF_PRIMARY_CONTACT_PHONE, "")
            );
            if (isValidCroatianPhoneNumber(legacyPhoneNumber)) {
                deduplicatedPhoneNumbers.add(legacyPhoneNumber);
            }
        }

        return new ArrayList<>(deduplicatedPhoneNumbers);
    }

    public static SaveResult addPhoneNumber(Context context, String rawPhoneNumber) {
        String normalizedPhoneNumber = normalizePhoneNumber(rawPhoneNumber);
        if (!isValidCroatianPhoneNumber(normalizedPhoneNumber)) {
            return SaveResult.INVALID;
        }

        List<String> savedPhoneNumbers = getSavedPhoneNumbers(context);
        if (savedPhoneNumbers.contains(normalizedPhoneNumber)) {
            return SaveResult.DUPLICATE;
        }

        savedPhoneNumbers.add(normalizedPhoneNumber);
        persistPhoneNumbers(context, savedPhoneNumbers);
        return SaveResult.SAVED;
    }

    public static boolean isValidCroatianPhoneNumber(String rawPhoneNumber) {
        return CROATIAN_PHONE_PATTERN.matcher(normalizePhoneNumber(rawPhoneNumber)).matches();
    }

    @NonNull
    public static String normalizePhoneNumber(String rawPhoneNumber) {
        if (rawPhoneNumber == null) {
            return "";
        }

        return rawPhoneNumber
                .replace(" ", "")
                .replace("-", "")
                .replace("(", "")
                .replace(")", "")
                .trim();
    }

    private static void persistPhoneNumbers(Context context, List<String> phoneNumbers) {
        SharedPreferences preferences = context.getSharedPreferences(
                FirestoreRepository.PREFS_NAME,
                Context.MODE_PRIVATE
        );

        String serializedPhoneNumbers = TextUtils.join("\n", phoneNumbers);
        String firstPhoneNumber = phoneNumbers.isEmpty() ? "" : phoneNumbers.get(0);

        preferences.edit()
                .putString(PREF_EMERGENCY_CONTACT_NUMBERS, serializedPhoneNumbers)
                .putString(FirestoreRepository.PREF_PRIMARY_CONTACT_PHONE, firstPhoneNumber)
                .apply();

        new FirestoreRepository(context).syncEmergencyContacts(phoneNumbers);
    }
}
