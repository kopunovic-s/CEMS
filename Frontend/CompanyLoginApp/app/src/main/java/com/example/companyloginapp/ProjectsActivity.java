package com.example.companyloginapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ProjectsActivity extends AppCompatActivity {

    private int userId, companyID;
    private String role, userEmail, firstName, lastName, username;
    private int selectedProjectIndex = -1;

    private ArrayList<String> projectList = new ArrayList<>();
    private ArrayList<Integer> projectIds = new ArrayList<>();
    private ArrayList<Boolean> projectStatus = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private EditText projectInput, dueDateInput;
    private AutoCompleteTextView assignedUsersInput;
    private CheckBox visibilityCheckbox;

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        companyID = intent.getIntExtra("companyID", -1);
        role = intent.getStringExtra("role");
        userEmail = intent.getStringExtra("userEmail");
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        username = intent.getStringExtra("username");

        ListView projectsListView = findViewById(R.id.projects_list);
        projectInput = findViewById(R.id.project_input);
        dueDateInput = findViewById(R.id.project_due_date_input);
        assignedUsersInput = findViewById(R.id.project_assigned_users_input);
        visibilityCheckbox = findViewById(R.id.visibility_checkbox);

        dueDateInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ProjectsActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        dueDateInput.setText(formatted);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        Button addProjectButton = findViewById(R.id.add_project_button);
        Button fetchProjectsButton = findViewById(R.id.fetch_projects_button);
        Button fetchActiveProjectsButton = findViewById(R.id.fetch_active_projects_button);
        Button fetchInactiveProjectsButton = findViewById(R.id.fetch_inactive_projects_button);
        Button deleteProjectButton = findViewById(R.id.delete_project_button);
        Button toggleProjectButton = findViewById(R.id.toggle_project_button);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, projectList);
        projectsListView.setAdapter(adapter);

        projectsListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedProjectIndex = position;
            int projectId = projectIds.get(position);

            Intent detailIntent = new Intent(this, ProjectDetailsActivity.class);
            detailIntent.putExtra("userId", userId);
            detailIntent.putExtra("projectId", projectId);
            detailIntent.putExtra("companyID", companyID);
            detailIntent.putExtra("role", role);
            detailIntent.putExtra("username", username);
            detailIntent.putExtra("firstName", firstName);
            detailIntent.putExtra("lastName", lastName);
            startActivity(detailIntent);
        });

        fetchProjectsButton.setOnClickListener(v -> fetchProjects("all"));
        fetchActiveProjectsButton.setOnClickListener(v -> fetchProjects("active"));
        fetchInactiveProjectsButton.setOnClickListener(v -> fetchProjects("inactive"));

        addProjectButton.setOnClickListener(v -> {
            String name = projectInput.getText().toString().trim();
            String dueDate = dueDateInput.getText().toString().trim();
            String assignedUsers = assignedUsersInput.getText().toString().trim();

            if (!name.isEmpty()) {
                addProject(name, dueDate, assignedUsers);
                projectInput.setText("");
                dueDateInput.setText("");
                assignedUsersInput.setText("");
                visibilityCheckbox.setChecked(false);
            } else {
                Toast.makeText(this, "Project name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        deleteProjectButton.setOnClickListener(v -> {
            if (selectedProjectIndex != -1 && selectedProjectIndex < projectIds.size()) {
                int projectId = projectIds.get(selectedProjectIndex);
                deleteProject(projectId);
                selectedProjectIndex = -1;
            } else {
                Toast.makeText(this, "Please select a project to delete", Toast.LENGTH_SHORT).show();
            }
        });

        toggleProjectButton.setOnClickListener(v -> {
            if (selectedProjectIndex != -1 && selectedProjectIndex < projectIds.size()) {
                int projectId = projectIds.get(selectedProjectIndex);
                boolean currentStatus = projectStatus.get(selectedProjectIndex);
                updateProjectStatus(projectId, !currentStatus);
            } else {
                Toast.makeText(this, "Please select a project to toggle", Toast.LENGTH_SHORT).show();
            }
        });

        fetchProjects("all");
        loadCompanyUsersForDropdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchProjects("all");
    }

    private void addProject(String projectName, String dueDate, String assignedUsers) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/projects/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject project = new JSONObject();
                project.put("name", projectName);
                project.put("description", "Created via app");
                project.put("isActive", true);
                project.put("visibleToEntireCompany", visibilityCheckbox.isChecked());

                if (!dueDate.isEmpty()) {
                    String formatted = dueDate + "T00:00:00";
                    project.put("deadline", formatted);
                }

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(project.toString());
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) response.append(scanner.nextLine());
                scanner.close();

                String responseText = response.toString().toLowerCase();
                if ((responseCode == 200 || responseCode == 201) && responseText.contains("success")) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Project added!", Toast.LENGTH_SHORT).show();
                        new Handler(Looper.getMainLooper()).postDelayed(() -> fetchProjects("all"), 500);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Project not added: " + responseText, Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("AddProjectError", "Exception: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error adding project", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateProjectStatus(int projectId, boolean newStatus) {
        new Thread(() -> {
            try {
                String action = newStatus ? "open" : "close";
                URL url = new URL(BASE_URL + "/projects/" + action + "/" + userId + "/" + projectId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");

                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> {
                        fetchProjects("all");
                        Toast.makeText(this, "Project status updated", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void deleteProject(int projectId) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/projects/" + userId + "/" + projectId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> fetchProjects("all"));
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Remove Assigned Users First", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error deleting project", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void fetchProjects(String type) {
        new Thread(() -> {
            try {
                String endpoint = "/projects/" + userId;
                if (type.equals("active")) endpoint = "/projects/active/" + userId;
                else if (type.equals("inactive")) endpoint = "/projects/inactive/" + userId;

                URL url = new URL(BASE_URL + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) response.append(scanner.nextLine());
                scanner.close();

                JSONArray projects = new JSONArray(response.toString());
                projectList.clear();
                projectIds.clear();
                projectStatus.clear();

                for (int i = 0; i < projects.length(); i++) {
                    JSONObject project = projects.getJSONObject(i);
                    String name = project.getString("name");
                    int projectId = project.getInt("id");
                    boolean isActive = project.getBoolean("isActive");
                    String status = project.optString("status", "IN_PROGRESS");
                    String deadline = project.optString("deadline", "");

                    projectIds.add(projectId);
                    projectStatus.add(isActive);

                    String readableStatus;
                    if ("COMPLETE".equalsIgnoreCase(status)) {
                        readableStatus = "Complete";
                    } else if ("IN_PROGRESS".equalsIgnoreCase(status)) {
                        readableStatus = "In Progress";
                    } else {
                        readableStatus = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
                    }

                    String display = name + " - " + (isActive ? "Active" : "Inactive") + " - " + readableStatus;
                    if (!deadline.isEmpty() && !deadline.equals("null")) {
                        display += " (Due: " + deadline.substring(0, 10) + ")";
                    }

                    projectList.add(display);
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("FetchProjectsError", "Exception: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Failed to fetch projects", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadCompanyUsersForDropdown() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/users/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) response.append(scanner.nextLine());
                scanner.close();

                JSONArray users = new JSONArray(response.toString());
                List<String> userEmails = new ArrayList<>();

                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    if (user.has("email") && !user.isNull("email")) {
                        userEmails.add(user.getString("email"));
                    }
                }

                runOnUiThread(() -> {
                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            userEmails
                    );

                    assignedUsersInput.setAdapter(userAdapter);
                    assignedUsersInput.setOnClickListener(v -> assignedUsersInput.showDropDown());
                    assignedUsersInput.setOnItemClickListener((adapterView, view, position, id) -> {
                        String selected = userAdapter.getItem(position);
                        String current = assignedUsersInput.getText().toString();
                        if (!current.contains(selected)) {
                            assignedUsersInput.setText(current.isEmpty() ? selected : current + ", " + selected);
                            assignedUsersInput.setSelection(assignedUsersInput.getText().length());
                        }
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("UserDropdownError", "Exception: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}