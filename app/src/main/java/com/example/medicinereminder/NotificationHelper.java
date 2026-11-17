package com.example.medicinereminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * One place that knows how to create the notification channel
 * and how to build + show the medicine notification.
 */
public class NotificationHelper {

    public static final String CHANNEL_ID = "medicine_reminder_channel";

    // Keys used to pass data from the notification to DetailsActivity
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_DOSAGE = "extra_dosage";
    public static final String EXTRA_INSTRUCTIONS = "extra_instructions";
    public static final String EXTRA_TIME = "extra_time";

    /** Android 8.0+ requires every notification to belong to a channel. */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Medicine Reminders",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Reminders to take your medicine on time");
            channel.enableVibration(true);

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * Builds and shows the notification.
     * @return false if notifications are blocked by the user, true otherwise.
     */
    public static boolean showMedicineNotification(Context context, String name,
                                                   String dosage, String instructions,
                                                   String time) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!manager.areNotificationsEnabled()) {
            return false; // permission denied or notifications turned off
        }

        createChannel(context);

        // A unique id so that two notifications never overwrite each other
        int notificationId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

        // The screen that should open when the notification is tapped
        Intent detailsIntent = new Intent(context, DetailsActivity.class);
        detailsIntent.putExtra(EXTRA_NAME, name);
        detailsIntent.putExtra(EXTRA_DOSAGE, dosage);
        detailsIntent.putExtra(EXTRA_INSTRUCTIONS, instructions);
        detailsIntent.putExtra(EXTRA_TIME, time);

        // TaskStackBuilder makes the Back button go to MainActivity (the parent)
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(detailsIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String bigText = "Time to take your medicine: " + name
                + (dosage.isEmpty() ? "" : " (" + dosage + ")");

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_pill)                 // the icon
                .setContentTitle("Medicine Reminder")             // the title
                .setContentText("Time to take your medicine")     // the message
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)                  // what happens on tap
                .setAutoCancel(true)                              // remove after tap
                .build();

        manager.notify(notificationId, notification);
        return true;
    }
}
