package com.example.companyloginapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.java_websocket.handshake.ServerHandshake;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ProjectDetailsActivity extends AppCompatActivity implements ProjectWebSocketListener {

    private int userId, projectId, companyId;
    private String role, username, firstName, lastName;
    private boolean isActive;

    private TextView projectName, projectStatus;
    private EditText projectDescription, projectDueDate;
    private AutoCompleteTextView assignedUsers;
    private Button saveButton, toggleStatusButton, completeProjectButton, addUserButton, removeUserButton;
    private ListView assignedUsersListView;

    private RecyclerView chatRecycler;
    private EditText chatInput;
    private Button sendChatButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();

    private ArrayAdapter<String> assignedUsersAdapter;
    private List<String> assignedUsersList = new ArrayList<>();
    private String selectedUserEmail = null;

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_details);

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        projectId = intent.getIntExtra("projectId", -1);
        companyId = intent.getIntExtra("companyID", -1);
        role = intent.getStringExtra("role");
        username = intent.getStringExtra("username");
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");

        if (firstName == null) firstName = "Unknown";
        if (lastName == null) lastName = "User";

        TextView userNameTextView = findViewById(R.id.user_name_text_view);
        userNameTextView.setText(firstName + " " + lastName);

        projectName = findViewById(R.id.project_name);
        projectStatus = findViewById(R.id.project_status);
        projectDescription = findViewById(R.id.project_description);
        projectDueDate = findViewById(R.id.project_due_date);
        assignedUsers = findViewById(R.id.assigned_users);
        saveButton = findViewById(R.id.save_button);
        toggleStatusButton = findViewById(R.id.toggle_status_button);
        completeProjectButton = findViewById(R.id.complete_project_button);
        addUserButton = findViewById(R.id.add_user_button);
        removeUserButton = findViewById(R.id.remove_user_button);
        assignedUsersListView = findViewById(R.id.assigned_users_list);
        chatRecycler = findViewById(R.id.chat_recycler);
        chatInput = findViewById(R.id.chat_input);
        sendChatButton = findViewById(R.id.send_chat_button);

        assignedUsersAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, assignedUsersList);
        assignedUsersListView.setAdapter(assignedUsersAdapter);

        assignedUsersListView.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedUserEmail = assignedUsersList.get(i);
            assignedUsersListView.setItemChecked(i, true);
        });

        projectDueDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(ProjectDetailsActivity.this, (view, y, m, d) -> {
                String formatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
                projectDueDate.setText(formatted);
            }, year, month, day).show();
        });

        saveButton.setOnClickListener(v -> saveProjectChanges());
        toggleStatusButton.setOnClickListener(v -> toggleProjectStatus());
        completeProjectButton.setOnClickListener(v -> markProjectCompleted());

        addUserButton.setOnClickListener(v -> {
            String email = assignedUsers.getText().toString().trim();
            if (!email.isEmpty()) assignUserToProject(email);
        });

        removeUserButton.setOnClickListener(v -> {
            if (selectedUserEmail != null) removeUserFromProject(selectedUserEmail);
        });

        sendChatButton.setOnClickListener(v -> {
            String message = chatInput.getText().toString().trim();
            if (!message.isEmpty()) {
                String fullName = firstName + " " + lastName;
                LiveProjectWebSocketManager.getInstance().sendMessage(projectId, fullName, message);
                chatInput.setText("");
            }
        });

        boolean hasPermission = role.equalsIgnoreCase("owner") || role.equalsIgnoreCase("manager");
        addUserButton.setEnabled(hasPermission);
        removeUserButton.setEnabled(hasPermission);

        fetchProjectDetails();
        loadCompanyUsersForDropdown();
        setupChatRecycler();
        setupWebSocket();
    }

    private void setupWebSocket() {
        LiveProjectWebSocketManager.getInstance().setWebSocketListener(this);
        String fullNameForSocket = (firstName + "_" + lastName).replace(" ", "_");
        LiveProjectWebSocketManager.getInstance().connect(BASE_URL + "/project-chat/" + projectId + "/" + fullNameForSocket);
    }

    private void setupChatRecycler() {
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(chatAdapter);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d("WebSocket", "Connected to project chat");
    }

    @Override
    public void onMessage(String message) {
        runOnUiThread(() -> {
            String sender = "";
            String content = message;

            if (message.contains(":")) {
                int firstColon = message.indexOf(":");
                sender = message.substring(0, firstColon).trim();
                content = message.substring(firstColon + 1).trim();

                // Check for nested sender prefix
                if (content.contains(":")) {
                    int secondColon = content.indexOf(":");
                    String secondPrefix = content.substring(0, secondColon).trim();

                    if (sender.equals(secondPrefix)) {
                        content = content.substring(secondColon + 1).trim();
                    }
                }
            }

            chatMessages.add(new ChatMessage(sender, content, ""));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecycler.scrollToPosition(chatMessages.size() - 1);
        });
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d("WebSocket", "Disconnected from chat: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.e("WebSocket", "Error: " + ex.getMessage());
        runOnUiThread(() -> Toast.makeText(ProjectDetailsActivity.this, "WebSocket error: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchProjectDetails() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/projects/id/" + userId + "/" + projectId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) response.append(scanner.nextLine());
                scanner.close();

                JSONObject project = new JSONObject(response.toString());

                runOnUiThread(() -> {
                    try {
                        projectName.setText(project.getString("name"));
                        projectDescription.setText(project.getString("description"));
                        projectDueDate.setText(project.optString("deadline", "").replace("T00:00:00", ""));
                        isActive = project.getBoolean("isActive");
                        projectStatus.setText(isActive ? "Active" : "Inactive");
                        toggleStatusButton.setText(isActive ? "Mark Inactive" : "Reopen Project");

                        JSONArray users = project.getJSONArray("users");
                        assignedUsersList.clear();
                        for (int i = 0; i < users.length(); i++) {
                            assignedUsersList.add(users.getJSONObject(i).getString("email"));
                        }
                        assignedUsersAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error fetching project details", Toast.LENGTH_SHORT).show());
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
                List<String> emails = new ArrayList<>();

                for (int i = 0; i < users.length(); i++) {
                    emails.add(users.getJSONObject(i).getString("email"));
                }

                runOnUiThread(() -> {
                    ArrayAdapter<String> userAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, emails);
                    assignedUsers.setAdapter(userAdapter);
                    assignedUsers.setOnClickListener(v -> assignedUsers.showDropDown());
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void assignUserToProject(String email) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/projects/" + projectId + "/project-add-user/" + userId + "/" + email);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> {
                        assignedUsersList.add(email);
                        assignedUsersAdapter.notifyDataSetChanged();
                        assignedUsers.setText("");
                        Toast.makeText(this, "User added", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error adding user", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void removeUserFromProject(String email) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/projects/" + projectId + "/project-remove-user/" + userId + "/" + email);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");

                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> {
                        assignedUsersList.remove(email);
                        assignedUsersAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "User removed", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to remove user", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error removing user", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void saveProjectChanges() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/projects/" + userId + "/" + projectId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("name", projectName.getText().toString().trim());
                body.put("description", projectDescription.getText().toString().trim());
                body.put("deadline", projectDueDate.getText().toString().trim() + "T00:00:00");

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(body.toString());
                writer.flush();
                writer.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Project updated", Toast.LENGTH_SHORT).show();
                        fetchProjectDetails(); // re-fetch to reflect saved values
                    });
                } else {
                    Scanner scanner = new Scanner(conn.getErrorStream());
                    StringBuilder error = new StringBuilder();
                    while (scanner.hasNext()) error.append(scanner.nextLine());
                    scanner.close();

                    Log.e("ProjectUpdate", "Error: " + error.toString());
                    runOnUiThread(() -> Toast.makeText(this, "Failed to update project", Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error saving project", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    private void toggleProjectStatus() {
        new Thread(() -> {
            try {
                String action = isActive ? "close" : "open";
                URL url = new URL(BASE_URL + "/projects/" + action + "/" + userId + "/" + projectId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");

                if (conn.getResponseCode() == 200) {
                    isActive = !isActive;
                    runOnUiThread(() -> {
                        projectStatus.setText(isActive ? "Active" : "Inactive");
                        toggleStatusButton.setText(isActive ? "Mark Inactive" : "Reopen Project");
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error toggling status", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void markProjectCompleted() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/projects/" + userId + "/" + projectId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("status", "COMPLETE");

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(body.toString());
                writer.flush();
                writer.close();

                runOnUiThread(() -> Toast.makeText(this, "Marked complete", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error completing project", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}