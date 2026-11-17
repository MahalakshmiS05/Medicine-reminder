package com.example.medicinereminder;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/** The second screen. It opens when the user taps the notification. */
public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Medicine Details");
            actionBar.setDisplayHomeAsUpEnabled(true); // shows the back arrow
        }

        TextView tvName = findViewById(R.id.tvName);
        TextView tvDosage = findViewById(R.id.tvDosage);
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvInstructions = findViewById(R.id.tvInstructions);

        // Read the data that was attached to the notification
        tvName.setText(getExtra(NotificationHelper.EXTRA_NAME, "Not provided"));
        tvDosage.setText(getExtra(NotificationHelper.EXTRA_DOSAGE, "Not provided"));
        tvTime.setText(getExtra(NotificationHelper.EXTRA_TIME, "Not provided"));
        tvInstructions.setText(getExtra(NotificationHelper.EXTRA_INSTRUCTIONS, "Not provided"));
    }

    private String getExtra(String key, String fallback) {
        String value = getIntent().getStringExtra(key);
        return (value == null || value.trim().isEmpty()) ? fallback : value;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
