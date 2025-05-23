package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class AnalyticsDetailActivity extends AppCompatActivity {

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080/sales";
    private int userId;
    private int projectId;
    private Button deleteBtn, editBtn;
    private TextView analyticsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_detail);

        // Allow network operations on the main thread (not recommended for production)
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_analytics_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get user data from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        projectId = intent.getIntExtra("projectId", -1);

        deleteBtn = findViewById(R.id.delete_button);
        editBtn = findViewById(R.id.edit_button);

        analyticsText = findViewById(R.id.analytics_text);

        fetchAnalytics();

        deleteBtn.setOnClickListener(v -> fetchAnalytics());
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

    private void fetchAnalytics() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            Scanner scanner = null;
            try {
            //    URL url = new URL(BASE_URL + "/get-data-project/" + userId + "/" + projectId);
                URL url = new URL("http://coms-3090-024.class.las.iastate.edu:8080/sales/get-data-project/1/1");
                Log.d("fetchAnalytics", "Fetching data from " + url);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                scanner = new Scanner(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                JSONArray analyticsArray = new JSONArray(response.toString());

                StringBuilder displayText = new StringBuilder();
                for (int i = 0; i < analyticsArray.length(); i++) {
                    JSONObject analytic = analyticsArray.getJSONObject(i);

                    int id = analytic.getInt("id");
                    String date = analytic.getString("date");
                    double income = analytic.getDouble("income");
                    double expenses = analytic.getDouble("expenses");
                    double revenue = analytic.getDouble("revenue");

                    displayText.append("Data ID: ").append(id).append("\n")
                            .append("Date:" ).append(date).append("\n")
                            .append("Income: $").append(income).append("\n")
                            .append("Expenses: $").append(expenses).append("\n")
                            .append("Revenue: $").append(revenue).append("\n\n");
                }

                runOnUiThread(() -> analyticsText.setText(displayText.toString()));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to fetch analytics", Toast.LENGTH_SHORT).show());
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    private void deleteAnalytics() {

    }


}

