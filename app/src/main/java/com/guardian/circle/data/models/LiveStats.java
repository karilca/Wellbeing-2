package com.guardian.circle.data.models;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;

import com.guardian.circle.data.FirestoreRepository;

import java.util.HashMap;
import java.util.Map;

public class LiveStats {

    private final Location location;
    private final float heartRateBpm;
    private final int bloodPressureSystolic;
    private final int bloodPressureDiastolic;
    private final int stepsToday;
    private final int batteryPercent;
    private final boolean emergencyActive;
    private final String lastEmergencyId;
    private final String deviceModel;
    private final long updatedAtEpochMs;

    public LiveStats(
            Location location,
            float heartRateBpm,
            int bloodPressureSystolic,
            int bloodPressureDiastolic,
            int stepsToday,
            int batteryPercent,
            boolean emergencyActive,
            String lastEmergencyId,
            String deviceModel,
            long updatedAtEpochMs
    ) {
        this.location = location;
        this.heartRateBpm = heartRateBpm;
        this.bloodPressureSystolic = bloodPressureSystolic;
        this.bloodPressureDiastolic = bloodPressureDiastolic;
        this.stepsToday = stepsToday;
        this.batteryPercent = batteryPercent;
        this.emergencyActive = emergencyActive;
        this.lastEmergencyId = lastEmergencyId;
        this.deviceModel = deviceModel;
        this.updatedAtEpochMs = updatedAtEpochMs;
    }

    public static LiveStats fromDevice(Context context, Location location, boolean emergencyActive, String lastEmergencyId) {
        android.content.SharedPreferences preferences =
                context.getSharedPreferences(FirestoreRepository.PREFS_NAME, Context.MODE_PRIVATE);

        float heartRate = preferences.getFloat(FirestoreRepository.PREF_HEART_RATE, -1.0f);
        int systolic = preferences.getInt(FirestoreRepository.PREF_BP_SYSTOLIC, -1);
        int diastolic = preferences.getInt(FirestoreRepository.PREF_BP_DIASTOLIC, -1);
        int stepsToday = preferences.getInt(FirestoreRepository.PREF_STEP_COUNT, 0);

        return new LiveStats(
                location,
                heartRate,
                systolic,
                diastolic,
                stepsToday,
                readBatteryPercent(context),
                emergencyActive,
                lastEmergencyId,
                Build.MODEL,
                System.currentTimeMillis()
        );
    }

    public Map<String, Object> toMap() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("heart_rate_bpm", heartRateBpm);
        payload.put("steps_today", stepsToday);
        payload.put("battery_percent", batteryPercent);
        payload.put("device_model", deviceModel);
        payload.put("updated_at_epoch_ms", updatedAtEpochMs);

        Map<String, Object> bloodPressure = new HashMap<>();
        bloodPressure.put("systolic", bloodPressureSystolic);
        bloodPressure.put("diastolic", bloodPressureDiastolic);
        bloodPressure.put("unit", "mmHg");
        payload.put("blood_pressure", bloodPressure);

        Map<String, Object> gps = new HashMap<>();
        gps.put("latitude", location != null ? location.getLatitude() : null);
        gps.put("longitude", location != null ? location.getLongitude() : null);
        gps.put("accuracy_m", location != null ? location.getAccuracy() : null);
        gps.put("provider", location != null ? location.getProvider() : "unavailable");
        payload.put("gps", gps);

        Map<String, Object> emergency = new HashMap<>();
        emergency.put("active", emergencyActive);
        emergency.put("last_emergency_id", lastEmergencyId);
        payload.put("emergency_state", emergency);

        return payload;
    }

    private static int readBatteryPercent(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) {
            return -1;
        }

        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level < 0 || scale <= 0) {
            return -1;
        }
        return Math.round((level / (float) scale) * 100.0f);
    }
}
