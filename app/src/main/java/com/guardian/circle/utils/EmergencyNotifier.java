package com.guardian.circle.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import com.guardian.circle.R;
import com.guardian.circle.workers.EmergencySyncWorker;

public final class EmergencyNotifier {

    private EmergencyNotifier() {
    }

    public static void enqueueEmergencyUpload(
            Context context,
            String triggerType,
            String triggerSource,
            float peakGForce,
            long countdownMs,
            boolean callAttempted
    ) {
        Data inputData = new Data.Builder()
                .putString(EmergencySyncWorker.INPUT_TRIGGER_TYPE, triggerType)
                .putString(EmergencySyncWorker.INPUT_TRIGGER_SOURCE, triggerSource)
                .putFloat(EmergencySyncWorker.INPUT_PEAK_G_FORCE, peakGForce)
                .putLong(EmergencySyncWorker.INPUT_COUNTDOWN_MS, countdownMs)
                .putBoolean(EmergencySyncWorker.INPUT_CALL_ATTEMPTED, callAttempted)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(EmergencySyncWorker.class)
                .setInputData(inputData)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                EmergencySyncWorker.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
        );
    }

    public static void placeEmergencyCall(Context context, String primaryPhone) {
        if (TextUtils.isEmpty(primaryPhone)) {
            Toast.makeText(context, R.string.emergency_call_missing_phone, Toast.LENGTH_LONG).show();
            return;
        }

        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", primaryPhone.trim(), null));
        if (PermissionHelper.hasPermission(context, Manifest.permission.CALL_PHONE)) {
            callIntent = new Intent(Intent.ACTION_CALL, Uri.fromParts("tel", primaryPhone.trim(), null));
        }
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            ContextCompat.startActivity(context, callIntent, null);
        } catch (Exception exception) {
            Toast.makeText(context, R.string.emergency_call_failed, Toast.LENGTH_LONG).show();
        }
    }
}
