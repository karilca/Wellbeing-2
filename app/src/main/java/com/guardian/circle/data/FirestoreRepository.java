package com.guardian.circle.data;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.guardian.circle.data.models.EmergencyLog;
import com.guardian.circle.data.models.LiveStats;
import com.guardian.circle.utils.PermissionHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FirestoreRepository {

    public static final String PREFS_NAME = "guardian_circle_prefs";
    public static final String PREF_PRIMARY_CONTACT_PHONE = "primary_contact_phone";
    public static final String PREF_STEP_COUNT = "steps_today";
    public static final String PREF_HEART_RATE = "heart_rate_bpm";
    public static final String PREF_BP_SYSTOLIC = "bp_systolic";
    public static final String PREF_BP_DIASTOLIC = "bp_diastolic";

    private static final String PREF_LAST_LAT = "last_latitude";
    private static final String PREF_LAST_LON = "last_longitude";
    private static final String PREF_LAST_ACC = "last_accuracy";
    private static final String PREF_FALLBACK_UID = "fallback_uid";
    private static final String TAG = "FirestoreRepository";

    private final Context appContext;
    private final SharedPreferences preferences;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;
    private final boolean firebaseReady;

    public FirestoreRepository(Context context) {
        appContext = context.getApplicationContext();
        preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        FirebaseApp firebaseApp = null;
        try {
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                firebaseApp = FirebaseApp.initializeApp(appContext);
            } else {
                firebaseApp = FirebaseApp.getInstance();
            }
        } catch (IllegalStateException exception) {
            Log.w(TAG, "Firebase not available yet.", exception);
        }

        if (firebaseApp == null) {
            firestore = null;
            auth = null;
            firebaseReady = false;
        } else {
            firestore = FirebaseFirestore.getInstance(firebaseApp);
            auth = FirebaseAuth.getInstance(firebaseApp);
            firebaseReady = true;
        }
    }

    public boolean isFirebaseReady() {
        return firebaseReady;
    }

    public boolean updateLiveStats(LiveStats liveStats) {
        if (!firebaseReady || firestore == null) {
            return false;
        }

        String uid = resolveUserId();
        if (TextUtils.isEmpty(uid)) {
            return false;
        }

        try {
            DocumentReference userRef = firestore.collection("users").document(uid);
            DocumentReference statsRef = userRef.collection("live_stats").document("current");

            WriteBatch batch = firestore.batch();
            batch.set(userRef, buildUserShell(uid), SetOptions.merge());
            batch.set(statsRef, liveStats.toMap(), SetOptions.merge());
            Tasks.await(batch.commit());
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "Live stats upload failed.", exception);
            return false;
        }
    }

    public boolean persistEmergencyLog(EmergencyLog emergencyLog) {
        if (!firebaseReady || firestore == null) {
            return false;
        }

        String uid = resolveUserId();
        if (TextUtils.isEmpty(uid)) {
            return false;
        }

        try {
            DocumentReference userRef = firestore.collection("users").document(uid);
            DocumentReference emergencyRef = userRef.collection("emergency_logs").document(emergencyLog.getEventId());
            DocumentReference statsRef = userRef.collection("live_stats").document("current");

            LiveStats liveStats = LiveStats.fromDevice(
                    appContext,
                    emergencyLog.getLocation(),
                    true,
                    emergencyLog.getEventId()
            );

            WriteBatch batch = firestore.batch();
            batch.set(userRef, buildUserShell(uid), SetOptions.merge());
            batch.set(emergencyRef, emergencyLog.toMap(), SetOptions.merge());
            batch.set(statsRef, liveStats.toMap(), SetOptions.merge());
            Tasks.await(batch.commit());
            cacheLocation(emergencyLog.getLocation());
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "Emergency log upload failed.", exception);
            return false;
        }
    }

    public boolean syncEmergencyContacts(List<String> phoneNumbers) {
        if (!firebaseReady || firestore == null) {
            return false;
        }

        String uid = resolveUserId();
        if (TextUtils.isEmpty(uid)) {
            return false;
        }

        try {
            DocumentReference userRef = firestore.collection("users").document(uid);
            DocumentReference profileRef = userRef.collection("user_profile").document("main");

            Map<String, Object> payload = new HashMap<>();
            payload.put("emergency_call_numbers", phoneNumbers);
            payload.put("updated_at_epoch_ms", System.currentTimeMillis());

            WriteBatch batch = firestore.batch();
            batch.set(userRef, buildUserShell(uid), SetOptions.merge());
            batch.set(profileRef, payload, SetOptions.merge());
            Tasks.await(batch.commit());
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "Emergency contacts sync failed.", exception);
            return false;
        }
    }

    public void syncWearToken(String token) {
        if (!firebaseReady || firestore == null || TextUtils.isEmpty(token)) {
            return;
        }

        String uid = resolveUserId();
        if (TextUtils.isEmpty(uid)) {
            return;
        }

        Map<String, Object> tokenPayload = new HashMap<>();
        tokenPayload.put("platform", "wearos");
        tokenPayload.put("device_model", android.os.Build.MODEL);
        tokenPayload.put("fcm_token", token);
        tokenPayload.put("updated_at_epoch_ms", System.currentTimeMillis());

        firestore.collection("users")
                .document(uid)
                .collection("devices")
                .document("watch")
                .set(tokenPayload, SetOptions.merge());
    }

    @Nullable
    public static Location resolveBestEffortLocation(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null && PermissionHelper.hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Location gpsLocation = null;
            try {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (gpsLocation == null) {
                    gpsLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            } catch (SecurityException exception) {
                Log.w(TAG, "Location permission missing during fallback lookup.", exception);
            }

            if (gpsLocation != null) {
                preferences.edit()
                        .putString(PREF_LAST_LAT, String.valueOf(gpsLocation.getLatitude()))
                        .putString(PREF_LAST_LON, String.valueOf(gpsLocation.getLongitude()))
                        .putFloat(PREF_LAST_ACC, gpsLocation.getAccuracy())
                        .apply();
                return gpsLocation;
            }
        }

        if (preferences.contains(PREF_LAST_LAT) && preferences.contains(PREF_LAST_LON)) {
            try {
                Location cachedLocation = new Location("cached");
                cachedLocation.setLatitude(Double.parseDouble(preferences.getString(PREF_LAST_LAT, "0")));
                cachedLocation.setLongitude(Double.parseDouble(preferences.getString(PREF_LAST_LON, "0")));
                cachedLocation.setAccuracy(preferences.getFloat(PREF_LAST_ACC, 0.0f));
                return cachedLocation;
            } catch (NumberFormatException exception) {
                Log.w(TAG, "Cached location is malformed.", exception);
            }
        }

        return null;
    }

    private String resolveUserId() {
        FirebaseUser currentUser = auth != null ? auth.getCurrentUser() : null;
        if (currentUser != null) {
            return currentUser.getUid();
        }

        String fallbackUid = preferences.getString(PREF_FALLBACK_UID, "");
        if (TextUtils.isEmpty(fallbackUid)) {
            fallbackUid = "demo-" + UUID.randomUUID();
            preferences.edit().putString(PREF_FALLBACK_UID, fallbackUid).apply();
        }
        return fallbackUid;
    }

    private Map<String, Object> buildUserShell(String uid) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", uid);
        payload.put("device_type", "wear_os");
        payload.put("display_name", "GuardianCircle Wear User");
        payload.put("updated_at_epoch_ms", System.currentTimeMillis());
        return payload;
    }

    private void cacheLocation(@Nullable Location location) {
        if (location == null) {
            return;
        }

        preferences.edit()
                .putString(PREF_LAST_LAT, String.valueOf(location.getLatitude()))
                .putString(PREF_LAST_LON, String.valueOf(location.getLongitude()))
                .putFloat(PREF_LAST_ACC, location.getAccuracy())
                .apply();
    }
}
