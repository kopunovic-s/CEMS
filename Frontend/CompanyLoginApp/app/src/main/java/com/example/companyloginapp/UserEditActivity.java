package com.example.companyloginapp;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserEditActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView userImageView;
    private Button uploadImageButton, updateUserButton;
    private EditText editId, editFirstName, editLastName, editEmail, editPassword, editSSN, editHourlyRate, editCompanyName, editCompanyId, editProfileImage, editStreetAddress, editCity, editState, editZipCode, editCountry;
    private Spinner roleSpinner;
    private static final String[] ROLES = {"EXECUTIVE", "OWNER", "MANAGER", "EMPLOYEE"};
    private int userId, companyId, targetUserId;
    private static final int PICK_IMAGE_REQUEST = 1;
    private final String GET_URL = "http://coms-3090-024.class.las.iastate.edu:8080/users/";
    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080/Companies/";
    private ArrayAdapter<String> adapter;
    private String existingProfileImageBase64 = null;
    private boolean imageUploaded = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_edit); // Make sure this matches your actual XML file name

        // Initialize views
        toolbar = findViewById(R.id.toolbar_user_edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit User");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        companyId = intent.getIntExtra("companyId", -1);
        targetUserId = intent.getIntExtra("userIdToSee", -1);

        userImageView = findViewById(R.id.user_image_view);
        uploadImageButton = findViewById(R.id.upload_image_button);
        updateUserButton = findViewById(R.id.update_user_button);

        uploadImageButton.setOnClickListener(v -> openImagePicker());


        userImageView = findViewById(R.id.user_image_view);
        // EditText fields
        editId = findViewById(R.id.edit_id);
        editFirstName = findViewById(R.id.edit_first_name);
        editLastName = findViewById(R.id.edit_last_name);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        editSSN = findViewById(R.id.edit_ssn);
        editHourlyRate = findViewById(R.id.edit_hourly_rate);
        editCompanyName = findViewById(R.id.edit_company_name);
        editCompanyId = findViewById(R.id.edit_company_id);
        //editProfileImage = findViewById(R.id.edit_profile_image);
        editStreetAddress = findViewById(R.id.edit_street_address);
        editCity = findViewById(R.id.edit_city);
        editState = findViewById(R.id.edit_state);
        editZipCode = findViewById(R.id.edit_zip_code);
        editCountry = findViewById(R.id.edit_country);

        roleSpinner = findViewById(R.id.edit_role_spinner);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, ROLES);
        roleSpinner.setAdapter(adapter);


        fetchUserDetails();

        // Upload Image button click
        uploadImageButton.setOnClickListener(v -> {
            openImagePicker();
        });

        // Update Company button click
        updateUserButton.setOnClickListener(v -> updateUserDetails());

    }

    private void uploadImageToServer(File imageFile, int userId, int companyId) {
        OkHttpClient client = new OkHttpClient();

        // Create request body for the image file
        RequestBody fileBody = RequestBody.create(imageFile, MediaType.parse("image/png"));

        // Full URL
        String url = "http://coms-3090-024.class.las.iastate.edu:8080/images/company/" + userId + "/" + companyId;

        // Build the request
        Request request = new Request.Builder()
                .url(url)
                .put(fileBody)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UserEditActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(UserEditActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        imageUploaded = true;
                    } else {
                        Toast.makeText(UserEditActivity.this, "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            userImageView.setImageURI(imageUri); // Optional: Show preview

            // Convert URI to File
            File file = new File(getRealPathFromURI(imageUri)); // Below
            uploadImageToServer(file, userId, companyId);
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    private void fetchUserDetails() {
        new Thread(() -> {
            try {
                URL url = new URL(GET_URL + userId + "/" + targetUserId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                JSONObject user = new JSONObject(response.toString());

                runOnUiThread(() -> {
                    try {
                        editId.setText(String.valueOf(user.getInt("id")));
                        editFirstName.setText(user.getString("firstName"));
                        editLastName.setText(user.getString("lastName"));
                        editEmail.setText(user.getString("email"));
                        editPassword.setText(user.getString("password"));
                        String role = user.getString("role");
                        int spinnerPosition = adapter.getPosition(role);
                        roleSpinner.setSelection(spinnerPosition);
                        editSSN.setText(user.getString("ssn"));
                        if (user.has("hourlyRate") && !user.isNull("hourlyRate")) {
                            double hourlyRate = user.getDouble("hourlyRate");
                            editHourlyRate.setText(String.format("%.2f", hourlyRate));
                        } else {
                            editHourlyRate.setText("0.0"); // or "0.00" or whatever default you prefer
                        }
                        editCompanyName.setText(user.getString("companyName"));
                        editCompanyId.setText(String.valueOf(user.getInt("companyId")));
                       // editProfileImage.setText(user.getString("profileImage"));
                        editStreetAddress.setText(user.getString("streetAddress"));
                        editCity.setText(user.getString("city"));
                        editState.setText(user.getString("state"));
                        editZipCode.setText(user.getString("zipCode"));
                        editCountry.setText(user.getString("country"));
                        if (user.has("profileImage") && !user.isNull("profileImage")) {
                            existingProfileImageBase64 = user.getString("profileImage");
                        }
                        if (existingProfileImageBase64 != null) {
                            loadBase64ImageIntoImageView(existingProfileImageBase64, userImageView);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(UserEditActivity.this, "Error parsing user data", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UserEditActivity.this, "Failed to load user details", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateUserDetails() {
        new Thread(() -> {
            try {

                JSONObject userJson = new JSONObject();
                userJson.put("id", Integer.parseInt(editId.getText().toString()));
                userJson.put("firstName", editFirstName.getText().toString());
                userJson.put("lastName", editLastName.getText().toString());
                userJson.put("role", roleSpinner.getSelectedItem().toString());
                userJson.put("email", editEmail.getText().toString());
                userJson.put("password", editPassword.getText().toString());
                userJson.put("role", roleSpinner.getSelectedItem().toString());
                userJson.put("ssn", editSSN.getText().toString());
                userJson.put("hourlyRate", Double.parseDouble(editHourlyRate.getText().toString()));
                Log.d("updateUser", "Parsed hourly rate: " + editHourlyRate.getText().toString());
                userJson.put("streetAddress", editStreetAddress.getText().toString());
                userJson.put("city", editCity.getText().toString());
                userJson.put("state", editState.getText().toString());
                userJson.put("zipCode", editZipCode.getText().toString());
                userJson.put("country", editCountry.getText().toString());
                userJson.put("companyName", editCompanyName.getText().toString());
                userJson.put("companyId", editCompanyId.getText().toString());
                if (!imageUploaded && existingProfileImageBase64 != null) {
                    userJson.put("profileImage", existingProfileImageBase64);
                }

                URL url = new URL(GET_URL + userId + "/update-info/" + targetUserId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = userJson.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        setResult(RESULT_OK);
                        Toast.makeText(UserEditActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                } else {
                    Scanner errorScanner = new Scanner(conn.getErrorStream()).useDelimiter("\\A");
                    String errorBody = errorScanner.hasNext() ? errorScanner.next() : "No error body";
                    Log.e("updateUserDetails", "Error " + responseCode + ": " + errorBody);
                    runOnUiThread(() -> Toast.makeText(UserEditActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(UserEditActivity.this, "Update error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void loadBase64ImageIntoImageView(String base64String, ImageView imageView) {
        try {
            if (base64String == null || base64String.isEmpty()) {
                imageView.setImageResource(R.drawable.user_placeholder); // ✅ fallback
                return;
            }

            byte[] decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                imageView.setImageResource(R.drawable.user_placeholder); // ✅ fallback on null bitmap
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageView.setImageResource(R.drawable.user_placeholder); // ✅ fallback on error
            Toast.makeText(this, "Failed to load image, using placeholder", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

