package com.example.wil_tasks;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.titleTextView.setText(task.getTitle());
        holder.dueDateTextView.setText(task.getDueDate());
        holder.assignedUserTextView.setText(String.join(", ", task.getAssignedUsers()));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(holder.itemView.getContext(),
                R.array.task_status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(adapter);

        // Set current status
        int statusPosition = adapter.getPosition(task.getStatus());
        holder.statusSpinner.setSelection(statusPosition);
        setStatusColor(holder.statusSpinner, task.getStatus());

        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                String selectedStatus = (String) parentView.getItemAtPosition(pos);
                if (!selectedStatus.equals(task.getStatus())) {
                    task.setStatus(selectedStatus);
                    // Update task status in Firestore
                    db.collection("tasks").document(task.getId()).update("status", selectedStatus);
                    setStatusColor(holder.statusSpinner, selectedStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No action needed
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove task from Firestore
                db.collection("tasks").document(task.getId()).delete();
                // Remove task from the list
                taskList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, taskList.size());
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), TaskDetailActivity.class);
                intent.putExtra("title", task.getTitle());
                intent.putExtra("description", task.getDescription());
                intent.putExtra("dueDate", task.getDueDate());
                intent.putExtra("assignedUsers", String.join(", ", task.getAssignedUsers()));
                intent.putExtra("status", task.getStatus());
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    private void setStatusColor(Spinner statusSpinner, String status) {
        switch (status) {
            case "Pending":
                statusSpinner.setBackgroundColor(Color.RED);
                break;
            case "In-Progress":
                statusSpinner.setBackgroundColor(Color.YELLOW);
                break;
            case "Completed":
                statusSpinner.setBackgroundColor(Color.GREEN);
                break;
            default:
                statusSpinner.setBackgroundColor(Color.WHITE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dueDateTextView, assignedUserTextView;
        Spinner statusSpinner;
        Button deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            dueDateTextView = itemView.findViewById(R.id.dueDateTextView);
            assignedUserTextView = itemView.findViewById(R.id.assignedUserTextView);
            statusSpinner = itemView.findViewById(R.id.statusSpinner);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
