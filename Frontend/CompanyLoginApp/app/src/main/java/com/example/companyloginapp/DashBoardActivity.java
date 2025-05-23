package com.example.companyloginapp;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

public class DashBoardActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private int userId, companyId;
    private String firstName, lastName, email, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

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
        TextView helloText = findViewById(R.id.hello_text);
        Button projectsButton = findViewById(R.id.button_projects);
        Button timeCardButton = findViewById(R.id.button_timecard);
        Button employeesButton = findViewById(R.id.button_employees);
        Button scheduleButton = findViewById(R.id.button_schedule);
        Button storeButton = findViewById(R.id.button_store);
        Button taxButton = findViewById(R.id.button_tax); // NEW TAX BUTTON

        // Set welcome message
        helloText.setText("Welcome back, " + firstName + " " + lastName + "!");

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
                    Toast.makeText(DashBoardActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (item.getItemId() == R.id.nav_time_card) {
                    Toast.makeText(DashBoardActivity.this, "Time Card", Toast.LENGTH_SHORT).show();
                    openTimeCardPage();
                } else if (item.getItemId() == R.id.nav_employees) {
                    Toast.makeText(DashBoardActivity.this, "Employees", Toast.LENGTH_SHORT).show();
                    openEmployeesPage();
                } else if (item.getItemId() == R.id.nav_payroll) {
                    Toast.makeText(DashBoardActivity.this, "Payroll", Toast.LENGTH_SHORT).show();
                    openPayrollPage();
                } else if (item.getItemId() == R.id.nav_live_time_card) {
                    Toast.makeText(DashBoardActivity.this, "Live Time Card", Toast.LENGTH_SHORT).show();
                    openLiveTimeCardPage();
                } else if (item.getItemId() == R.id.nav_analytics) {
                    Toast.makeText(DashBoardActivity.this, "Analytics", Toast.LENGTH_SHORT).show();
                    openAnalyticsPage();
                } else if (item.getItemId() == R.id.nav_store) {
                    Toast.makeText(DashBoardActivity.this, "Store", Toast.LENGTH_SHORT).show();
                    openStorePage();
                } else if (item.getItemId() == R.id.nav_tax) {
                    Toast.makeText(DashBoardActivity.this, "W-2 Tax Form", Toast.LENGTH_SHORT).show();
                    openTaxPage();
                } else if (item.getItemId() == R.id.nav_analytic_reports) {
                    Toast.makeText(DashBoardActivity.this, "Analytic Reports", Toast.LENGTH_SHORT).show();
                    openAnalyticReportsPage();
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

        // Set button listeners
        projectsButton.setOnClickListener(v -> openProjectsPage());
        timeCardButton.setOnClickListener(v -> openTimeCardPage());
        employeesButton.setOnClickListener(v -> openEmployeesPage());
        scheduleButton.setOnClickListener(v -> openSchedulePage());
        storeButton.setOnClickListener(v -> openStorePage());
        taxButton.setOnClickListener(v -> openTaxPage()); // TAX BUTTON LISTENER
    }

    private void openProjectsPage() {
        Intent projectIntent = new Intent(DashBoardActivity.this, ProjectsActivity.class);
        projectIntent.putExtra("userId", userId);
        projectIntent.putExtra("firstName", firstName);
        projectIntent.putExtra("lastName", lastName);
        projectIntent.putExtra("email", email);
        projectIntent.putExtra("role", role);
        startActivity(projectIntent);
    }

    private void openTimeCardPage() {
        Intent timeCardIntent = new Intent(DashBoardActivity.this, TimeCardActivity.class);
        timeCardIntent.putExtra("userId", userId);
        timeCardIntent.putExtra("firstName", firstName);
        timeCardIntent.putExtra("lastName", lastName);
        timeCardIntent.putExtra("email", email);
        timeCardIntent.putExtra("role", role);
        startActivity(timeCardIntent);
    }

    private void openSchedulePage() {
        Intent scheduleIntent = new Intent(DashBoardActivity.this, ScheduleActivity.class);
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

    private void openTaxPage() {
        Intent taxIntent = new Intent(this, TaxActivity.class);
        taxIntent.putExtra("userId", userId);
        taxIntent.putExtra("role", role);
        taxIntent.putExtra("companyId", companyId);
        startActivity(taxIntent);
    }

    private void openAnalyticReportsPage() {
        Intent reportsIntent = new Intent(this, AnalyticReportsActivity.class);
        reportsIntent.putExtra("userId", userId);
        reportsIntent.putExtra("firstName", firstName);
        reportsIntent.putExtra("lastName", lastName);
        reportsIntent.putExtra("email", email);
        reportsIntent.putExtra("role", role);
        reportsIntent.putExtra("companyID", companyId);
        startActivity(reportsIntent);
    }
}
