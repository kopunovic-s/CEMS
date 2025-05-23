package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

public class ListAnalyticsActivity extends AppCompatActivity {

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080/projects";
    private ArrayList<String> analyticsList = new ArrayList<>();
    private ArrayList<Integer> projectIdList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private int userId;
    private Button refreshBtn;
    private ListView analyticsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_list);

        // Allow network operations on the main thread (not recommended for production)
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_employee_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Analytics List");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get user data from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);

        refreshBtn = findViewById(R.id.refresh_button);
        analyticsListView = findViewById(R.id.analytics_list);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, analyticsList);
        analyticsListView.setAdapter(adapter);

        fetchProjects();

        analyticsListView.setOnItemClickListener((parent, view, position, id) -> {
            int selectedProjectId = projectIdList.get(position);

            Intent detailIntent = new Intent(ListAnalyticsActivity.this, AnalyticsDetailActivity.class);
            detailIntent.putExtra("projectId", selectedProjectId);
            detailIntent.putExtra("userId", userId);
            startActivity(detailIntent);
        });

        refreshBtn.setOnClickListener(v -> fetchProjects());
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

    private void fetchProjects() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            Scanner scanner = null;
            try {
                URL url = new URL(BASE_URL + "/" + userId);
                Log.d("fetchAnalytics", "Fetching data from " + url);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                // Handling InputStream with InputStreamReader
                scanner = new Scanner(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                JSONArray projects = new JSONArray(response.toString());
                analyticsList.clear();
                projectIdList.clear();

                for (int i = 0; i < projects.length(); i++) {
                    JSONObject data = projects.getJSONObject(i);
                    int projectId = data.getInt("id");
                    String projectName = data.optString("name", "Unnamed Project"); // Fetch project name (add "Unnamed Project" as fallback)
                    projectIdList.add(projectId);
                    analyticsList.add(projectName); // Display project name instead of just ID
                }

                // Update UI on the main thread
                runOnUiThread(() -> adapter.notifyDataSetChanged());

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
}