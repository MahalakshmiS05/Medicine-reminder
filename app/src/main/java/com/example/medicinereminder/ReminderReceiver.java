package com.example.medicinereminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * AlarmManager sends a broadcast to this class when the reminder time arrives.
 * It simply reads the saved details and shows the notification.
 */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = valueOrEmpty(intent.getStringExtra(NotificationHelper.EXTRA_NAME));
        String dosage = valueOrEmpty(intent.getStringExtra(NotificationHelper.EXTRA_DOSAGE));
        String instructions = valueOrEmpty(intent.getStringExtra(NotificationHelper.EXTRA_INSTRUCTIONS));
        String time = valueOrEmpty(intent.getStringExtra(NotificationHelper.EXTRA_TIME));

        NotificationHelper.showMedicineNotification(context, name, dosage, instructions, time);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
