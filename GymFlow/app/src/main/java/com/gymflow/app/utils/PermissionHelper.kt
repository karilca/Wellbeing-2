package com.gymflow.app.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionHelper {

    val CAMERA_PERMISSION = Manifest.permission.CAMERA
    val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    val BLUETOOTH_CONNECT_PERMISSION = Manifest.permission.BLUETOOTH_CONNECT
    val BLUETOOTH_SCAN_PERMISSION = Manifest.permission.BLUETOOTH_SCAN

    val REQUIRED_PERMISSIONS = arrayOf(
        CAMERA_PERMISSION,
        RECORD_AUDIO_PERMISSION
    )

    val BLUETOOTH_PERMISSIONS = arrayOf(
        BLUETOOTH_CONNECT_PERMISSION,
        BLUETOOTH_SCAN_PERMISSION
    )

    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCameraPermission(context: Context): Boolean {
        return hasPermission(context, CAMERA_PERMISSION)
    }

    fun hasAudioPermission(context: Context): Boolean {
        return hasPermission(context, RECORD_AUDIO_PERMISSION)
    }

    fun hasBluetoothPermissions(context: Context): Boolean {
        return BLUETOOTH_PERMISSIONS.all { permission ->
            hasPermission(context, permission)
        }
    }

    fun hasAllRequiredPermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            hasPermission(context, permission)
        }
    }

    fun getMissingPermissions(context: Context): List<String> {
        return REQUIRED_PERMISSIONS.filter { permission ->
            !hasPermission(context, permission)
        }
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            CAMERA_PERMISSION -> "Kamera je potrebna za analizu forme vjezbi"
            RECORD_AUDIO_PERMISSION -> "Mikrofon je potreban za glasovne naredbe"
            BLUETOOTH_CONNECT_PERMISSION -> "Bluetooth je potreban za komunitetske izazove"
            BLUETOOTH_SCAN_PERMISSION -> "Bluetooth skeniranje je potrebno za pronalazak uredaja"
            else -> "Ova dozvola je potrebna za rad aplikacije"
        }
    }
}