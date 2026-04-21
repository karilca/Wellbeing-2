package com.guardian.circle.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

import com.guardian.circle.services.FallDetectionService;
import com.guardian.circle.workers.HealthSyncWorker;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)) {
            ContextCompat.startForegroundService(context, FallDetectionService.createStartIntent(context));
            HealthSyncWorker.schedulePeriodic(context);
        }
    }
}
