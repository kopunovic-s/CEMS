package com.example.companyloginapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import android.widget.Button;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.material.navigation.NavigationView;

import java.time.LocalDateTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class TimeCardActivity extends AppCompatActivity {
    // Declare the DrawerLayout, NavigationView, and Toolbar
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private Button clockInButton;
    private Button clockOutButton;
    private TextView checkInStatusText;
    private TextView latestTimeCard;
    private RequestQueue queue;
    private int userId;
    private String firstName, lastName, email, role;
    private static final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the activity_main layout
        setContentView(R.layout.activity_time_card);

        // Get user data from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        email = intent.getStringExtra("email");
        role = intent.getStringExtra("role");

        checkInStatusText = findViewById(R.id.check_in_label);
        latestTimeCard = findViewById(R.id.latestTimeCard);

        clockInButton = findViewById(R.id.btnClockIn);
        clockOutButton = findViewById(R.id.btnClockOut);

        clockInButton.setVisibility(View.VISIBLE);
        clockOutButton.setVisibility(View.INVISIBLE);


        // Initialize the DrawerLayout, Toolbar, and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);

        // Create an ActionBarDrawerToggle to handle
        // the drawer's open/close state
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);

        // Add the toggle as a listener to the DrawerLayout
        drawerLayout.addDrawerListener(toggle);

        // Synchronize the toggle's state with the linked DrawerLayout
        toggle.syncState();

        queue = Volley.newRequestQueue(this);

        clockInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(TimeCardActivity.this,
//                        "Clocked In!", Toast.LENGTH_SHORT).show();
//                clockInButton.setVisibility(View.INVISIBLE);
//                clockOutButton.setVisibility(View.VISIBLE);
                clockIn();
//                checkInStatusText.setText("You are checked in");
            }
        });

        clockOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(TimeCardActivity.this,
//                        "Clocked Out!", Toast.LENGTH_SHORT).show();
//                clockInButton.setVisibility(View.VISIBLE);
//                clockOutButton.setVisibility(View.INVISIBLE);
                clockOut();
//                checkInStatusText.setText("You are not checked in");
            }
        });


        // Set a listener for when an item in the NavigationView is selected
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // Called when an item in the NavigationView is selected.
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle the selected item based on its ID
                if (item.getItemId() == R.id.nav_home) {
                    // Show a Toast message for the Account item
                    Toast.makeText(TimeCardActivity.this,
                            "Home", Toast.LENGTH_SHORT).show();
                    openDashBoard();
                }

                if (item.getItemId() == R.id.nav_employees) {
                    // Show a Toast message for the Settings item
                    Toast.makeText(TimeCardActivity.this,
                            "Employees", Toast.LENGTH_SHORT).show();
                    openEmployees();
                }

                if (item.getItemId() == R.id.nav_time_card) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(TimeCardActivity.this,
                            "Time Card", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.nav_project_management) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(TimeCardActivity.this,
                            "Project Management", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.nav_tasks) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(TimeCardActivity.this,
                            "Tasks", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.nav_analytics) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(TimeCardActivity.this,
                            "Analytics", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.nav_payroll) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(TimeCardActivity.this,
                            "Payroll", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.nav_sign_out) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(TimeCardActivity.this,
                            "Sign Out", Toast.LENGTH_SHORT).show();
                }

                // Close the drawer after selection
                drawerLayout.closeDrawers();
                // Indicate that the item selection has been handled
                return true;
            }
        });

        // Add a callback to handle the back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            // Called when the back button is pressed.
            @Override
            public void handleOnBackPressed() {
                // Check if the drawer is open
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    // Close the drawer if it's open
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Finish the activity if the drawer is closed
                    finish();
                }
            }
        });
    }

    private void clockIn() {
        LocalDateTime clockIn = LocalDateTime.now();  // Get current local date and time
        String url = BASE_URL + "users/timecards/clockIn/" + userId;  // Adjust as per backend API

        // Create a JSON object with required fields
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("clockIn", clockIn.toString());  // Convert LocalDateTime to string for JSON
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Create a RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a JsonObjectRequest for POST request
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        clockInButton.setVisibility(View.VISIBLE);
                        clockOutButton.setVisibility(View.INVISIBLE);
                        checkInStatusText.setText("You are checked in");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        clockOutButton.setVisibility(View.VISIBLE);
                        clockInButton.setVisibility(View.INVISIBLE);
                        checkInStatusText.setText("You are checked in");
                    }
                });

        // Add the request to the queue
        queue.add(jsonRequest);
    }






    // Clock Out Function
    private void clockOut() {
        LocalDateTime clockOut = LocalDateTime.now();
        // URL for the clock-out API endpoint
        String url = BASE_URL + "users/timecards/clockOut/" + userId;  // Adjust as per backend API

        // Create a JSONObject to send the necessary data (e.g., user ID, and clock-out timestamp)
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("clockOut", clockOut.toString());  // Convert LocalDateTime to string for JSON
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Create a RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a JsonObjectRequest for POST request
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        clockInButton.setVisibility(View.VISIBLE);
                        clockOutButton.setVisibility(View.INVISIBLE);
                        checkInStatusText.setText("You are not checked in");
                        fetchLatestTimeCard();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        clockInButton.setVisibility(View.VISIBLE);
                        clockOutButton.setVisibility(View.INVISIBLE);
                        checkInStatusText.setText("You are not checked in");
                        fetchLatestTimeCard();
                    }
                });

        // Add the request to the queue
        queue.add(jsonRequest);
    }

    private void fetchLatestTimeCard() {
        String url = BASE_URL + "users/timecards/latest/" + userId; // Ensure LATEST is properly defined with the correct user ID

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String latestClockIn = response.getString("clockIn"); // Adjust key names based on API response
                            String latestClockOut = response.optString("clockOut", "Still Checked In"); // Handle null values
                            Double hoursWorked = response.optDouble("hoursWorked");

                            String displayText = "Clock In: " + latestClockIn + "\nClock Out: " + latestClockOut + "\nHours Worked: " + hoursWorked;
                            latestTimeCard.setText(displayText);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            latestTimeCard.setText("Error retrieving time card data.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        latestTimeCard.setText("Failed to fetch latest time card.");
                    }
                });

        // Add the request to the Volley queue
        queue.add(jsonRequest);
    }


    private void openDashBoard() {
        Intent dashBoardIntent = new Intent(TimeCardActivity.this, DashBoardActivity.class);
        dashBoardIntent.putExtra("userId", userId);
        dashBoardIntent.putExtra("firstName", firstName);
        dashBoardIntent.putExtra("lastName", lastName);
        dashBoardIntent.putExtra("email", email);
        dashBoardIntent.putExtra("role", role);
        startActivity(dashBoardIntent);
    }

    private void openEmployees() {
        Intent employeeIntent = new Intent(TimeCardActivity.this, CompanyRosterActivity.class);
        employeeIntent.putExtra("userId", userId);
        employeeIntent.putExtra("firstName", firstName);
        employeeIntent.putExtra("lastName", lastName);
        employeeIntent.putExtra("email", email);
        employeeIntent.putExtra("role", role);
        startActivity(employeeIntent);
    }
}

