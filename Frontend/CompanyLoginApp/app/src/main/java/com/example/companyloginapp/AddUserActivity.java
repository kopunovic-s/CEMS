package com.example.companyloginapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.database.Cursor;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddUserActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button addUserButton;
    private EditText editFirstName, editLastName, editEmail, editPassword, editSSN, editHourlyRate, editProfileImage,
            editStreetAddress, editCity, editState, editZipCode, editCountry;
    private Spinner roleSpinner;

    private static final String[] ROLES = {"EXECUTIVE", "OWNER", "MANAGER", "EMPLOYEE"};
    private ArrayAdapter<String> adapter;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private int userId, companyId;

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080/users/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add); // Ensure this XML exists

        // Setup toolbar
        toolbar = findViewById(R.id.toolbar_employee_add);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add User");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI references

        addUserButton = findViewById(R.id.add_employee_button);

        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        editSSN = findViewById(R.id.edit_ssn);
        editHourlyRate = findViewById(R.id.edit_hourly_rate);
        editProfileImage = findViewById(R.id.edit_profile_image);
        editStreetAddress = findViewById(R.id.edit_street_address);
        editCity = findViewById(R.id.edit_city);
        editState = findViewById(R.id.edit_state);
        editZipCode = findViewById(R.id.edit_zip_code);
        editCountry = findViewById(R.id.edit_country);

        roleSpinner = findViewById(R.id.edit_role_spinner);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ROLES);
        roleSpinner.setAdapter(adapter);

        Intent intent = getIntent();
        companyId = intent.getIntExtra("companyId", -1);
        userId = intent.getIntExtra("userId", -1);

        Log.d("AddUserActivity", "userId: " + userId + ", companyId: " + companyId);

        addUserButton.setOnClickListener(v -> submitNewUser());
    }

//    private void openImagePicker() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            selectedImageUri = data.getData();
//            userImageView.setImageURI(selectedImageUri);
//        }
//    }

//    private String getRealPathFromURI(Uri uri) {
//        String[] proj = {MediaStore.Images.Media.DATA};
//        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
//        if (cursor != null) {
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            String path = cursor.getString(column_index);
//            cursor.close();
//            return path;
//        }
//        return null;
//    }

    private void submitNewUser() {

        // Get values from inputs
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString().trim();

        // Validate required fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                JSONObject userJson = new JSONObject();
                userJson.put("firstName", editFirstName.getText().toString());
                userJson.put("lastName", editLastName.getText().toString());
                userJson.put("email", editEmail.getText().toString());
                userJson.put("password", editPassword.getText().toString());
                userJson.put("role", roleSpinner.getSelectedItem().toString());
                userJson.put("ssn", editSSN.getText().toString());
                userJson.put("hourlyRate", editHourlyRate.getText().toString());
                userJson.put("profileImage", editProfileImage.getText().toString());
                userJson.put("streetAddress", editStreetAddress.getText().toString());
                userJson.put("city", editCity.getText().toString());
                userJson.put("state", editState.getText().toString());
                userJson.put("zipCode", editZipCode.getText().toString());
                userJson.put("country", editCountry.getText().toString());

                URL url = new URL(BASE_URL + userId + "/user-add");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = userJson.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(AddUserActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AddUserActivity.this, "Failed to create user", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(AddUserActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

