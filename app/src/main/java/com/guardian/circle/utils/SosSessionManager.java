package com.guardian.circle.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.guardian.circle.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SosSessionManager {

    public static final String ACTION_SOS_STATE_CHANGED = "com.guardian.circle.action.SOS_STATE_CHANGED";

    private static final Object LOCK = new Object();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final long CALL_CONNECTION_TIMEOUT_MS = 5_000L;
    private static final long NEXT_CALL_DELAY_MS = 1_500L;

    private static MediaPlayer mediaPlayer;
    private static AudioManager audioManager;
    private static Context appContext;
    private static TelephonyManager telephonyManager;
    private static TelephonyCallback callStateCallback;
    private static PhoneStateListener legacyCallStateListener;
    private static Runnable connectionTimeoutRunnable;
    private static List<String> dialTargets = new ArrayList<>();
    private static int nextDialIndex;
    private static int previousAlarmVolume = -1;
    private static boolean active;
    private static boolean awaitingCallConnection;
    private static boolean callInProgress;
    private static int callWarningResId;
    private static String currentPhoneNumber = "";

    private SosSessionManager() {
    }

    public static void startSos(Context context, List<String> phoneNumbers) {
        synchronized (LOCK) {
            Context applicationContext = context.getApplicationContext();
            stopSos(applicationContext);

            appContext = applicationContext;
            dialTargets = new ArrayList<>(phoneNumbers);
            nextDialIndex = 0;
            active = true;
            currentPhoneNumber = "";
            awaitingCallConnection = false;
            callInProgress = false;
            callWarningResId = 0;

            forceAlarmVolumeToMax(applicationContext);
            startAlarmPlayback(applicationContext);
            registerCallStateListener(applicationContext);

            if (!dialTargets.isEmpty()) {
                dialNextPhoneNumberLocked();
            } else {
                broadcastStateLocked();
            }
        }
    }

    public static void stopSos(Context context) {
        synchronized (LOCK) {
            active = false;
            awaitingCallConnection = false;
            callInProgress = false;
            currentPhoneNumber = "";
            callWarningResId = 0;
            nextDialIndex = 0;
            dialTargets = new ArrayList<>();
            cancelConnectionTimeoutLocked();
            unregisterCallStateListenerLocked();
            stopAlarmPlaybackLocked();
            restoreAlarmVolumeLocked();
            appContext = context.getApplicationContext();
            broadcastStateLocked();
        }
    }

    public static boolean isActive() {
        synchronized (LOCK) {
            return active;
        }
    }

    @NonNull
    public static List<String> getDialTargets() {
        synchronized (LOCK) {
            return new ArrayList<>(dialTargets);
        }
    }

    @NonNull
    public static String getCurrentPhoneNumber() {
        synchronized (LOCK) {
            return currentPhoneNumber;
        }
    }

    @StringRes
    public static int getCallWarningResId() {
        synchronized (LOCK) {
            return callWarningResId;
        }
    }

    private static void dialNextPhoneNumberLocked() {
        if (!active || appContext == null || nextDialIndex >= dialTargets.size()) {
            currentPhoneNumber = "";
            broadcastStateLocked();
            return;
        }

        String phoneNumber = dialTargets.get(nextDialIndex++);
        currentPhoneNumber = phoneNumber;
        broadcastStateLocked();

        boolean canObserveCallState =
                telephonyManager != null
                        && PermissionHelper.hasPermission(appContext, Manifest.permission.READ_PHONE_STATE)
                        && PermissionHelper.hasPermission(appContext, Manifest.permission.CALL_PHONE);

        awaitingCallConnection = canObserveCallState;
        callInProgress = false;
        if (canObserveCallState) {
            scheduleConnectionTimeoutLocked();
        }

        CallStartResult callStartResult = startCallLocked(phoneNumber);
        if (callStartResult == CallStartResult.UNSUPPORTED_ENVIRONMENT) {
            awaitingCallConnection = false;
            callInProgress = false;
            currentPhoneNumber = "";
            broadcastStateLocked();
            return;
        }

        if (callStartResult == CallStartResult.RETRYABLE_FAILURE) {
            awaitingCallConnection = false;
            callInProgress = false;
            currentPhoneNumber = "";
            broadcastStateLocked();
            queueDialRetryLocked();
        }
    }

    private static Intent buildCallIntent(Context context, String phoneNumber) {
        Uri phoneUri = Uri.fromParts("tel", phoneNumber, null);
        Intent callIntent = new Intent(Intent.ACTION_DIAL, phoneUri);
        if (PermissionHelper.hasPermission(context, Manifest.permission.CALL_PHONE)) {
            callIntent = new Intent(Intent.ACTION_CALL, phoneUri);
        }
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return callIntent;
    }

    private static CallStartResult startCallLocked(String phoneNumber) {
        if (appContext == null) {
            callWarningResId = R.string.sos_status_call_unavailable;
            return CallStartResult.UNSUPPORTED_ENVIRONMENT;
        }

        if (isProbablyEmulator()) {
            callWarningResId = R.string.sos_status_emulator_call_unavailable;
            return CallStartResult.UNSUPPORTED_ENVIRONMENT;
        }

        Uri phoneUri = Uri.fromParts("tel", phoneNumber, null);
        if (PermissionHelper.hasPermission(appContext, Manifest.permission.CALL_PHONE)) {
            TelecomManager telecomManager = appContext.getSystemService(TelecomManager.class);
            if (telecomManager != null) {
                try {
                    telecomManager.placeCall(phoneUri, Bundle.EMPTY);
                    callWarningResId = 0;
                    return CallStartResult.STARTED;
                } catch (SecurityException | IllegalStateException | UnsupportedOperationException ignored) {
                    // Fall back to the call activity when the watch cannot place the call directly.
                }
            }
        }

        Intent callIntent = buildCallIntent(appContext, phoneNumber);
        if (callIntent.resolveActivity(appContext.getPackageManager()) == null) {
            callWarningResId = R.string.sos_status_call_unavailable;
            return CallStartResult.UNSUPPORTED_ENVIRONMENT;
        }

        try {
            ContextCompat.startActivity(appContext, callIntent, null);
            callWarningResId = 0;
            return CallStartResult.STARTED;
        } catch (Exception exception) {
            callWarningResId = R.string.sos_status_call_unavailable;
            return CallStartResult.UNSUPPORTED_ENVIRONMENT;
        }
    }

    private static void queueDialRetryLocked() {
        if (!active || appContext == null || nextDialIndex >= dialTargets.size()) {
            return;
        }

        MAIN_HANDLER.postDelayed(() -> {
            synchronized (LOCK) {
                if (!active) {
                    return;
                }
                dialNextPhoneNumberLocked();
            }
        }, NEXT_CALL_DELAY_MS);
    }

    private static void scheduleConnectionTimeoutLocked() {
        cancelConnectionTimeoutLocked();
        if (appContext == null) {
            return;
        }

        connectionTimeoutRunnable = () -> {
            synchronized (LOCK) {
                if (!active || !awaitingCallConnection || callInProgress) {
                    return;
                }

                awaitingCallConnection = false;
                currentPhoneNumber = "";
                dialNextPhoneNumberLocked();
            }
        };
        MAIN_HANDLER.postDelayed(connectionTimeoutRunnable, CALL_CONNECTION_TIMEOUT_MS);
    }

    private static void cancelConnectionTimeoutLocked() {
        if (connectionTimeoutRunnable != null) {
            MAIN_HANDLER.removeCallbacks(connectionTimeoutRunnable);
            connectionTimeoutRunnable = null;
        }
    }

    private static void registerCallStateListener(Context context) {
        if (!PermissionHelper.hasPermission(context, Manifest.permission.READ_PHONE_STATE)) {
            telephonyManager = null;
            return;
        }

        telephonyManager = context.getSystemService(TelephonyManager.class);
        if (telephonyManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            callStateCallback = new SosCallStateCallback();
            telephonyManager.registerTelephonyCallback(context.getMainExecutor(), callStateCallback);
        } else {
            legacyCallStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    handleCallStateChanged(state);
                }
            };
            telephonyManager.listen(legacyCallStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    private static void unregisterCallStateListenerLocked() {
        if (telephonyManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && callStateCallback != null) {
            telephonyManager.unregisterTelephonyCallback(callStateCallback);
            callStateCallback = null;
        }

        if (legacyCallStateListener != null) {
            telephonyManager.listen(legacyCallStateListener, PhoneStateListener.LISTEN_NONE);
            legacyCallStateListener = null;
        }

        telephonyManager = null;
    }

    private static void handleCallStateChanged(int state) {
        synchronized (LOCK) {
            if (!active) {
                return;
            }

            if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                awaitingCallConnection = false;
                callInProgress = true;
                callWarningResId = 0;
                cancelConnectionTimeoutLocked();
                broadcastStateLocked();
                return;
            }

            if (state == TelephonyManager.CALL_STATE_IDLE && callInProgress) {
                callInProgress = false;
                awaitingCallConnection = false;
                currentPhoneNumber = "";
                cancelConnectionTimeoutLocked();
                broadcastStateLocked();
                queueDialRetryLocked();
            }
        }
    }

    private static void broadcastStateLocked() {
        if (appContext == null) {
            return;
        }

        appContext.sendBroadcast(new Intent(ACTION_SOS_STATE_CHANGED));
    }

    private static boolean isProbablyEmulator() {
        String fingerprint = Build.FINGERPRINT != null ? Build.FINGERPRINT.toLowerCase(Locale.US) : "";
        String model = Build.MODEL != null ? Build.MODEL.toLowerCase(Locale.US) : "";
        String manufacturer = Build.MANUFACTURER != null ? Build.MANUFACTURER.toLowerCase(Locale.US) : "";
        String brand = Build.BRAND != null ? Build.BRAND.toLowerCase(Locale.US) : "";
        String device = Build.DEVICE != null ? Build.DEVICE.toLowerCase(Locale.US) : "";
        String product = Build.PRODUCT != null ? Build.PRODUCT.toLowerCase(Locale.US) : "";
        String hardware = Build.HARDWARE != null ? Build.HARDWARE.toLowerCase(Locale.US) : "";

        return fingerprint.startsWith("generic")
                || fingerprint.startsWith("unknown")
                || model.contains("emulator")
                || model.contains("android sdk built for")
                || manufacturer.contains("genymotion")
                || (brand.startsWith("generic") && device.startsWith("generic"))
                || product.contains("sdk")
                || hardware.contains("goldfish")
                || hardware.contains("ranchu");
    }

    private enum CallStartResult {
        STARTED,
        RETRYABLE_FAILURE,
        UNSUPPORTED_ENVIRONMENT
    }

    private static void startAlarmPlayback(Context context) {
        stopAlarmPlaybackLocked();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
            );
            mediaPlayer.setDataSource(
                    context,
                    Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alarm)
            );
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception exception) {
            stopAlarmPlaybackLocked();
        }
    }

    private static void stopAlarmPlaybackLocked() {
        if (mediaPlayer == null) {
            return;
        }

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        } catch (IllegalStateException ignored) {
            // The player might already be released if SOS is stopped during state changes.
        }

        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    private static void forceAlarmVolumeToMax(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return;
        }

        previousAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        int maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarmVolume, 0);
    }

    private static void restoreAlarmVolumeLocked() {
        if (audioManager == null || previousAlarmVolume < 0) {
            return;
        }

        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previousAlarmVolume, 0);
        previousAlarmVolume = -1;
    }

    private static final class SosCallStateCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        @Override
        public void onCallStateChanged(int state) {
            handleCallStateChanged(state);
        }
    }
}
