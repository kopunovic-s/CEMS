package com.example.companyloginapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class ScheduleActivity extends AppCompatActivity {

    private int userId, companyId;
    private String firstName, lastName, email, role;

    private TextView scheduleTitle, scheduleDisplay;
    private Button refreshButton, editTimeButton, editScheduleButton;
    private Spinner userSpinner;

    private int selectedUserId;
    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        companyId = intent.getIntExtra("companyID", -1);
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        email = intent.getStringExtra("email");
        role = intent.getStringExtra("role");

        scheduleTitle = findViewById(R.id.schedule_title);
        scheduleDisplay = findViewById(R.id.schedule_display);
        refreshButton = findViewById(R.id.refresh_button);
        editTimeButton = findViewById(R.id.edit_time_button);
        userSpinner = findViewById(R.id.user_spinner);
        editScheduleButton = findViewById(R.id.edit_schedule_button);

        setupUserDropdown();

        refreshButton.setOnClickListener(v -> fetchSchedule());

        editTimeButton.setOnClickListener(v -> {
            if (selectedUserId == userId || isManager()) {
                showEditAvailabilityDialog();
            } else {
                Toast.makeText(this, "No permission to edit this user's availability", Toast.LENGTH_SHORT).show();
            }
        });

        if (isManager()) {
            editScheduleButton.setVisibility(View.VISIBLE);
            editScheduleButton.setOnClickListener(v -> showEditScheduleDialog());
        } else {
            editScheduleButton.setVisibility(View.GONE);
        }
    }

    private boolean isManager() {
        return role.equalsIgnoreCase("OWNER") ||
                role.equalsIgnoreCase("EXECUTIVE") ||
                role.equalsIgnoreCase("MANAGER");
    }

    private void setupUserDropdown() {
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
                List<Integer> ids = new ArrayList<>();

                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    emails.add(user.getString("email"));
                    ids.add(user.getInt("id"));
                }

                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ScheduleActivity.this,
                            R.layout.spinner_item,
                            emails
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userSpinner.setAdapter(adapter);

                    if (!isManager()) {
                        userSpinner.setEnabled(false);
                        userSpinner.setVisibility(View.GONE);
                        selectedUserId = userId;
                        fetchSchedule();
                        return;
                    }

                    userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedUserId = ids.get(position);
                            fetchSchedule();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedUserId = userId;
                        }
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void fetchSchedule() {
        new Thread(() -> {
            try {
                String date = new SimpleDateFormat("MM-dd-yyyy", Locale.US).format(new Date());
                String scheduleEndpoint = "/schedule/" + userId + "/user/" + selectedUserId + "/week/" + date;
                String availabilityEndpoint = "/availability/" + userId + "/user/" + selectedUserId;

                URL scheduleUrl = new URL(BASE_URL + scheduleEndpoint);
                HttpURLConnection scheduleConn = (HttpURLConnection) scheduleUrl.openConnection();
                scheduleConn.setRequestMethod("GET");

                Scanner scheduleScanner = new Scanner(scheduleConn.getInputStream());
                StringBuilder scheduleResponse = new StringBuilder();
                while (scheduleScanner.hasNext()) scheduleResponse.append(scheduleScanner.nextLine());
                scheduleScanner.close();

                URL availabilityUrl = new URL(BASE_URL + availabilityEndpoint);
                HttpURLConnection availabilityConn = (HttpURLConnection) availabilityUrl.openConnection();
                availabilityConn.setRequestMethod("GET");

                Scanner availabilityScanner = new Scanner(availabilityConn.getInputStream());
                StringBuilder availabilityResponse = new StringBuilder();
                while (availabilityScanner.hasNext()) availabilityResponse.append(availabilityScanner.nextLine());
                availabilityScanner.close();

                runOnUiThread(() -> {
                    try {
                        StringBuilder display = new StringBuilder();

                        display.append("\uD83D\uDCC5 SCHEDULE:\n\n");
                        JSONArray arr = new JSONArray(scheduleResponse.toString());
                        if (arr.length() == 0) {
                            createScheduleFromAvailability();
                            return;
                        }
                        for (int i = 0; i < arr.length(); i++) {
                            display.append(formatSchedule(arr.getJSONObject(i))).append("\n\n");
                        }

                        display.append("\uD83D\uDD52 AVAILABILITY:\n\n");
                        JSONArray availArr = new JSONArray(availabilityResponse.toString());
                        for (int i = 0; i < availArr.length(); i++) {
                            JSONObject item = availArr.getJSONObject(i);
                            String day = item.optString("dayOfWeek");
                            boolean isAvailable = item.optBoolean("isAvailable");
                            String start = item.optString("startTime");
                            String end = item.optString("endTime");

                            display.append(day)
                                    .append(": ")
                                    .append(isAvailable ? start + " - " + end : "Not Available")
                                    .append("\n");
                        }

                        scheduleDisplay.setText(display.toString().trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                        scheduleDisplay.setText("Error processing schedule or availability.");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> scheduleDisplay.setText("Error loading schedule or availability."));
            }
        }).start();
    }

    private String formatSchedule(JSONObject obj) {
        String start = obj.optString("startTime", "").replace("-", "/");
        String end = obj.optString("endTime", "").replace("-", "/");
        return "Start: " + start + "\nEnd: " + end;
    }

    private void createScheduleFromAvailability() {
        new Thread(() -> {
            try {
                String weekStart = new SimpleDateFormat("MM-dd-yyyy", Locale.US).format(new Date());
                String createEndpoint = "/schedule/" + userId + "/createWeekAvailability/" + selectedUserId + "/date/" + weekStart;

                URL postUrl = new URL(BASE_URL + createEndpoint);
                HttpURLConnection conn = (HttpURLConnection) postUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.getOutputStream().write("[]".getBytes());

                int responseCode = conn.getResponseCode();
                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder responseBody = new StringBuilder();
                while (scanner.hasNext()) responseBody.append(scanner.nextLine());
                scanner.close();

                runOnUiThread(() -> {
                    if (responseCode == 200 || responseCode == 201) {
                        Toast.makeText(this, "Schedule created from availability!", Toast.LENGTH_SHORT).show();
                        fetchSchedule();
                    } else {
                        scheduleDisplay.setText("Failed to create schedule:\n" + responseBody);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> scheduleDisplay.setText("Error: schedule creation failed."));
            }
        }).start();
    }

    private LinearLayout.LayoutParams getDialogMarginParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 8, 0, 8);
        return params;
    }

    private void showEditAvailabilityDialog() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/availability/" + userId + "/user/" + selectedUserId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) response.append(scanner.nextLine());
                scanner.close();

                JSONArray availabilityArray = new JSONArray(response.toString());

                runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                    builder.setTitle("Edit Availability");

                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_availability, null);
                    LinearLayout container = dialogView.findViewById(R.id.edit_availability_container);

                    List<View[]> inputList = new ArrayList<>();
                    String[] daysOfWeek = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};

                    for (String day : daysOfWeek) {
                        JSONObject dayData = null;
                        for (int i = 0; i < availabilityArray.length(); i++) {
                            JSONObject item = availabilityArray.optJSONObject(i);
                            if (item.optString("dayOfWeek").equalsIgnoreCase(day)) {
                                dayData = item;
                                break;
                            }
                        }

                        CheckBox availableCheck = new CheckBox(ScheduleActivity.this);
                        availableCheck.setText(day);
                        availableCheck.setLayoutParams(getDialogMarginParams());
                        container.addView(availableCheck);

                        EditText startInput = new EditText(ScheduleActivity.this);
                        startInput.setHint("Start Time (HH:mm)");
                        startInput.setLayoutParams(getDialogMarginParams());
                        container.addView(startInput);

                        EditText endInput = new EditText(ScheduleActivity.this);
                        endInput.setHint("End Time (HH:mm)");
                        endInput.setLayoutParams(getDialogMarginParams());
                        container.addView(endInput);

                        if (dayData != null) {
                            availableCheck.setChecked(dayData.optBoolean("isAvailable", false));
                            startInput.setText(dayData.optString("startTime", ""));
                            endInput.setText(dayData.optString("endTime", ""));
                        }

                        inputList.add(new View[]{availableCheck, startInput, endInput});
                    }

                    builder.setView(dialogView);
                    builder.setPositiveButton("Save", (dialog, which) -> {
                        new Thread(() -> {
                            try {
                                JSONArray newAvailability = new JSONArray();

                                for (int i = 0; i < inputList.size(); i++) {
                                    CheckBox check = (CheckBox) inputList.get(i)[0];
                                    EditText start = (EditText) inputList.get(i)[1];
                                    EditText end = (EditText) inputList.get(i)[2];

                                    if (!check.isChecked()) continue;

                                    String startTime = start.getText().toString().trim();
                                    String endTime = end.getText().toString().trim();

                                    if (!startTime.matches("^\\d{2}:\\d{2}$") || !endTime.matches("^\\d{2}:\\d{2}$")) {
                                        int finalI = i;
                                        runOnUiThread(() -> Toast.makeText(this,
                                                "Time format error on " + daysOfWeek[finalI] + " (HH:mm required)", Toast.LENGTH_SHORT).show());
                                        return;
                                    }

                                    JSONObject entry = new JSONObject();
                                    entry.put("dayOfWeek", daysOfWeek[i]);
                                    entry.put("startTime", startTime);
                                    entry.put("endTime", endTime);
                                    entry.put("isAvailable", true);
                                    newAvailability.put(entry);
                                }

                                URL postUrl = new URL(BASE_URL + "/availability/" + userId + "/update/" + selectedUserId);
                                HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
                                postConn.setRequestMethod("POST");
                                postConn.setRequestProperty("Content-Type", "application/json");
                                postConn.setDoOutput(true);
                                postConn.getOutputStream().write(newAvailability.toString().getBytes());

                                int responseCode = postConn.getResponseCode();
                                runOnUiThread(() -> {
                                    if (responseCode == 200) {
                                        Toast.makeText(this, "Availability updated!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Failed to update availability.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(() -> Toast.makeText(this, "Error saving availability.", Toast.LENGTH_SHORT).show());
                            }
                        }).start();
                    });

                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to load availability.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    private void showEditScheduleDialog() {
        new Thread(() -> {
            try {
                String date = new SimpleDateFormat("MM-dd-yyyy", Locale.US).format(new Date());
                URL url = new URL(BASE_URL + "/schedule/" + userId + "/user/" + selectedUserId + "/week/" + date);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) response.append(scanner.nextLine());
                scanner.close();

                JSONArray scheduleArray = new JSONArray(response.toString());

                runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                    builder.setTitle("Edit Weekly Schedule");

                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_schedule, null);
                    LinearLayout container = dialogView.findViewById(R.id.edit_schedule_container);

                    List<View[]> inputs = new ArrayList<>();

                    for (int i = 0; i < scheduleArray.length(); i++) {
                        JSONObject obj = scheduleArray.optJSONObject(i);
                        int shiftId = obj.optInt("id");
                        String start = obj.optString("startTime").substring(11);
                        String end = obj.optString("endTime").substring(11);
                        String dateText = obj.optString("startTime").substring(0, 10);

                        TextView label = new TextView(this);
                        label.setText("📅 " + dateText + " (Shift ID: " + shiftId + ")");
                        label.setTypeface(null, Typeface.BOLD);
                        label.setLayoutParams(getDialogMarginParams());
                        container.addView(label);

                        EditText startInput = new EditText(this);
                        startInput.setHint("Start (HH:mm)");
                        startInput.setText(start);
                        startInput.setLayoutParams(getDialogMarginParams());
                        container.addView(startInput);

                        EditText endInput = new EditText(this);
                        endInput.setHint("End (HH:mm)");
                        endInput.setText(end);
                        endInput.setLayoutParams(getDialogMarginParams());
                        container.addView(endInput);

                        inputs.add(new View[]{label, startInput, endInput});
                    }

                    builder.setView(dialogView);
                    builder.setPositiveButton("Save", (dialog, which) -> {
                        new Thread(() -> {
                            try {
                                JSONArray updateArray = new JSONArray();
                                for (int i = 0; i < scheduleArray.length(); i++) {
                                    JSONObject original = scheduleArray.getJSONObject(i);
                                    int shiftId = original.getInt("id");
                                    String dateStr = original.getString("startTime").substring(0, 10);

                                    EditText startBox = (EditText) inputs.get(i)[1];
                                    EditText endBox = (EditText) inputs.get(i)[2];
                                    String newStart = startBox.getText().toString().trim();
                                    String newEnd = endBox.getText().toString().trim();

                                    if (!newStart.matches("^\\d{2}:\\d{2}$") || !newEnd.matches("^\\d{2}:\\d{2}$")) {
                                        int finalI = i;
                                        runOnUiThread(() -> Toast.makeText(this,
                                                "Invalid time format at index " + finalI + " (HH:mm required)", Toast.LENGTH_SHORT).show());
                                        return;
                                    }

                                    JSONObject updated = new JSONObject();
                                    updated.put("id", shiftId);
                                    updated.put("userId", selectedUserId);
                                    updated.put("startTime", dateStr + "-" + newStart);
                                    updated.put("endTime", dateStr + "-" + newEnd);

                                    updateArray.put(updated);
                                }

                                URL putUrl = new URL(BASE_URL + "/schedule/" + userId + "/updateBatch");
                                HttpURLConnection putConn = (HttpURLConnection) putUrl.openConnection();
                                putConn.setRequestMethod("PUT");
                                putConn.setRequestProperty("Content-Type", "application/json");
                                putConn.setDoOutput(true);
                                putConn.getOutputStream().write(updateArray.toString().getBytes());

                                int code = putConn.getResponseCode();
                                runOnUiThread(() -> {
                                    if (code == 200) {
                                        Toast.makeText(this, "Schedule updated!", Toast.LENGTH_SHORT).show();
                                        fetchSchedule();
                                    } else {
                                        Toast.makeText(this, "Failed to update schedule.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(() -> Toast.makeText(this, "Error saving updates", Toast.LENGTH_SHORT).show());
                            }
                        }).start();
                    });

                    builder.setNegativeButton("Cancel", null);
                    builder.show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Could not load schedule", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

}
