package com.guardian.circle.workers;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.guardian.circle.data.FirestoreRepository;
import com.guardian.circle.data.models.EmergencyLog;

public class EmergencySyncWorker extends Worker {

    public static final String UNIQUE_WORK_NAME = "guardian-emergency-sync";

    public static final String INPUT_TRIGGER_TYPE = "trigger_type";
    public static final String INPUT_TRIGGER_SOURCE = "trigger_source";
    public static final String INPUT_PEAK_G_FORCE = "peak_g_force";
    public static final String INPUT_COUNTDOWN_MS = "countdown_ms";
    public static final String INPUT_CALL_ATTEMPTED = "call_attempted";

    public static final String TRIGGER_FALL_DETECTED = "fall_detected";
    public static final String TRIGGER_MANUAL_SOS = "manual_sos";
    public static final String SOURCE_ACCELEROMETER = "accelerometer";
    public static final String SOURCE_WATCH_BUTTON = "watch_button";

    public EmergencySyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        FirestoreRepository repository = new FirestoreRepository(getApplicationContext());
        if (!repository.isFirebaseReady()) {
            return Result.success();
        }

        Location location = FirestoreRepository.resolveBestEffortLocation(getApplicationContext());
        EmergencyLog log = EmergencyLog.create(
                getInputData().getString(INPUT_TRIGGER_TYPE),
                getInputData().getString(INPUT_TRIGGER_SOURCE),
                getInputData().getFloat(INPUT_PEAK_G_FORCE, 0.0f),
                getInputData().getLong(INPUT_COUNTDOWN_MS, 0L),
                location,
                getInputData().getBoolean(INPUT_CALL_ATTEMPTED, false)
        );

        return repository.persistEmergencyLog(log) ? Result.success() : Result.retry();
    }
}
