package com.guardian.circle.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.guardian.circle.R;
import com.guardian.circle.data.FirestoreRepository;
import com.guardian.circle.services.FallDetectionService;
import com.guardian.circle.utils.EmergencyContactStore;
import com.guardian.circle.utils.EmergencyNotifier;
import com.guardian.circle.utils.PermissionHelper;
import com.guardian.circle.utils.SosSessionManager;
import com.guardian.circle.workers.EmergencySyncWorker;
import com.guardian.circle.workers.HealthSyncWorker;

import java.util.List;

public class MainActivity extends Activity {

    private TextView statusTitleTextView;
    private TextView statusDetailTextView;
    private TextView savedPhoneNumbersEmptyTextView;
    private TextView savedPhoneNumbersHintTextView;
    private EditText phoneNumberEditText;
    private LinearLayout savedPhoneNumbersContainer;
    private ScrollView mainScrollView;
    private Button addPhoneButton;
    private Button sosButton;
    private Button stopSosButton;
    private boolean receiverRegistered;

    private final BroadcastReceiver monitoringStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            String action = intent.getAction();
            if (SosSessionManager.ACTION_SOS_STATE_CHANGED.equals(action)) {
                syncSosUiState();
                if (SosSessionManager.isActive()) {
                    renderActiveSosStatusIfNeeded();
                }
                return;
            }

            if (!FallDetectionService.ACTION_MONITORING_STATE_CHANGED.equals(action)) {
                return;
            }

            if (SosSessionManager.isActive()) {
                syncSosUiState();
                renderActiveSosStatusIfNeeded();
                return;
            }

            String status = intent.getStringExtra(FallDetectionService.EXTRA_STATUS);
            String detail = intent.getStringExtra(FallDetectionService.EXTRA_DETAIL);
            if (status != null) {
                statusTitleTextView.setText(status);
            }
            if (detail != null) {
                statusDetailTextView.setText(detail);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTitleTextView = findViewById(R.id.statusTitleTextView);
        statusDetailTextView = findViewById(R.id.statusDetailTextView);
        savedPhoneNumbersContainer = findViewById(R.id.savedPhoneNumbersContainer);
        savedPhoneNumbersEmptyTextView = findViewById(R.id.savedPhoneNumbersEmptyTextView);
        savedPhoneNumbersHintTextView = findViewById(R.id.savedPhoneNumbersHintTextView);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        mainScrollView = findViewById(R.id.mainScrollView);
        addPhoneButton = findViewById(R.id.addPhoneButton);
        sosButton = findViewById(R.id.sosButton);
        stopSosButton = findViewById(R.id.stopSosButton);

        PermissionHelper.requestMissingRuntimePermissions(this);
        startMonitoringService();
        HealthSyncWorker.schedulePeriodic(this);
        renderFirebaseHintIfNeeded();
        renderSavedPhoneNumbers();
        syncSosUiState();

        sosButton.setOnClickListener(view -> triggerManualSos());
        addPhoneButton.setOnClickListener(view -> savePhoneNumber());
        stopSosButton.setOnClickListener(view -> stopActiveSos());
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerMonitoringReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderSavedPhoneNumbers();
        renderActiveSosStatusIfNeeded();
        syncSosUiState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterMonitoringReceiver();
    }

    private void triggerManualSos() {
        List<String> emergencyNumbers = EmergencyContactStore.getSavedPhoneNumbers(this);
        FirestoreRepository repository = new FirestoreRepository(this);
        boolean hasConfiguredNumbers = !emergencyNumbers.isEmpty();

        SosSessionManager.startSos(this, emergencyNumbers);
        renderActiveSosStatusIfNeeded();
        syncSosUiState();

        EmergencyNotifier.enqueueEmergencyUpload(
                this,
                EmergencySyncWorker.TRIGGER_MANUAL_SOS,
                EmergencySyncWorker.SOURCE_WATCH_BUTTON,
                0.0f,
                0L,
                hasConfiguredNumbers
        );

        if (!repository.isFirebaseReady()) {
            Toast.makeText(this, R.string.firebase_sync_unavailable_toast, Toast.LENGTH_LONG).show();
        }
    }

    private void renderFirebaseHintIfNeeded() {
        if (SosSessionManager.isActive()) {
            return;
        }

        FirestoreRepository repository = new FirestoreRepository(this);
        if (!repository.isFirebaseReady()) {
            statusTitleTextView.setText(R.string.sos_status_firebase_missing);
            statusDetailTextView.setText(R.string.sos_status_firebase_missing_detail);
        }
    }

