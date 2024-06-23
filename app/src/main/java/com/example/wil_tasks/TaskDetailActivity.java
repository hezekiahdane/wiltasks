package com.example.wil_tasks;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TaskDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        TextView dueDateTextView = findViewById(R.id.dueDateTextView);
        TextView assignedUserTextView = findViewById(R.id.assignedUserTextView);
        TextView statusTextView = findViewById(R.id.statusTextView);

        // Get the task details from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            titleTextView.setText(extras.getString("title"));
            descriptionTextView.setText(extras.getString("description"));
            dueDateTextView.setText(extras.getString("dueDate"));
            assignedUserTextView.setText(extras.getString("assignedUser"));
            statusTextView.setText(extras.getString("status"));
        }
    }
}

