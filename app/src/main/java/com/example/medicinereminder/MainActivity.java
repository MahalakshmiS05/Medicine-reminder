package com.example.medicinereminder;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int ALARM_REQUEST_CODE = 1001;

    private TextInputEditText etName, etDosage, etInstructions, etDelay;

    // Shows the "Allow notifications?" pop-up on Android 13+
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this,
                            "Notifications are blocked. Please allow them in Settings.",
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = findViewById(R.id.etName);
        etDosage = findViewById(R.id.etDosage);
        etInstructions = findViewById(R.id.etInstructions);
        etDelay = findViewById(R.id.etDelay);

        Button btnNotifyNow = findViewById(R.id.btnNotifyNow);
        Button btnSchedule = findViewById(R.id.btnSchedule);
        Button btnCancel = findViewById(R.id.btnCancel);

        NotificationHelper.createChannel(this);
        askNotificationPermission();

        btnNotifyNow.setOnClickListener(v -> notifyNow());
        btnSchedule.setOnClickListener(v -> scheduleReminder());
        btnCancel.setOnClickListener(v -> cancelReminder());
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    // ---------- Option 1: show notification immediately ----------
    private void notifyNow() {
        if (!inputIsValid()) return;

        boolean shown = NotificationHelper.showMedicineNotification(
                this, text(etName), text(etDosage), text(etInstructions), currentTime(0));

        if (!shown) {
            Toast.makeText(this, "Please allow notifications first.", Toast.LENGTH_LONG).show();
            askNotificationPermission();
        }
    }

    // ---------- Option 2: show notification after a set time ----------
    private void scheduleReminder() {
        if (!inputIsValid()) return;

        int delaySeconds;
        try {
            delaySeconds = Integer.parseInt(text(etDelay));
        } catch (NumberFormatException e) {
            delaySeconds = -1;
        }
        if (delaySeconds <= 0) {
            etDelay.setError("Enter a number greater than 0");
            return;
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra(NotificationHelper.EXTRA_NAME, text(etName));
        intent.putExtra(NotificationHelper.EXTRA_DOSAGE, text(etDosage));
        intent.putExtra(NotificationHelper.EXTRA_INSTRUCTIONS, text(etInstructions));
        intent.putExtra(NotificationHelper.EXTRA_TIME, currentTime(delaySeconds));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long triggerAt = System.currentTimeMillis() + delaySeconds * 1000L;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Fallback: not exact, but still works
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }

        Toast.makeText(this, "Reminder set for " + delaySeconds + " second(s) from now",
                Toast.LENGTH_SHORT).show();
    }

    private void cancelReminder() {
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, ALARM_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Toast.makeText(this, "Reminder cancelled", Toast.LENGTH_SHORT).show();
    }

    // ---------- Small helper methods ----------
    private boolean inputIsValid() {
        if (text(etName).isEmpty()) {
            etName.setError("Please enter the medicine name");
            return false;
        }
        return true;
    }

    private String text(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String currentTime(int plusSeconds) {
        long millis = System.currentTimeMillis() + plusSeconds * 1000L;
        return new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(new Date(millis));
    }
}