    private void startMonitoringService() {
        Intent serviceIntent = FallDetectionService.createStartIntent(this);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void registerMonitoringReceiver() {
        if (receiverRegistered) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(FallDetectionService.ACTION_MONITORING_STATE_CHANGED);
        filter.addAction(SosSessionManager.ACTION_SOS_STATE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(monitoringStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(monitoringStateReceiver, filter);
        }
        receiverRegistered = true;
    }

    private void unregisterMonitoringReceiver() {
        if (!receiverRegistered) {
            return;
        }
        unregisterReceiver(monitoringStateReceiver);
        receiverRegistered = false;
    }

    private void savePhoneNumber() {
        String rawPhoneNumber = phoneNumberEditText.getText().toString();
        EmergencyContactStore.SaveResult result = EmergencyContactStore.addPhoneNumber(this, rawPhoneNumber);

        if (result == EmergencyContactStore.SaveResult.INVALID) {
            Toast.makeText(this, R.string.phone_validation_error, Toast.LENGTH_LONG).show();
            return;
        }

        if (result == EmergencyContactStore.SaveResult.DUPLICATE) {
            Toast.makeText(this, R.string.phone_duplicate, Toast.LENGTH_SHORT).show();
            return;
        }

        phoneNumberEditText.setText("");
        renderSavedPhoneNumbers();
        statusTitleTextView.setText(R.string.phone_saved_title);
        statusDetailTextView.setText(R.string.phone_saved_detail);
        Toast.makeText(this, R.string.phone_saved_toast, Toast.LENGTH_SHORT).show();
    }

    private void renderSavedPhoneNumbers() {
        List<String> savedPhoneNumbers = EmergencyContactStore.getSavedPhoneNumbers(this);
        savedPhoneNumbersContainer.removeAllViews();

        if (savedPhoneNumbers.isEmpty()) {
            savedPhoneNumbersEmptyTextView.setVisibility(View.VISIBLE);
            savedPhoneNumbersHintTextView.setVisibility(View.GONE);
            return;
        }

        boolean editingEnabled = !SosSessionManager.isActive();
        savedPhoneNumbersEmptyTextView.setVisibility(View.GONE);
        savedPhoneNumbersHintTextView.setVisibility(View.VISIBLE);
        savedPhoneNumbersHintTextView.setText(
                editingEnabled ? R.string.saved_phone_numbers_hint : R.string.saved_phone_numbers_locked_hint
        );

        for (int index = 0; index < savedPhoneNumbers.size(); index++) {
            String savedPhoneNumber = savedPhoneNumbers.get(index);
            TextView phoneNumberView = createPhoneNumberItem(savedPhoneNumber, editingEnabled);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (index > 0) {
                layoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.watch_saved_number_spacing);
            }
            phoneNumberView.setLayoutParams(layoutParams);
            savedPhoneNumbersContainer.addView(phoneNumberView);
        }
    }

    private void renderActiveSosStatusIfNeeded() {
        if (!SosSessionManager.isActive()) {
            return;
        }

        statusTitleTextView.setText(R.string.sos_status_manual_active);

        List<String> dialTargets = SosSessionManager.getDialTargets();
        String currentPhoneNumber = SosSessionManager.getCurrentPhoneNumber();
        int callWarningResId = SosSessionManager.getCallWarningResId();
        if (callWarningResId != 0) {
            statusDetailTextView.setText(callWarningResId);
            return;
        }

        if (!TextUtils.isEmpty(currentPhoneNumber)) {
            statusDetailTextView.setText(
                    getString(R.string.sos_status_calling_numbers_with_current, dialTargets.size(), currentPhoneNumber)
            );
            return;
        }

        if (dialTargets.isEmpty()) {
            statusDetailTextView.setText(R.string.sos_status_alarm_only);
            return;
        }

        statusDetailTextView.setText(getString(R.string.sos_status_calling_numbers, dialTargets.size()));
    }

    private void stopActiveSos() {
        SosSessionManager.stopSos(this);
        syncSosUiState();
        statusTitleTextView.setText(R.string.sos_status_stopped);
        statusDetailTextView.setText(R.string.sos_status_stopped_detail);
    }

    private void syncSosUiState() {
        boolean sosActive = SosSessionManager.isActive();
        boolean shouldRevealStopButton = sosActive && stopSosButton.getVisibility() != View.VISIBLE;
        stopSosButton.setVisibility(sosActive ? View.VISIBLE : View.GONE);
        sosButton.setEnabled(!sosActive);
        addPhoneButton.setEnabled(!sosActive);
        phoneNumberEditText.setEnabled(!sosActive);
        phoneNumberEditText.setAlpha(sosActive ? 0.6f : 1.0f);
        addPhoneButton.setAlpha(sosActive ? 0.6f : 1.0f);
        sosButton.setAlpha(sosActive ? 0.6f : 1.0f);
        renderSavedPhoneNumbers();

        if (shouldRevealStopButton && mainScrollView != null) {
            mainScrollView.post(() -> mainScrollView.smoothScrollTo(0, stopSosButton.getBottom()));
        }
    }

    private TextView createPhoneNumberItem(String phoneNumber, boolean enabled) {
        TextView phoneNumberView = new TextView(this);
        int horizontalPadding = getResources().getDimensionPixelSize(R.dimen.watch_saved_number_padding_horizontal);
        int verticalPadding = getResources().getDimensionPixelSize(R.dimen.watch_saved_number_padding_vertical);

        phoneNumberView.setBackgroundResource(R.drawable.bg_phone_number_item);
        phoneNumberView.setGravity(Gravity.CENTER);
        phoneNumberView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        phoneNumberView.setText(phoneNumber);
        phoneNumberView.setTextColor(ContextCompat.getColor(this, R.color.guardian_text));
        phoneNumberView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        phoneNumberView.setClickable(enabled);
        phoneNumberView.setFocusable(enabled);
        phoneNumberView.setEnabled(enabled);
        phoneNumberView.setAlpha(enabled ? 1.0f : 0.6f);

        phoneNumberView.setOnClickListener(view -> {
            if (!view.isEnabled()) {
                return;
            }
            showRemovePhoneNumberDialog(phoneNumber);
        });
        return phoneNumberView;
    }

    private void showRemovePhoneNumberDialog(String phoneNumber) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.remove_phone_dialog_title)
                .setMessage(getString(R.string.remove_phone_dialog_message, phoneNumber))
                .setPositiveButton(R.string.remove_phone_confirm, (dialogInterface, i) -> removePhoneNumber(phoneNumber))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void removePhoneNumber(String phoneNumber) {
        if (!EmergencyContactStore.removePhoneNumber(this, phoneNumber)) {
            return;
        }

        renderSavedPhoneNumbers();
        statusTitleTextView.setText(R.string.phone_removed_title);
        statusDetailTextView.setText(R.string.phone_removed_detail);
        Toast.makeText(this, R.string.phone_removed_toast, Toast.LENGTH_SHORT).show();
    }
}
