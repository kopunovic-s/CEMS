package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;


import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import com.google.android.material.navigation.NavigationView;

public class CompanyRosterActivity extends AppCompatActivity {

    private ArrayList<String> employeeList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private int userId, companyId;
    private String firstName, lastName, email, role;

    private Button addEmployeeBtn, fetchEmployeeBtn, companyInfoBtn;
    private ListView employeeListView;
    private ActivityResultLauncher<Intent> userDetailLauncher;
    private ActivityResultLauncher<Intent> userAddLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_roster);

        // Get user data from intent
        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        email = intent.getStringExtra("email");
        role = intent.getStringExtra("role");
        companyId = intent.getIntExtra("companyID", -1);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);


        addEmployeeBtn = findViewById(R.id.add_employee_button);
        fetchEmployeeBtn = findViewById(R.id.fetch_employee_button);
        companyInfoBtn = findViewById(R.id.company_info_button);


        employeeListView = findViewById(R.id.employee_list); // Add this line before setAdapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, employeeList);
        employeeListView.setAdapter(adapter);


        // Setup toolbar and drawer
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (Objects.equals(role, "MANAGER") || Objects.equals(role, "EMPLOYEE")) {
                addEmployeeBtn.setVisibility(View.INVISIBLE);
        }

        fetchEmployees();

        fetchEmployeeBtn.setOnClickListener(v -> fetchEmployees());

        addEmployeeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchEmployees();
                openAddEmployee();
            }

        });

        companyInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCompanyDetail();
                fetchEmployees();
            }
        });

        employeeListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = employeeList.get(position);

            // Extract user info and user ID
            String[] parts = selectedItem.split("\\|");
            String userInfo = parts[0].trim();
            String userIdStr = parts.length > 1 ? parts[1].trim() : "";

            Toast.makeText(CompanyRosterActivity.this, "Selected: " + userInfo, Toast.LENGTH_SHORT).show();

            try {
                int userIdInt = Integer.parseInt(userIdStr);
                new AlertDialog.Builder(CompanyRosterActivity.this)
                        .setTitle("Select Action")
                        .setMessage("Do you want to view this employee?")
                        .setPositiveButton("View", (dialog, which) -> openEmployeeDetail(userIdInt))
                        .setNegativeButton("Cancel", null)
                        .show();
            } catch (NumberFormatException e) {
                Toast.makeText(CompanyRosterActivity.this, "Invalid user ID", Toast.LENGTH_SHORT).show();
            }
        });

        employeeListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String[] parts = employeeList.get(position).split("\\|");
            if (parts.length == 2) {
                String employeeIdToDelete = parts[1].trim();
                new AlertDialog.Builder(CompanyRosterActivity.this)
                        .setTitle("Select Action")
                        .setMessage("Do you want to delete this employee?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteEmployee(employeeIdToDelete))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            return true;
        });


        // Navigation item click listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_projects) {
                    openProjectsPage();
                } else if (item.getItemId() == R.id.nav_sign_out) {
                    Toast.makeText(CompanyRosterActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (item.getItemId() == R.id.nav_time_card) {
                    Toast.makeText(CompanyRosterActivity.this, "Time Card", Toast.LENGTH_SHORT).show();
                    openTimeCardPage();
                } else if (item.getItemId() == R.id.nav_employees) {
                    Toast.makeText(CompanyRosterActivity.this, "Employees", Toast.LENGTH_SHORT).show();
                    openEmployeesPage();
                } else if (item.getItemId() == R.id.nav_home) {
                    Toast.makeText(CompanyRosterActivity.this, "Employees", Toast.LENGTH_SHORT).show();
                    openDashBoard();
                } else if (item.getItemId() == R.id.nav_analytic_reports) {
                    Toast.makeText(CompanyRosterActivity.this, "Analytic Reports", Toast.LENGTH_SHORT).show();
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

        userDetailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Refresh the user list
                        fetchEmployees();
                    }
                }
        );

        userAddLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Refresh the user list
                        fetchEmployees();
                    }
                }
        );

    }

    private void openProjectsPage() {
        Intent projectIntent = new Intent(CompanyRosterActivity.this, ProjectsActivity.class);
        projectIntent.putExtra("userId", userId);
        projectIntent.putExtra("firstName", firstName);
        projectIntent.putExtra("lastName", lastName);
        projectIntent.putExtra("email", email);
        projectIntent.putExtra("role", role);
        projectIntent.putExtra("companyID", companyId);
        startActivity(projectIntent);
    }

    private void openTimeCardPage() {
        Intent timeCardIntent = new Intent(CompanyRosterActivity.this, TimeCardActivity.class);
        timeCardIntent.putExtra("userId", userId);
        timeCardIntent.putExtra("firstName", firstName);
        timeCardIntent.putExtra("lastName", lastName);
        timeCardIntent.putExtra("email", email);
        timeCardIntent.putExtra("role", role);
        startActivity(timeCardIntent);
    }

    private void openEmployeesPage() {
        Intent companyRosterIntent = new Intent(CompanyRosterActivity.this, CompanyRosterActivity.class);
        companyRosterIntent.putExtra("userId", userId);
        companyRosterIntent.putExtra("firstName", firstName);
        companyRosterIntent.putExtra("lastName", lastName);
        companyRosterIntent.putExtra("email", email);
        companyRosterIntent.putExtra("role", role);
        companyRosterIntent.putExtra("companyID", companyId);
        startActivity(companyRosterIntent);
    }

    private void fetchEmployees() {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-024.class.las.iastate.edu:8080/Companies/" + companyId + "/Users");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                JSONArray employees = new JSONArray(response.toString());
                employeeList.clear();

                for (int i = 0; i < employees.length(); i++) {
                    JSONObject employee = employees.getJSONObject(i);
                    String fullInfo = employee.getString("firstName") + " " + employee.getString("lastName") + " - " + employee.getString("role") + "\n" + employee.getString("email");
                    String userId = employee.getString("id");
                    employeeList.add(fullInfo + " | " + userId);
                }
                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to fetch employees", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void openAddEmployee() {
        Intent employeeAddIntent = new Intent(this, AddUserActivity.class);
        employeeAddIntent.putExtra("userId", userId);
        employeeAddIntent.putExtra("companyId", companyId);
        userAddLauncher.launch(employeeAddIntent);
    }

    private void openCompanyDetail() {
        Intent companyDetailIntent = new Intent(this, CompanyDetailActivity.class);
        companyDetailIntent.putExtra("companyId", companyId);
        companyDetailIntent.putExtra("userId", userId);
        startActivity(companyDetailIntent);
    }

    private void openEmployeeDetail(int userToSee) {
        Intent employeeDetailIntent = new Intent(this, UserDetailActivity.class);
        employeeDetailIntent.putExtra("userId", userId);
        employeeDetailIntent.putExtra("userIdToSee", userToSee);
        employeeDetailIntent.putExtra("companyId", companyId);
        userDetailLauncher.launch(employeeDetailIntent);
    }

