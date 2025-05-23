package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

//for graphing
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;
import java.util.HashMap;

public class AnalyticsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private int userId, companyId;
    private String firstName, lastName, email, role;
    private Button addAnalyticsBtn, listBtn, refreshBtn;
    private BarChart barChart;
    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080/sales";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Get user data from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        email = intent.getStringExtra("email");
        role = intent.getStringExtra("role");
        companyId = intent.getIntExtra("companyId", -1);

        barChart = findViewById(R.id.barChart);

        // Enable scrolling and zoom
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);
        barChart.setPinchZoom(true);

        addAnalyticsBtn = findViewById(R.id.add_analytics_button);
        listBtn = findViewById(R.id.list_analytics_button);
        refreshBtn = findViewById(R.id.refresh_button);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);

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
                    Toast.makeText(AnalyticsActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (item.getItemId() == R.id.nav_time_card) {
                    Toast.makeText(AnalyticsActivity.this, "Time Card", Toast.LENGTH_SHORT).show();
                    openTimeCardPage();
                } else if (item.getItemId() == R.id.nav_employees) {
                    Toast.makeText(AnalyticsActivity.this, "Time Card", Toast.LENGTH_SHORT).show();
                    openEmployeesPage();
                } else if (item.getItemId() == R.id.nav_payroll) {
                    Toast.makeText(AnalyticsActivity.this, "Payroll", Toast.LENGTH_SHORT).show();
                    openPayrollPage();
                } else if (item.getItemId() == R.id.nav_live_time_card) {
                    Toast.makeText(AnalyticsActivity.this, "Live Time Card", Toast.LENGTH_SHORT).show();
                    openLiveTimeCardPage();
                } else if (item.getItemId() == R.id.nav_home) {
                    Toast.makeText(AnalyticsActivity.this, "Dashboard", Toast.LENGTH_SHORT).show();
                    openDashBoardPage();
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

        addAnalyticsBtn.setOnClickListener(v -> openAddAnalytics());
        refreshBtn.setOnClickListener(v -> fetchAnalytics());
        fetchAnalytics();
        listBtn.setOnClickListener(v -> openListAnalytics());
    }

    private void fetchAnalytics() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/get-data-company/" + userId);
                Log.d("fetchAnalytics", "Fetching data from " + url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("HTTP error code: " + responseCode);
                }

                StringBuilder response = new StringBuilder();
                try (Scanner scanner = new Scanner(conn.getInputStream())) {
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                }

                Log.d("fetchAnalytics", "Response: " + response.toString());

                JSONArray analytics = new JSONArray(response.toString());

                ArrayList<BarEntry> entries = new ArrayList<>();
                ArrayList<String> labels = new ArrayList<>();
                ArrayList<Integer> colors = new ArrayList<>();

                for (int i = 0; i < analytics.length(); i++) {
                    JSONObject data = analytics.getJSONObject(i);
                    int projectId = data.getInt("id");
                    float income = (float) data.getDouble("income");
                    float expenses = (float) data.getDouble("expenses");
                    float revenue = (float) data.getDouble("revenue");

                    // x = i, y = income/expenses/revenue
                    entries.add(new BarEntry(i * 3f, income));     // shift to make bars not overlap
                    entries.add(new BarEntry(i * 3f + 1, expenses));
                    entries.add(new BarEntry(i * 3f + 2, revenue));

                    labels.add("Project: " + projectId);
                    colors.add(ContextCompat.getColor(this, R.color.income_color));
                    colors.add(ContextCompat.getColor(this, R.color.expenses_color));
                    colors.add(ContextCompat.getColor(this, R.color.revenue_color));
                }

                BarDataSet dataSet = new BarDataSet(entries, "Project Analytics");
                dataSet.setColors(colors);
                dataSet.setDrawValues(true);

                BarData barData = new BarData(dataSet);
                barData.setBarWidth(0.9f);

                runOnUiThread(() -> {
                    barChart.setData(barData);
                    Description desc = new Description();
                    desc.setText("Income, Expenses, Revenue per Project");
                    barChart.setDescription(desc);
                    barChart.setFitBars(true);
                    barChart.invalidate(); // refresh
                });


            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to fetch analytics", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }


    private void openDashBoardPage() {
        Intent dashBoardIntent = new Intent(AnalyticsActivity.this, DashBoardActivity.class);
        dashBoardIntent.putExtra("userId", userId);
        dashBoardIntent.putExtra("firstName", firstName);
        dashBoardIntent.putExtra("lastName", lastName);
        dashBoardIntent.putExtra("email", email);
        dashBoardIntent.putExtra("role", role);
        startActivity(dashBoardIntent);
    }

    private void openProjectsPage() {
        Intent projectIntent = new Intent(AnalyticsActivity.this, ProjectsActivity.class);
        projectIntent.putExtra("userId", userId);
        projectIntent.putExtra("firstName", firstName);
        projectIntent.putExtra("lastName", lastName);
        projectIntent.putExtra("email", email);
        projectIntent.putExtra("role", role);
        startActivity(projectIntent);
    }

    private void openTimeCardPage() {
        Intent timeCardIntent = new Intent(AnalyticsActivity.this, TimeCardActivity.class);
        timeCardIntent.putExtra("userId", userId);
        timeCardIntent.putExtra("firstName", firstName);
        timeCardIntent.putExtra("lastName", lastName);
        timeCardIntent.putExtra("email", email);
        timeCardIntent.putExtra("role", role);
        startActivity(timeCardIntent);
    }

    private void openEmployeesPage() {
        Intent timeCardIntent = new Intent(this, CompanyRosterActivity.class);
        timeCardIntent.putExtra("userId", userId);
        timeCardIntent.putExtra("firstName", firstName);
        timeCardIntent.putExtra("lastName", lastName);
        timeCardIntent.putExtra("email", email);
        timeCardIntent.putExtra("role", role);
        timeCardIntent.putExtra("companyID", companyId);
        startActivity(timeCardIntent);
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

    private void openAddAnalytics() {
        Intent addAnalyticsIntent = new Intent(this, AddAnalyticsActivity.class);
        addAnalyticsIntent.putExtra("userId", userId);
        startActivity(addAnalyticsIntent);
    }

    private void openListAnalytics() {
        Intent listAnalyticsIntent = new Intent(this, ListAnalyticsActivity.class);
        listAnalyticsIntent.putExtra("userId", userId);
        startActivity(listAnalyticsIntent);
    }
}
