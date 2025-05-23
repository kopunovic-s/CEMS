package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;


import org.java_websocket.handshake.ServerHandshake;


public class LiveTimeCardActivity extends AppCompatActivity implements WebSocketListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private int userId, companyId;
    private String firstName, lastName, email, role;
    private Button clockInBtn, clockOutBtn;
    private TextView live_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_time_card);

        String server = "ws://coms-3090-024.class.las.iastate.edu:8080/clockEvents/";

        // Get user data from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        email = intent.getStringExtra("email");
        role = intent.getStringExtra("role");
        companyId = intent.getIntExtra("companyId", -1);

        Log.d("LiveTimeCardActivity", "onCreate: User Data - userId: " + userId + ", companyId: " + companyId);


        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);

        //Initialize buttons
        clockInBtn = findViewById(R.id.btnClockIn);
        clockOutBtn = findViewById(R.id.btnClockOut);

        //Initialize text view
        live_tv = findViewById(R.id.live_time_card_tv);

        String serverUrl = server + companyId + "/" + userId;
        Log.d("LiveTimeCardActivity", "onCreate: WebSocket URL: " + serverUrl);
        LiveTimeCardWebSocketManager.getInstance().connectWebSocket(serverUrl);
        LiveTimeCardWebSocketManager.getInstance().setWebSocketListener(LiveTimeCardActivity.this);

        clockOutBtn.setVisibility(View.INVISIBLE);

        clockInBtn.setOnClickListener(v -> {
            Log.d("LiveTimeCardActivity", "clockInBtn clicked");
            try {
                // send message
                String clockInMsg = firstName + " " + lastName + ":CLOCK_IN";
                LiveTimeCardWebSocketManager.getInstance().sendMessage((clockInMsg));
                Log.d("LiveTimeCardActivity", "Sending clock-in message: " + clockInMsg);
                clockInBtn.setVisibility(View.INVISIBLE);
                clockOutBtn.setVisibility(View.VISIBLE);
                Toast.makeText(LiveTimeCardActivity.this, "Clocked In", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("ExceptionSendMessage:", e.getMessage().toString());
            }

        });

        clockOutBtn.setOnClickListener(v -> {
            String clockOutMsg = firstName + " " + lastName + ":CLOCK_OUT";
            LiveTimeCardWebSocketManager.getInstance().sendMessage((clockOutMsg));
            clockInBtn.setVisibility(View.VISIBLE);
            clockOutBtn.setVisibility(View.INVISIBLE);
            Toast.makeText(LiveTimeCardActivity.this, "Clocked Out", Toast.LENGTH_SHORT).show();
        });

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
                    Toast.makeText(LiveTimeCardActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (item.getItemId() == R.id.nav_time_card) {
                    Toast.makeText(LiveTimeCardActivity.this, "Time Card", Toast.LENGTH_SHORT).show();
                    openTimeCardPage();
                } else if (item.getItemId() == R.id.nav_employees) {
                    Toast.makeText(LiveTimeCardActivity.this, "Time Card", Toast.LENGTH_SHORT).show();
                    openEmployeesPage();
                } else if (item.getItemId() == R.id.nav_payroll) {
                    Toast.makeText(LiveTimeCardActivity.this, "Payroll", Toast.LENGTH_SHORT).show();
                    openPayrollPage();
                } else if (item.getItemId() == R.id.nav_home) {
                    Toast.makeText(LiveTimeCardActivity.this, "Dashboard", Toast.LENGTH_SHORT).show();
                    openDashBoard();
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
    }

    @Override
    public void onWebSocketMessage(String message) {
        Log.d("LiveTimeCardActivity", "Received WebSocket message: " + message);
        runOnUiThread(() -> {
            String s = live_tv.getText().toString();
            live_tv.setText(s + "\n"+message);
        });
    }

    @Override
    public void onWebSocketClose(int code, String reason, boolean remote) {
        String closedBy = remote ? "server" : "local";
        runOnUiThread(() -> {
            String s = live_tv.getText().toString();
            live_tv.setText(s + "---\nconnection closed by " + closedBy + "\nreason: " + reason);
        });
    }

    @Override
    public void onWebSocketOpen(ServerHandshake handshakedata) {
        runOnUiThread(() -> {
            String s = live_tv.getText().toString();
            live_tv.setText("WebSocket opened");
        });
    }

    @Override
    public void onWebSocketError(Exception ex) {}

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

    private void openDashBoard() {
        Intent dashBoardIntent = new Intent(this, DashBoardActivity.class);
        dashBoardIntent.putExtra("userId", userId);
        dashBoardIntent.putExtra("firstName", firstName);
        dashBoardIntent.putExtra("lastName", lastName);
        dashBoardIntent.putExtra("email", email);
        dashBoardIntent.putExtra("role", role);
        dashBoardIntent.putExtra("companyID", companyId);
        startActivity(dashBoardIntent);
    }
}