//    private void openAddEmployeeFragment() {
//
//        // Create a new instance of the AddEmployeeFragment
//        AddEmployeeFragment addEmployeeFragment = new AddEmployeeFragment();
//
//        // Create a Bundle to pass data (if necessary)
//        Bundle bundle = new Bundle();
//        bundle.putInt("userId", userId); // Add any necessary data to pass
//        addEmployeeFragment.setArguments(bundle);
//
//        // Begin the fragment transaction, replace the current fragment with AddEmployeeFragment
//        getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fragment_container, addEmployeeFragment) // Container to hold the fragment
//                .addToBackStack(null) // Enables back navigation
//                .commit();
//    }
    private void deleteEmployee(String employeeIdToDelete) {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-024.class.las.iastate.edu:8080/users/" + userId + "/user-delete/" + employeeIdToDelete);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        // Remove the deleted employee from the list based on userId
                        employeeList.removeIf(employee -> employee.contains(employeeIdToDelete));
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "Employee deleted successfully", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to delete employee", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }



    private void openDashBoard() {
        Intent dashBoardIntent = new Intent(this, DashBoardActivity.class);
        dashBoardIntent.putExtra("userId", userId);
        dashBoardIntent.putExtra("firstName", firstName);
        dashBoardIntent.putExtra("lastName", lastName);
        dashBoardIntent.putExtra("email", email);
        dashBoardIntent.putExtra("role", role);
        startActivity(dashBoardIntent);
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
