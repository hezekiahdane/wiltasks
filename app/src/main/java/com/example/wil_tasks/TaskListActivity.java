package com.example.wil_tasks;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskListActivity extends AppCompatActivity {

    private static final String TAG = "TaskListActivity";

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private FirebaseFirestore db;
    private Button filterButton, sortButton;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        recyclerView = findViewById(R.id.recyclerView);
        filterButton = findViewById(R.id.filterButton);
        sortButton = findViewById(R.id.sortButton);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerView.setAdapter(taskAdapter);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Load tasks without filters
        loadTasks(null, null);

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show filter options and reload tasks based on selection
                showFilterOptions();
            }
        });

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show sorting options and reload tasks based on selection
                showSortOptions();
            }
        });
    }

    private void loadTasks(String statusFilter, Query.Direction sortOrder) {
        if (currentUser == null) return;

        Query query = db.collection("tasks")
                .whereArrayContains("assignedUsers", currentUser.getEmail());

        if (statusFilter != null) {
            query = query.whereEqualTo("status", statusFilter);
        }
        if (sortOrder != null) {
            query = query.orderBy("dueDate", sortOrder);
        }

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e);
                    return;
                }

                Set<Task> taskSet = new HashSet<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Task task = doc.toObject(Task.class);
                    task.setId(doc.getId());
                    taskSet.add(task);
                }

                taskList.clear();
                taskList.addAll(taskSet);
                taskAdapter.notifyDataSetChanged();
                Log.d(TAG, "Tasks loaded: " + taskList.size());
            }
        });
    }

    private void showFilterOptions() {
        // Example filter: show only pending tasks
        String[] statuses = {"All", "Pending", "In Progress", "Completed"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Status")
                .setItems(statuses, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedStatus = statuses[which];
                        if (selectedStatus.equals("All")) {
                            loadTasks(null, null);
                        } else {
                            loadTasks(selectedStatus, null);
                        }
                        Toast.makeText(TaskListActivity.this, "Filtered by " + selectedStatus, Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
    }

    private void showSortOptions() {
        // Example sort: sort by due date
        String[] sortOptions = {"Ascending", "Descending"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort by Due Date")
                .setItems(sortOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Query.Direction selectedSortOrder = which == 0 ? Query.Direction.ASCENDING : Query.Direction.DESCENDING;
                        loadTasks(null, selectedSortOrder);
                        Toast.makeText(TaskListActivity.this, "Sorted by Due Date " + sortOptions[which], Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
    }
}

