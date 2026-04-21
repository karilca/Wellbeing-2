package com.guardian.circle.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.guardian.circle.R;
import com.guardian.circle.sensors.FallAnalyzer;
import com.guardian.circle.ui.MainActivity;
import com.guardian.circle.utils.EmergencyContactStore;
import com.guardian.circle.utils.EmergencyNotifier;
import com.guardian.circle.utils.SosSessionManager;
import com.guardian.circle.workers.EmergencySyncWorker;

import java.util.List;

public class FallDetectionService extends Service implements SensorEventListener {

    public static final String ACTION_START_MONITORING = "com.guardian.circle.action.START_MONITORING";
    public static final String ACTION_CANCEL_PENDING_ALERT = "com.guardian.circle.action.CANCEL_PENDING_ALERT";
    public static final String ACTION_MONITORING_STATE_CHANGED = "com.guardian.circle.action.MONITORING_STATE_CHANGED";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_DETAIL = "extra_detail";

    private static final String CHANNEL_MONITORING = "guardian_monitoring";
    private static final String CHANNEL_EMERGENCY = "guardian_emergency";
    private static final int FOREGROUND_NOTIFICATION_ID = 1101;
    private static final int EMERGENCY_NOTIFICATION_ID = 1102;
    private static final long COUNTDOWN_MS = 30_000L;
    private static final long DETECTION_COOLDOWN_MS = 60_000L;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private FallAnalyzer fallAnalyzer;
    private CountDownTimer countdownTimer;

    private boolean monitoring;
    private boolean countdownRunning;
    private float peakGForceDuringEvent;
    private long lastEmergencyAtMs;

    public static Intent createStartIntent(Context context) {
        return new Intent(context, FallDetectionService.class).setAction(ACTION_START_MONITORING);
    }

    public static Intent createCancelIntent(Context context) {
        return new Intent(context, FallDetectionService.class).setAction(ACTION_CANCEL_PENDING_ALERT);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) : null;
        fallAnalyzer = new FallAnalyzer(2.2f, 0.25f, 0.95f, 1_500L, 5_000L);
        createNotificationChannels();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : ACTION_START_MONITORING;

        if (ACTION_CANCEL_PENDING_ALERT.equals(action)) {
            cancelPendingAlarm(
                    getString(R.string.sos_status_cancelled),
                    getString(R.string.sos_status_cancelled_detail)
            );
            return START_STICKY;
        }

        startForeground(
                FOREGROUND_NOTIFICATION_ID,
                buildMonitoringNotification(
                        getString(R.string.monitor_notification_title),
                        getString(R.string.monitor_notification_text),
                        false
                )
        );

