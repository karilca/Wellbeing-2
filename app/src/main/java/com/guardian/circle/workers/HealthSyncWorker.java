package com.guardian.circle.workers;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.guardian.circle.data.FirestoreRepository;
import com.guardian.circle.data.models.LiveStats;

import java.util.concurrent.TimeUnit;

public class HealthSyncWorker extends Worker {

    public static final String UNIQUE_WORK_NAME = "guardian-health-sync";

    public HealthSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    public static void schedulePeriodic(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                HealthSyncWorker.class,
                15,
                TimeUnit.MINUTES
        ).setConstraints(constraints).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    @NonNull
    @Override
    public Result doWork() {
        FirestoreRepository repository = new FirestoreRepository(getApplicationContext());
        if (!repository.isFirebaseReady()) {
            return Result.success();
        }

        Location location = FirestoreRepository.resolveBestEffortLocation(getApplicationContext());
        LiveStats liveStats = LiveStats.fromDevice(getApplicationContext(), location, false, null);
        return repository.updateLiveStats(liveStats) ? Result.success() : Result.retry();
    }
}
