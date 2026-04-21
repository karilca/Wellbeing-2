package com.guardian.circle.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public final class PermissionHelper {

    private static final int REQUEST_CODE = 4001;

    private PermissionHelper() {
    }

    public static void requestMissingRuntimePermissions(Activity activity) {
        List<String> missingPermissions = new ArrayList<>();

        addIfMissing(activity, missingPermissions, Manifest.permission.CALL_PHONE);
        addIfMissing(activity, missingPermissions, Manifest.permission.ACCESS_FINE_LOCATION);
        addIfMissing(activity, missingPermissions, Manifest.permission.READ_PHONE_STATE);
        addIfMissing(activity, missingPermissions, Manifest.permission.RECORD_AUDIO);
        addIfMissing(activity, missingPermissions, Manifest.permission.BODY_SENSORS);
        addIfMissing(activity, missingPermissions, Manifest.permission.ACTIVITY_RECOGNITION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            addIfMissing(activity, missingPermissions, Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!missingPermissions.isEmpty()) {
            activity.requestPermissions(missingPermissions.toArray(new String[0]), REQUEST_CODE);
        }
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static void addIfMissing(Context context, List<String> missingPermissions, String permission) {
        if (!hasPermission(context, permission)) {
            missingPermissions.add(permission);
        }
    }
}
