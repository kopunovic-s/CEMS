package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;

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


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONObject;

public class CompanyEditActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView companyImageView;
    private Button uploadImageButton;
    private EditText editId, editName, editEIN, editStreetAddress, editCity, editState, editZipCode, editCountry;
    private Button updateCompanyButton, deleteCompanyButton;
    private int userId, companyId;
    private static final int PICK_IMAGE_REQUEST = 1;
    private final String GET_URL = "http://coms-3090-024.class.las.iastate.edu:8080/Companies/";
    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080/Companies/";
    private String existingCompanyLogoBase64 = null;
    private boolean imageUploaded = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_edit); // Make sure this matches your actual XML file name

        // Initialize views
        toolbar = findViewById(R.id.toolbar_company_edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Company");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        companyId = intent.getIntExtra("companyId", -1);

        companyImageView = findViewById(R.id.company_image_view);
        uploadImageButton = findViewById(R.id.upload_image_button);

        uploadImageButton.setOnClickListener(v -> openImagePicker());


        companyImageView = findViewById(R.id.company_image_view);
        uploadImageButton = findViewById(R.id.upload_image_button);
        editId = findViewById(R.id.edit_id);
        editName = findViewById(R.id.edit_name);
        editEIN = findViewById(R.id.edit_ein);
        editStreetAddress = findViewById(R.id.edit_street_address);
        editCity = findViewById(R.id.edit_city);
        editState = findViewById(R.id.edit_state);
        editZipCode = findViewById(R.id.edit_zip_code);
        editCountry = findViewById(R.id.edit_country);
        updateCompanyButton = findViewById(R.id.update_company_button);
        deleteCompanyButton = findViewById(R.id.delete_company_button);

        fetchCompanyDetails();

        // Upload Image button click
        uploadImageButton.setOnClickListener(v -> {
            openImagePicker();
        });

        // Update Company button click
        updateCompanyButton.setOnClickListener(v -> {
            updateCompanyDetails();
        });

        // Delete Company button click
        deleteCompanyButton.setOnClickListener(v -> {
           deleteCompanyDetails();
        });
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
                runOnUiThread(() -> Toast.makeText(CompanyEditActivity.this, "Upload failed", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(CompanyEditActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        imageUploaded = true;
                    } else {
                        Toast.makeText(CompanyEditActivity.this, "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show();
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
            companyImageView.setImageURI(imageUri); // Optional: Show preview

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

    private void fetchCompanyDetails() {
        new Thread(() -> {
            try {
                URL url = new URL(GET_URL + companyId);
                Log.d("fetchCompanyDetails", "Requesting URL: " + url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                JSONObject company = new JSONObject(response.toString());
                int id = company.getInt("id");
                String name = company.getString("name");
                String ein = company.getString("ein");
                String streetAddress = company.getString("streetAddress");
                String city = company.getString("city");
                String state = company.getString("state");
                String zipCode = company.getString("zipCode");
                String country = company.getString("country");
                if (company.has("companyLogo") && !company.isNull("companyLogo")) {
                    existingCompanyLogoBase64 = company.getString("companyLogo");
                }


                // Update UI components on the main thread
                runOnUiThread(() -> {
                    editId.setText(String.valueOf(id));
                    editName.setText(name);
                    editEIN.setText(ein);
                    editStreetAddress.setText(streetAddress);
                    editCity.setText(city);
                    editState.setText(state);
                    editZipCode.setText(zipCode);
                    editCountry.setText(country);
                    if (existingCompanyLogoBase64 != null) {
                        loadBase64ImageIntoImageView(existingCompanyLogoBase64, companyImageView);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(CompanyEditActivity.this, "Failed to load company details", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateCompanyDetails() {
        new Thread(() -> {
            try {
                // Get data from EditText fields
                int id = Integer.parseInt(editId.getText().toString());
                String name = editName.getText().toString();
                String ein = editEIN.getText().toString();
                String streetAddress = editStreetAddress.getText().toString();
                String city = editCity.getText().toString();
                String state = editState.getText().toString();
                String zipCode = editZipCode.getText().toString();
                String country = editCountry.getText().toString();

                Log.d("updateCompanyDetails", "Collected input data: " +
                        "id=" + id + ", name=" + name + ", ein=" + ein + ", address=" + streetAddress +
                        ", city=" + city + ", state=" + state + ", zip=" + zipCode + ", country=" + country);

                if (name.isEmpty() || ein.isEmpty() || streetAddress.isEmpty() || city.isEmpty() || state.isEmpty() || zipCode.isEmpty() || country.isEmpty()) {
                    Log.w("updateCompanyDetails", "Validation failed: One or more fields are empty");
                    runOnUiThread(() -> Toast.makeText(CompanyEditActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Create JSON Object
                JSONObject companyJson = new JSONObject();
                companyJson.put("id", id);
                companyJson.put("name", name);
                companyJson.put("ein", ein);
                companyJson.put("streetAddress", streetAddress);
                companyJson.put("city", city);
                companyJson.put("state", state);
                companyJson.put("zipCode", zipCode);
                companyJson.put("country", country);
                if (!imageUploaded) {
                    companyJson.put("companyLogo", existingCompanyLogoBase64);
                }

                Log.d("updateCompanyDetails", "Created JSON: " + companyJson.toString());

                // Set up the connection to send the update request
                URL url = new URL(BASE_URL + companyId);
                Log.d("updateCompanyDetails", "Requesting URL: " + url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Send the request
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = companyJson.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                    Log.d("updateCompanyDetails", "Request body sent successfully");
                }

                // Check the response
                int responseCode = conn.getResponseCode();
                Log.d("updateCompanyDetails", "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(CompanyEditActivity.this, "Company details updated successfully", Toast.LENGTH_SHORT).show());
                    setResult(RESULT_OK);
                    finish();
                } else {
                    // Read the error stream if available
                    Scanner errorScanner = new Scanner(conn.getErrorStream()).useDelimiter("\\A");
                    String errorBody = errorScanner.hasNext() ? errorScanner.next() : "No error body";

                    Log.e("updateCompanyDetails", "Failed to update. Code: " + responseCode + ", Error: " + errorBody);

                    runOnUiThread(() -> Toast.makeText(CompanyEditActivity.this,
                            "Failed to update company details. Server responded with code: " + responseCode,
                            Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                Log.e("updateCompanyDetails", "Exception occurred", e);
                runOnUiThread(() -> Toast.makeText(CompanyEditActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void deleteCompanyDetails() {
        new Thread(() -> {
            try {

                int id = Integer.parseInt(editId.getText().toString());
                String name = "";
                String ein = "";
                String streetAddress = "";
                String city = "";
                String state = "";
                String zipCode = "";
                String country = "";

                Log.d("updateCompanyDetails", "Collected input data: " +
                        "id=" + id + ", name=" + name + ", ein=" + ein + ", address=" + streetAddress +
                        ", city=" + city + ", state=" + state + ", zip=" + zipCode + ", country=" + country);



                // Create JSON Object
                JSONObject companyJson = new JSONObject();
                companyJson.put("id", id);
                companyJson.put("name", name);
                companyJson.put("ein", ein);
                companyJson.put("streetAddress", streetAddress);
                companyJson.put("city", city);
                companyJson.put("state", state);
                companyJson.put("zipCode", zipCode);
                companyJson.put("country", country);

                Log.d("updateCompanyDetails", "Created JSON: " + companyJson.toString());

                // Set up the connection to send the update request
                URL url = new URL(BASE_URL + companyId);
                Log.d("updateCompanyDetails", "Requesting URL: " + url.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Send the request
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = companyJson.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                    Log.d("deleteCompanyDetails", "Request body sent successfully");
                }

                // Check the response
                int responseCode = conn.getResponseCode();
                Log.d("deleteCompanyDetails", "Response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> Toast.makeText(CompanyEditActivity.this, "Company details deleted successfully", Toast.LENGTH_SHORT).show());
                    setResult(RESULT_OK);
                    finish();
                } else {
                    // Read the error stream if available
                    Scanner errorScanner = new Scanner(conn.getErrorStream()).useDelimiter("\\A");
                    String errorBody = errorScanner.hasNext() ? errorScanner.next() : "No error body";

                    Log.e("updateCompanyDetails", "Failed to delete. Code: " + responseCode + ", Error: " + errorBody);

                    runOnUiThread(() -> Toast.makeText(CompanyEditActivity.this,
                            "Failed to delete company details. Server responded with code: " + responseCode,
                            Toast.LENGTH_LONG).show());
                }

            } catch (Exception e) {
                Log.e("updateCompanyDetails", "Exception occurred", e);
                runOnUiThread(() -> Toast.makeText(CompanyEditActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

