package com.example.companyloginapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import java.io.InputStream;
import java.io.FileOutputStream;


public class AnalyticReportsActivity extends AppCompatActivity {

    private int userId, companyId;
    private String firstName, lastName, email, role;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TextView analyticReportsText, availableReportsText, createReportsText;
    private Spinner availableReportsSpinner, projectsSpinner;
    private Button viewReportButton, createReportButton;
    private HashMap<String, Integer> projectNameToIdMap = new HashMap<>();
    private HashMap<String, Long> reportNameToIdMap = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytic_reports);

        // Get user data from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        email = intent.getStringExtra("email");
        role = intent.getStringExtra("role");
        companyId = intent.getIntExtra("companyId", -1);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);

        analyticReportsText = findViewById(R.id.analytic_reports_text);
        availableReportsText = findViewById(R.id.available_reports_text);
        createReportsText = findViewById(R.id.create_reports_text);

        availableReportsSpinner = findViewById(R.id.available_reports_spinner);
        projectsSpinner = findViewById(R.id.projects_spinner);

        viewReportButton = findViewById(R.id.view_report_button);
        createReportButton = findViewById(R.id.create_report_button);

        // Setup toolbar and drawer
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation item click listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_projects) {
                    openProjectsPage();
                } else if (item.getItemId() == R.id.nav_sign_out) {
                    Toast.makeText(AnalyticReportsActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (item.getItemId() == R.id.nav_time_card) {
                    Toast.makeText(AnalyticReportsActivity.this, "Time Card", Toast.LENGTH_SHORT).show();
                    openTimeCardPage();
                } else if (item.getItemId() == R.id.nav_employees) {
                    Toast.makeText(AnalyticReportsActivity.this, "Employees", Toast.LENGTH_SHORT).show();
                    openEmployeesPage();
                } else if (item.getItemId() == R.id.nav_payroll) {
                    Toast.makeText(AnalyticReportsActivity.this, "Payroll", Toast.LENGTH_SHORT).show();
                    openPayrollPage();
                } else if (item.getItemId() == R.id.nav_live_time_card) {
                    Toast.makeText(AnalyticReportsActivity.this, "Live Time Card", Toast.LENGTH_SHORT).show();
                    openLiveTimeCardPage();
                } else if (item.getItemId() == R.id.nav_analytics) {
                    Toast.makeText(AnalyticReportsActivity.this, "Analytics", Toast.LENGTH_SHORT).show();
                    openAnalyticsPage();
                } else if (item.getItemId() == R.id.nav_store) {
                    Toast.makeText(AnalyticReportsActivity.this, "Store", Toast.LENGTH_SHORT).show();
                    openStorePage();
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });

        // Handle back button to close drawer if open
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        viewReportButton.setOnClickListener(v -> viewReport());

        loadAvailableReportsSpinner(userId);
        loadProjectsSpinner(userId);
        createReport();
    }

    private void loadAvailableReportsSpinner(int userId) {
        String mockUrl = "http://coms-3090-024.class.las.iastate.edu:8080/reports/get-reports-company/" + userId;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            ArrayList<String> reportList = new ArrayList<>();
            try {
                URL url = new URL(mockUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(json.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String reportName = obj.getString("fileName"); // e.g., "Report_12.pdf"
                    long reportId = obj.getLong("id");             // e.g., 12
                    reportList.add(reportName);
                    reportNameToIdMap.put(reportName, reportId);   // map name to ID
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                if (reportList.isEmpty()) {
                    Toast.makeText(AnalyticReportsActivity.this, "No reports found", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AnalyticReportsActivity.this,
                            android.R.layout.simple_spinner_item,
                            reportList
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    availableReportsSpinner.setAdapter(adapter);
                }
            });
        });
    }

    private void loadProjectsSpinner(int userId) {
        String mockUrl = "http://coms-3090-024.class.las.iastate.edu:8080/projects/" + userId;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            ArrayList<String> projectNames = new ArrayList<>();
            try {
                URL url = new URL(mockUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(json.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    int id = obj.getInt("id");           // from backend
                    String name = obj.getString("name"); // from backend
                    projectNames.add(name);
                    projectNameToIdMap.put(name, id);    // map name -> id
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.post(() -> {
                if (projectNames.isEmpty()) {
                    Toast.makeText(AnalyticReportsActivity.this, "No projects found", Toast.LENGTH_SHORT).show();
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AnalyticReportsActivity.this,
                            android.R.layout.simple_spinner_item,
                            projectNames
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    projectsSpinner.setAdapter(adapter);
                }
            });
        });
    }


    private void createReport() {
        createReportButton.setOnClickListener(view -> {
            String selectedProjectName = (String) projectsSpinner.getSelectedItem();
            if (selectedProjectName == null || selectedProjectName.isEmpty()) {
                Toast.makeText(this, "Please select a project", Toast.LENGTH_SHORT).show();
                return;
            }

            Integer projectId = projectNameToIdMap.get(selectedProjectName);
            if (projectId == null) {
                Toast.makeText(this, "Project ID not found", Toast.LENGTH_SHORT).show();
                return;
            }

            String urlStr = "http://coms-3090-024.class.las.iastate.edu:8080/reports/create-report/" + userId + "/" + projectId;

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                try {
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(false); // we’re not sending a body, just POSTing
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read the binary PDF bytes from response
                        InputStream input = conn.getInputStream();
                        String fileName = "Report_" + projectId + ".pdf"; // Or a UUID if needed
                        File file = new File(getExternalFilesDir(null), fileName);
                        FileOutputStream output = new FileOutputStream(file);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }

                        input.close();
                        output.close();
                        conn.disconnect();

                        handler.post(() -> {
                            Toast.makeText(this, "Report downloaded", Toast.LENGTH_SHORT).show();
                            openPdf(file);
                            loadAvailableReportsSpinner(userId); // refresh report list
                        });

                    } else {
                        handler.post(() -> Toast.makeText(this, "Report generation failed: " + responseCode, Toast.LENGTH_SHORT).show());
                        conn.disconnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    private void viewReport() {
        String selectedReport = (String) availableReportsSpinner.getSelectedItem();
        if (selectedReport == null || selectedReport.isEmpty()) {
            Toast.makeText(this, "Please select a report", Toast.LENGTH_SHORT).show();
            return;
        }

        Long reportId = reportNameToIdMap.get(selectedReport);
        if (reportId == null) {
            Toast.makeText(this, "Report ID not found for selected report", Toast.LENGTH_SHORT).show();
            return;
        }
        String urlStr = "http://coms-3090-024.class.las.iastate.edu:8080/reports/get-report/" + reportId;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream input = conn.getInputStream();
                    File file = new File(getExternalFilesDir(null), selectedReport);
                    FileOutputStream output = new FileOutputStream(file);

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }

                    input.close();
                    output.close();
                    conn.disconnect();

                    handler.post(() -> {
                        Toast.makeText(this, "Report opened", Toast.LENGTH_SHORT).show();
                        openPdf(file);
                    });
                } else {
                    handler.post(() -> Toast.makeText(this, "Failed to fetch report: " + responseCode, Toast.LENGTH_SHORT).show());
                    conn.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void openPdf(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
    }


    private void openProjectsPage() {
        Intent projectIntent = new Intent(this, ProjectsActivity.class);
        projectIntent.putExtra("userId", userId);
        projectIntent.putExtra("firstName", firstName);
        projectIntent.putExtra("lastName", lastName);
        projectIntent.putExtra("email", email);
        projectIntent.putExtra("role", role);
        startActivity(projectIntent);
    }

    private void openTimeCardPage() {
        Intent timeCardIntent = new Intent(this, TimeCardActivity.class);
        timeCardIntent.putExtra("userId", userId);
        timeCardIntent.putExtra("firstName", firstName);
        timeCardIntent.putExtra("lastName", lastName);
        timeCardIntent.putExtra("email", email);
        timeCardIntent.putExtra("role", role);
        startActivity(timeCardIntent);
    }

    private void openSchedulePage() {
        Intent scheduleIntent = new Intent(this, ScheduleActivity.class);
        scheduleIntent.putExtra("userId", userId);
        scheduleIntent.putExtra("firstName", firstName);
        scheduleIntent.putExtra("lastName", lastName);
        scheduleIntent.putExtra("email", email);
        scheduleIntent.putExtra("role", role);
        scheduleIntent.putExtra("companyID", companyId);
        startActivity(scheduleIntent);
    }

    private void openEmployeesPage() {
        Intent intent = new Intent(this, CompanyRosterActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("email", email);
        intent.putExtra("role", role);
        intent.putExtra("companyID", companyId);
        startActivity(intent);
    }

    private void openPayrollPage() {
        Intent payrollIntent = new Intent(this, PayrollActivity.class);
        payrollIntent.putExtra("userId", userId);
        payrollIntent.putExtra("firstName", firstName);
        payrollIntent.putExtra("lastName", lastName);
        payrollIntent.putExtra("email", email);
        payrollIntent.putExtra("role", role);
        payrollIntent.putExtra("companyID", companyId);
        startActivity(payrollIntent);
    }

    private void openLiveTimeCardPage() {
        Intent liveTCIntent = new Intent(this, LiveTimeCardActivity.class);
        liveTCIntent.putExtra("userId", userId);
        liveTCIntent.putExtra("firstName", firstName);
        liveTCIntent.putExtra("lastName", lastName);
        liveTCIntent.putExtra("email", email);
        liveTCIntent.putExtra("role", role);
        liveTCIntent.putExtra("companyId", companyId);
        startActivity(liveTCIntent);
    }

    private void openAnalyticsPage() {
        Intent analyticsIntent = new Intent(this, AnalyticsActivity.class);
        analyticsIntent.putExtra("userId", userId);
        analyticsIntent.putExtra("firstName", firstName);
        analyticsIntent.putExtra("lastName", lastName);
        analyticsIntent.putExtra("email", email);
        analyticsIntent.putExtra("role", role);
        analyticsIntent.putExtra("companyId", companyId);
        startActivity(analyticsIntent);
    }

    private void openStorePage() {
        Intent storeIntent = new Intent(this, StoreActivity.class);
        storeIntent.putExtra("userId", userId);
        storeIntent.putExtra("firstName", firstName);
        storeIntent.putExtra("lastName", lastName);
        storeIntent.putExtra("email", email);
        storeIntent.putExtra("role", role);
        storeIntent.putExtra("companyID", companyId);
        startActivity(storeIntent);
    }
}
