package com.guardian.circle.data.models;

import android.location.Location;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

public class EmergencyLog {

    private final String eventId;
    private final String triggerType;
    private final String triggerSource;
    private final float peakGForce;
    private final long countdownMs;
    private final long createdAtEpochMs;
    private final Location location;
    private final boolean callAttempted;

    public EmergencyLog(
            String eventId,
            String triggerType,
            String triggerSource,
            float peakGForce,
            long countdownMs,
            long createdAtEpochMs,
            Location location,
            boolean callAttempted
    ) {
        this.eventId = eventId;
        this.triggerType = triggerType;
        this.triggerSource = triggerSource;
        this.peakGForce = peakGForce;
        this.countdownMs = countdownMs;
        this.createdAtEpochMs = createdAtEpochMs;
        this.location = location;
        this.callAttempted = callAttempted;
    }

    public static EmergencyLog create(
            String triggerType,
            String triggerSource,
            float peakGForce,
            long countdownMs,
            Location location,
            boolean callAttempted
    ) {
        long timestamp = System.currentTimeMillis();
        return new EmergencyLog(
                "event-" + timestamp,
                triggerType != null ? triggerType : "unknown",
                triggerSource != null ? triggerSource : "unknown",
                peakGForce,
                countdownMs,
                timestamp,
                location,
                callAttempted
        );
    }

    public String getEventId() {
        return eventId;
    }

    public Location getLocation() {
        return location;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event_id", eventId);
        payload.put("trigger_type", triggerType);
        payload.put("trigger_source", triggerSource);
        payload.put("peak_g_force", peakGForce);
        payload.put("countdown_ms", countdownMs);
        payload.put("created_at_epoch_ms", createdAtEpochMs);
        payload.put("device_model", Build.MODEL);
        payload.put("call_attempted", callAttempted);

        Map<String, Object> locationPayload = new HashMap<>();
        locationPayload.put("latitude", location != null ? location.getLatitude() : null);
        locationPayload.put("longitude", location != null ? location.getLongitude() : null);
        locationPayload.put("accuracy_m", location != null ? location.getAccuracy() : null);
        locationPayload.put("provider", location != null ? location.getProvider() : "unavailable");
        payload.put("location", locationPayload);

        Map<String, Object> dispatchPayload = new HashMap<>();
        dispatchPayload.put("requires_push_fan_out", true);
        dispatchPayload.put("state", "queued");
        dispatchPayload.put("channel", "firebase_firestore_trigger");
        payload.put("dispatch", dispatchPayload);

        return payload;
    }
}