        startMonitoringIfPossible();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopMonitoring();
        cancelCountdown();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.values == null || event.values.length < 3) {
            return;
        }

        float gForce = calculateGForce(event.values[0], event.values[1], event.values[2]);
        long now = SystemClock.elapsedRealtime();

        if (SosSessionManager.isActive()) {
            return;
        }

        if (countdownRunning) {
            if (fallAnalyzer.shouldCancelCountdown(gForce)) {
                cancelPendingAlarm(
                        getString(R.string.sos_status_cancelled),
                        getString(R.string.sos_status_cancelled_detail)
                );
            }
            return;
        }

        if (now - lastEmergencyAtMs < DETECTION_COOLDOWN_MS) {
            return;
        }

        FallAnalyzer.AnalysisResult result = fallAnalyzer.evaluate(gForce, now);
        switch (result.getEventType()) {
            case IMPACT_DETECTED:
                peakGForceDuringEvent = result.getPeakGForce();
                publishUiState(
                        getString(R.string.sos_status_impact_detected),
                        getString(R.string.sos_status_impact_detail)
                );
                break;
            case FALL_CONFIRMED:
                peakGForceDuringEvent = result.getPeakGForce();
                startEmergencyCountdown();
                break;
            case IMPACT_DISMISSED:
                peakGForceDuringEvent = 0.0f;
                publishUiState(
                        getString(R.string.sos_status_idle),
                        getString(R.string.sos_status_monitoring_detail)
                );
                updateForegroundNotification(
                        getString(R.string.monitor_notification_title),
                        getString(R.string.monitor_notification_text),
                        false
                );
                break;
            case NONE:
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No-op for the lightweight threshold algorithm.
    }

    private void startMonitoringIfPossible() {
        if (accelerometer == null || sensorManager == null) {
            publishUiState(
                    getString(R.string.sos_status_sensor_unavailable),
                    getString(R.string.sos_status_sensor_unavailable_detail)
            );
            updateForegroundNotification(
                    getString(R.string.sos_status_sensor_unavailable),
                    getString(R.string.sos_status_sensor_unavailable_detail),
                    false
            );
            return;
        }

        if (monitoring) {
            return;
        }

        monitoring = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        publishUiState(
                getString(R.string.sos_status_idle),
                getString(R.string.sos_status_monitoring_detail)
        );
    }

    private void stopMonitoring() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        monitoring = false;
    }

    private void startEmergencyCountdown() {
        if (countdownRunning) {
            return;
        }

        countdownRunning = true;
        updateForegroundNotification(
                getString(R.string.emergency_notification_title),
                getString(R.string.emergency_notification_text),
                true
        );

        countdownTimer = new CountDownTimer(COUNTDOWN_MS, 1_000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
                publishUiState(
                        getString(R.string.sos_status_countdown),
                        getString(R.string.sos_status_countdown_detail, secondsLeft)
                );
                updateForegroundNotification(
                        getString(R.string.emergency_notification_title),
                        getString(R.string.sos_status_countdown_detail, secondsLeft),
                        true
                );
            }

            @Override
            public void onFinish() {
                countdownRunning = false;
                countdownTimer = null;
                dispatchEmergencyToCloud();
            }
        }.start();
    }

    private void dispatchEmergencyToCloud() {
        List<String> emergencyNumbers = EmergencyContactStore.getSavedPhoneNumbers(this);
        boolean hasConfiguredNumbers = !emergencyNumbers.isEmpty();
        SosSessionManager.startSos(this, emergencyNumbers);
        lastEmergencyAtMs = SystemClock.elapsedRealtime();

        EmergencyNotifier.enqueueEmergencyUpload(
                this,
                EmergencySyncWorker.TRIGGER_FALL_DETECTED,
                EmergencySyncWorker.SOURCE_ACCELEROMETER,
                peakGForceDuringEvent,
                COUNTDOWN_MS,
                hasConfiguredNumbers
        );

        String activeSosDetail = buildActiveSosDetail(emergencyNumbers);

        NotificationManagerCompat.from(this).notify(
                EMERGENCY_NOTIFICATION_ID,
                new NotificationCompat.Builder(this, CHANNEL_EMERGENCY)
                        .setSmallIcon(R.drawable.splash_icon)
                        .setContentTitle(getString(R.string.sos_status_manual_active))
                        .setContentText(activeSosDetail)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .build()
        );

        peakGForceDuringEvent = 0.0f;
        fallAnalyzer.reset();

        publishUiState(
                getString(R.string.sos_status_manual_active),
                activeSosDetail
        );
        updateForegroundNotification(
                getString(R.string.sos_status_manual_active),
                activeSosDetail,
                false
        );
    }

    private void cancelPendingAlarm(String status, String detail) {
        cancelCountdown();
        fallAnalyzer.reset();
        peakGForceDuringEvent = 0.0f;
        publishUiState(status, detail);
        updateForegroundNotification(
                getString(R.string.monitor_notification_title),
                getString(R.string.monitor_notification_text),
                false
        );
    }

    private void cancelCountdown() {
        countdownRunning = false;
        if (countdownTimer != null) {
            countdownTimer.cancel();
            countdownTimer = null;
        }
    }

    private void publishUiState(String status, String detail) {
        Intent broadcast = new Intent(ACTION_MONITORING_STATE_CHANGED);
        broadcast.putExtra(EXTRA_STATUS, status);
        broadcast.putExtra(EXTRA_DETAIL, detail);
        sendBroadcast(broadcast);
    }

    private void updateForegroundNotification(String title, String text, boolean emergencyMode) {
        NotificationManagerCompat.from(this).notify(
                FOREGROUND_NOTIFICATION_ID,
                buildMonitoringNotification(title, text, emergencyMode)
        );
    }

    private NotificationCompat.Builder createBaseNotificationBuilder(String channelId) {
        Intent launchIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                101,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.splash_icon)
                .setContentIntent(contentIntent)
                .setContentTitle(getString(R.string.monitor_notification_title))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(true);
    }

    private Notification buildMonitoringNotification(String title, String text, boolean emergencyMode) {
        String channelId = emergencyMode ? CHANNEL_EMERGENCY : CHANNEL_MONITORING;
        NotificationCompat.Builder builder = createBaseNotificationBuilder(channelId)
                .setContentTitle(title)
                .setContentText(text);

        if (emergencyMode) {
            PendingIntent cancelIntent = PendingIntent.getService(
                    this,
                    202,
                    createCancelIntent(this),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .addAction(0, getString(R.string.cancel_alert), cancelIntent);
        }

        return builder.build();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager == null) {
            return;
        }

        NotificationChannel monitoringChannel = new NotificationChannel(
                CHANNEL_MONITORING,
                CHANNEL_MONITORING,
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationChannel emergencyChannel = new NotificationChannel(
                CHANNEL_EMERGENCY,
                CHANNEL_EMERGENCY,
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(monitoringChannel);
        notificationManager.createNotificationChannel(emergencyChannel);
    }

    private float calculateGForce(float x, float y, float z) {
        float totalAcceleration = (float) Math.sqrt((x * x) + (y * y) + (z * z));
        return totalAcceleration / SensorManager.GRAVITY_EARTH;
    }

    private String buildActiveSosDetail(List<String> emergencyNumbers) {
        String currentPhoneNumber = SosSessionManager.getCurrentPhoneNumber();
        int callWarningResId = SosSessionManager.getCallWarningResId();
        if (callWarningResId != 0) {
            return getString(callWarningResId);
        }

        if (!TextUtils.isEmpty(currentPhoneNumber)) {
            return getString(
                    R.string.sos_status_calling_numbers_with_current,
                    emergencyNumbers.size(),
                    currentPhoneNumber
            );
        }

        if (emergencyNumbers.isEmpty()) {
            return getString(R.string.sos_status_alarm_only);
        }

        return getString(R.string.sos_status_calling_numbers, emergencyNumbers.size());
    }
}
