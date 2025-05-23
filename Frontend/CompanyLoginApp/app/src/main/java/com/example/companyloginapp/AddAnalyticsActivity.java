package com.example.companyloginapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AddAnalyticsActivity extends AppCompatActivity {

    private int userId;
    private EditText dateInput, incomeInput, expensesInput;
    private Button addButton;
    private Spinner projectSpinner;
    private HashMap<String, Integer> projectMap = new HashMap<>();
    private int selectedProjectId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_add);

        // Allow network operations on the main thread (only for quick testing)
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_employee_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Analytics");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get userId from Intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);

        // Initialize views
        projectSpinner = findViewById(R.id.spinner_project);
        dateInput = findViewById(R.id.edit_date);
        incomeInput = findViewById(R.id.edit_income);
        expensesInput = findViewById(R.id.edit_expenses);
        addButton = findViewById(R.id.add_analytics_button);

        // Load project data
        loadProjectsFromServer();

        // Set date picker
        dateInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AddAnalyticsActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Pad month and day with leading zeros if needed
                        String paddedMonth = String.format("%02d", selectedMonth + 1); // Month is 0-based
                        String paddedDay = String.format("%02d", selectedDay);
                        String selectedDate = selectedYear + "-" + paddedMonth + "-" + paddedDay;
                        dateInput.setText(selectedDate);
                    }, year, month, day);

            datePickerDialog.show();
        });

        // Submit analytics
        addButton.setOnClickListener(v -> addAnalytics());
    }

    private void addAnalytics() {
        String date = dateInput.getText().toString().trim();
        String incomeText = incomeInput.getText().toString().trim();
        String expenseText = expensesInput.getText().toString().trim();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(incomeText) || TextUtils.isEmpty(expenseText)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProjectId == -1) {
            Toast.makeText(this, "Please select a project", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int income = Integer.parseInt(incomeText);
            int expenses = Integer.parseInt(expenseText);

            JSONObject requestBody = new JSONObject();
            requestBody.put("date", date);
            requestBody.put("income", income);
            requestBody.put("expenses", expenses);

            String fullUrl = "http://coms-3090-024.class.las.iastate.edu:8080/sales/post-data/" + userId + "/" + selectedProjectId;
            android.util.Log.d("POST_URL", fullUrl);
            android.util.Log.d("POST_BODY", requestBody.toString());

            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.toString().getBytes("UTF-8"));
            os.close();

            int responseCode = conn.getResponseCode();
            android.util.Log.d("POST_RESPONSE_CODE", String.valueOf(responseCode));

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                Toast.makeText(this, "Analytics added successfully", Toast.LENGTH_SHORT).show();
                conn.disconnect();
                finish();
            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                android.util.Log.e("POST_ERROR", errorResponse.toString());

                Toast.makeText(this, "Failed to add analytics. Code: " + responseCode, Toast.LENGTH_LONG).show();
            }

            conn.disconnect();

        } catch (Exception e) {
            android.util.Log.e("POST_EXCEPTION", e.toString());
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    private void loadProjectsFromServer() {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-024.class.las.iastate.edu:8080/projects/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                conn.disconnect();

                JSONArray jsonArray = new JSONArray(response.toString());
                List<String> projectNames = new ArrayList<>();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    int id = obj.getInt("id");
                    String name = obj.getString("name");
                    projectMap.put(name, id);
                    projectNames.add(name);
                }

                runOnUiThread(() -> {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, projectNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    projectSpinner.setAdapter(adapter);

                    projectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedName = parent.getItemAtPosition(position).toString();
                            selectedProjectId = projectMap.get(selectedName);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedProjectId = -1;
                        }
                    });
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error loading projects", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Handle toolbar back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

