package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class StoreActivity extends AppCompatActivity {

    private int userId, companyId;
    private String firstName, lastName, email, role;
    private ListView departmentListView;
    private Button addDepartmentButton;
    private DepartmentAdapter adapter;
    private List<JSONObject> departmentList = new ArrayList<>();

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        companyId = intent.getIntExtra("companyID", -1);
        firstName = intent.getStringExtra("firstName");
        lastName = intent.getStringExtra("lastName");
        email = intent.getStringExtra("email");
        role = intent.getStringExtra("role");

        Toolbar toolbar = findViewById(R.id.toolbar_store);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Company Store");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        departmentListView = findViewById(R.id.department_list);
        addDepartmentButton = findViewById(R.id.add_department_button);

        adapter = new DepartmentAdapter(
                this,
                departmentList,
                this::deleteDepartment,
                this::openDepartmentProducts
        );
        departmentListView.setAdapter(adapter);

        addDepartmentButton.setOnClickListener(v -> showAddDepartmentDialog());

        fetchDepartments();
    }

    private void fetchDepartments() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/department/get-departments/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                Log.e("FETCH_DEPTS", "Response code: " + responseCode);

                if (responseCode == 200) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) response.append(scanner.nextLine());
                    scanner.close();

                    JSONArray depts = new JSONArray(response.toString());
                    departmentList.clear();

                    for (int i = 0; i < depts.length(); i++) {
                        departmentList.add(depts.getJSONObject(i));
                    }

                    runOnUiThread(() -> adapter.notifyDataSetChanged());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to fetch departments", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error loading departments", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showAddDepartmentDialog() {
        EditText input = new EditText(this);
        input.setHint("Department Name");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Add Department")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        createDepartment(name);
                    } else {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createDepartment(String name) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/department/create-department/" + userId + "/" + name);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                int responseCode = conn.getResponseCode();
                Log.e("CREATE_DEPT", "Response code: " + responseCode);

                if (responseCode == 200 || responseCode == 201) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Department created", Toast.LENGTH_SHORT).show();
                        fetchDepartments();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to create department", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error creating department", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void deleteDepartment(int departmentId) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/department/remove-department/" + departmentId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Department deleted", Toast.LENGTH_SHORT).show();
                        fetchDepartments();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to delete department", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error deleting department", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void openDepartmentProducts(int departmentId, String departmentName) {
        Intent i = new Intent(this, ProductActivity.class);
        i.putExtra("departmentId", departmentId);
        i.putExtra("departmentName", departmentName);
        i.putExtra("userId", userId);
        i.putExtra("companyID", companyId);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // back arrow
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
