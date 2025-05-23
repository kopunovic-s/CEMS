package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import android.util.Log;



public class PayrollDetailActivity extends AppCompatActivity {

    private int userId, employeeId;
    private TextView employeeDetailText;
    private String date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payroll_detail);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        // Set up toolbar as action bar
        Toolbar toolbar = findViewById(R.id.toolbar_employee_detail);
        setSupportActionBar(toolbar);

        // Enable the back button in the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Employee Detail");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        employeeDetailText = findViewById(R.id.employee_detail_text);

        // Get user data from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        employeeId = intent.getIntExtra("employeeId", -1);
        date = intent.getStringExtra("date");

        fetchEmployeeDetails();
    }

    private void fetchEmployeeDetails() {
        try {
            String endpoint = "http://coms-3090-024.class.las.iastate.edu:8080/payroll/"
                    + userId + "/employeeSummary/" + employeeId + "/" + date;

            Log.d("PayrollDetailActivity", "Fetching data from endpoint: " + endpoint);

            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check the response code
            int responseCode = connection.getResponseCode();
            Log.d("PayrollDetailActivity", "Response Code: " + responseCode);

            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A"); // read entire stream
            String result = scanner.hasNext() ? scanner.next() : "";
            Log.d("PayrollDetailActivity", "Response: " + result);

            connection.disconnect();

            parseAndDisplayResult(result);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PayrollDetailActivity", "Error fetching employee details: ", e);
            employeeDetailText.setText("Failed to load employee details.");
        }
    }

    private void parseAndDisplayResult(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(jsonObject.getString("name")).append("\n");
        sb.append("Role: ").append(jsonObject.getString("role")).append("\n\n");
        sb.append("Daily Details:\n");

        JSONArray dailyDetails = jsonObject.getJSONArray("dailyDetails");
        for (int i = 0; i < dailyDetails.length(); i++) {
            JSONObject day = dailyDetails.getJSONObject(i);
            sb.append("Date: ").append(day.getString("weekDay")).append("\n");
            sb.append("Hours Worked: ").append(day.getDouble("hoursWorked")).append("\n");
            sb.append("Time: ").append(day.getString("times")).append("\n\n");
        }

        employeeDetailText.setText(sb.toString());
    }

    // Handle back button in toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to PayrollActivity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}