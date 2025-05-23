package com.example.companyloginapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Scanner;


public class PayrollActivity extends AppCompatActivity {
    // Declare the DrawerLayout, NavigationView, and Toolbar
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RequestQueue queue;
    private int userId, companyId;
    private String firstName, lastName, email, role;
    private String date = "2025-03-24";
    private Button btnRefresh, btnEditWeek;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the activity_payroll layout
        setContentView(R.layout.activity_payroll);

        // Get user data from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        email = intent.getStringExtra("email");
        role = intent.getStringExtra("role");
        companyId = intent.getIntExtra("companyId", -1);



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

        fetchWeekSummary();

        btnEditWeek = findViewById(R.id.btnEditWeek);
        btnEditWeek.setOnClickListener(v -> {
            openCalendar();
        });

        btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(v -> {
            // Fetch and update the week summary when the button is clicked
            fetchWeekSummary();
        });

        // Set a listener for when an item in the NavigationView is selected
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // Called when an item in the NavigationView is selected.
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle the selected item based on its ID
                if (item.getItemId() == R.id.nav_home) {
                    // Show a Toast message for the Account item
                    Toast.makeText(PayrollActivity.this,
                            "Home", Toast.LENGTH_SHORT).show();
                    openDashBoard();
                }

                if (item.getItemId() == R.id.nav_employees) {
                    // Show a Toast message for the Settings item
                    Toast.makeText(PayrollActivity.this,
                            "Employees", Toast.LENGTH_SHORT).show();
                    openEmployees();
                }

                if (item.getItemId() == R.id.nav_time_card) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(PayrollActivity.this,
                            "Time Card", Toast.LENGTH_SHORT).show();
                    openTimeCardPage();
                }

                if (item.getItemId() == R.id.nav_project_management) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(PayrollActivity.this,
                            "Project Management", Toast.LENGTH_SHORT).show();
                    openProjectsPage();
                }

                if (item.getItemId() == R.id.nav_tasks) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(PayrollActivity.this,
                            "Tasks", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.nav_analytics) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(PayrollActivity.this,
                            "Analytics", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.nav_payroll) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(PayrollActivity.this,
                            "Payroll", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.nav_sign_out) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(PayrollActivity.this,
                            "Sign Out", Toast.LENGTH_SHORT).show();
                }

                if (item.getItemId() == R.id.nav_live_time_card) {
                    // Show a Toast message for the Logout item
                    Toast.makeText(PayrollActivity.this,
                            "Live Time Card", Toast.LENGTH_SHORT).show();
                    openLiveTimeCardPage();
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

    private void fetchWeekSummary() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://coms-3090-024.class.las.iastate.edu:8080/payroll/" + userId + "/weekSummary/" + date); // You need to construct the URL properly
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) {
                        response.append(scanner.nextLine());
                    }
                    scanner.close();

                    JSONObject weekSummary = new JSONObject(response.toString());
                    int weekNumber = weekSummary.getInt("weekNumber");
                    int year = weekSummary.getInt("year");
                    boolean isPaid = weekSummary.getBoolean("isPaid");
                    JSONArray employees = weekSummary.getJSONArray("employees");

                    runOnUiThread(() -> {
                        try {
                            updatePayrollTable(employees);
                            updateWeekStatus(weekNumber, year);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(PayrollActivity.this, "Error parsing employee data", Toast.LENGTH_SHORT).show();
                        }
                    });


                    // Update UI if needed using runOnUiThread
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(PayrollActivity.this, "Failed to fetch employees", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private void updatePayrollTable (JSONArray employees) throws JSONException {
        TableLayout tableLayout = findViewById(R.id.payroll_table);
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        for (int i = 0; i < employees.length(); i++) {
            JSONObject employee = employees.getJSONObject(i);
            JSONObject user = employee.getJSONObject("user");

            String employeeFirstName = user.getString("firstName");
            String employeeLastName = user.getString("lastName");
            int employeeId = user.getInt("id");
            String employeeRole = user.getString("role");

            double totalHours = employee.getDouble("totalHours");
            double hourlyRate = employee.isNull("hourlyRate") ? 0.0 : employee.getDouble("hourlyRate");
            double totalPay = employee.getDouble("totalPay");

            TableRow row = new TableRow(tableLayout.getContext());

            // Create and add TextViews
            row.addView(createTextView(String.valueOf(employeeId)));
            row.addView(createTextView(employeeFirstName + " " + employeeLastName));
            row.addView(createTextView(String.format("$%.2f", hourlyRate)));
            row.addView(createTextView(String.valueOf(totalHours)));
            row.addView(createTextView(String.format("$%.2f", totalPay)));

            row.setTag(employeeId); // You can tag with the employee ID or any unique identifier
            row.setClickable(true);
            row.setOnClickListener(v -> {
                Intent intent = new Intent(PayrollActivity.this, PayrollDetailActivity.class);
                intent.putExtra("employeeId", employeeId);
                intent.putExtra("userId", userId);
                intent.putExtra("date", date);
//                intent.putExtra("firstName", firstName);
//                intent.putExtra("lastName", lastName);
//                intent.putExtra("role", role);
//                intent.putExtra("totalHours", totalHours);
//                intent.putExtra("hourlyRate", hourlyRate);
//                intent.putExtra("totalPay", totalPay);
                startActivity(intent);
            });

            // Add the row to the TableLayout
            tableLayout.addView(row);

        }
    }

    private void updateWeekStatus(int weekNumber, int year) {
        TextView weekStatusTextView = findViewById(R.id.weekStatus);
        weekStatusTextView.setText("Week Number: " + weekNumber + "    Year: " + year);
    }



    // Helper method to create a styled TextView
    private TextView createTextView(String text) {
        TextView textView = new TextView(this); // or getActivity() if in Fragment
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        return textView;
    }

    private void openCalendar() {

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String formattedMonth = String.format("%02d", month + 1);
                String formattedDay = String.format("%02d", dayOfMonth);
                date = String.valueOf(year) + "-" + formattedMonth + "-" + formattedDay;
                fetchWeekSummary();
            }
        }, 2025, 3, 24); // Pass initial date values to the dialog
        dialog.show(); // Don't forget to show the dialog
    }


    private void openDashBoard() {
        Intent dashBoardIntent = new Intent(PayrollActivity.this, DashBoardActivity.class);
        dashBoardIntent.putExtra("userId", userId);
        dashBoardIntent.putExtra("firstName", firstName);
        dashBoardIntent.putExtra("lastName", lastName);
        dashBoardIntent.putExtra("email", email);
        dashBoardIntent.putExtra("role", role);
        startActivity(dashBoardIntent);
    }

    private void openEmployees() {
        Intent employeeIntent = new Intent(PayrollActivity.this, CompanyRosterActivity.class);
        employeeIntent.putExtra("userId", userId);
        employeeIntent.putExtra("firstName", firstName);
        employeeIntent.putExtra("lastName", lastName);
        employeeIntent.putExtra("email", email);
        employeeIntent.putExtra("role", role);
        startActivity(employeeIntent);
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
}