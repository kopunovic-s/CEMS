package com.example.companyloginapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResult;

import com.bumptech.glide.Glide;
import android.widget.ImageView;

public class CompanyDetailActivity extends AppCompatActivity {

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080/Companies/";
    private final String IMG_URL = "http://coms-3090-024.class.las.iastate.edu:8080/images/company/";

    private int companyId, userId;
    private Button deleteImgBtn, editBtn;
    private TextView detailText;
    private ActivityResultLauncher<Intent> editCompanyLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_detail);

        // Allow network operations on the main thread (not recommended for production)
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_company_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get user data from intent
        Intent intent = getIntent();
        companyId = intent.getIntExtra("companyId", -1);
        userId = intent.getIntExtra("userId", -1);

        deleteImgBtn = findViewById(R.id.delete_image_button);
        editBtn = findViewById(R.id.edit_button);

        detailText = findViewById(R.id.detail_text);

        ImageView imageView = findViewById(R.id.imgView);
        String imageUrl = IMG_URL + userId + "/" + companyId;


        loadImg();

        fetchInfo();

        deleteImgBtn.setOnClickListener(v -> deleteImg());
        editBtn.setOnClickListener(v -> openCompanyEdit());

        editCompanyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Company was updated, so refresh details
                        fetchInfo();
                        loadImg();
                    }
                }
        );
    }

    // Handle toolbar back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchInfo() {
        new Thread(() -> {
            HttpURLConnection conn = null;
            Scanner scanner = null;
            try {
                URL url = new URL(BASE_URL + companyId);
                Log.d("fetchInfo", "Fetching data from " + url);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                scanner = new Scanner(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                JSONObject infoObject = new JSONObject(response.toString());
                StringBuilder displayText = new StringBuilder();

                // Map for human-friendly labels
                Map<String, String> labelMap = new HashMap<>();
                labelMap.put("id", "Company ID");
                labelMap.put("name", "Company Name");
                labelMap.put("ein", "Employer ID Number (EIN)");
                labelMap.put("streetAddress", "Street Address");
                labelMap.put("city", "City");
                labelMap.put("state", "State");
                labelMap.put("zipCode", "ZIP Code");
                labelMap.put("country", "Country");

                Iterator<String> keys = infoObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = infoObject.get(key);

                    // Skip Base64 image or unwanted fields
                    if (key.equalsIgnoreCase("image") || key.equalsIgnoreCase("companyLogo") || key.toLowerCase().contains("base64")) {
                        continue;
                    }

                    // Get label or fallback to key
                    String label = labelMap.getOrDefault(key, key);
                    displayText.append("<b>").append(label).append(":</b> ").append(value).append("<br>");
                }

                // Update UI with formatted HTML text
                runOnUiThread(() -> detailText.setText(Html.fromHtml(displayText.toString())));

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to fetch info: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (scanner != null) scanner.close();
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void loadImg() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            Scanner scanner = null;
            try {
                String imageUrl = IMG_URL + userId + "/" + companyId;
                Log.d("ImageLoad", "Fetching base64 image data from URL: " + imageUrl);

                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();

                int responseCode = connection.getResponseCode();
                Log.d("ImageLoad", "HTTP Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    scanner = new Scanner(new InputStreamReader(connection.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    while (scanner.hasNext()) {
                        responseBuilder.append(scanner.nextLine());
                    }

                    String imageData = responseBuilder.toString();
                    Log.d("ImageLoad", "Received data (truncated): " + imageData.substring(0, Math.min(imageData.length(), 100)) + "...");

                    byte[] decodedBytes;
                    if (imageData.startsWith("data:image")) {
                        String base64Part = imageData.substring(imageData.indexOf(",") + 1);
                        decodedBytes = Base64.decode(base64Part, Base64.DEFAULT);
                        Log.d("ImageLoad", "Decoded image from data URI format");
                    } else {
                        decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                        Log.d("ImageLoad", "Decoded raw base64 image");
                    }

                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    if (bitmap != null) {
                        runOnUiThread(() -> {
                            ImageView imageView = findViewById(R.id.imgView);
                            imageView.setImageBitmap(bitmap);
                            Log.d("ImageLoad", "Image set to ImageView");
                        });
                    } else {
                        Log.e("ImageLoad", "Bitmap decoding failed (null)");
                    }

                } else {
                    Log.e("ImageLoad", "Server returned HTTP " + responseCode);
                }
            } catch (Exception e) {
                Log.e("ImageLoad", "Exception while loading base64 image", e);
            } finally {
                if (scanner != null) scanner.close();
                if (connection != null) connection.disconnect();
                Log.d("ImageLoad", "Connection closed");
            }
        }).start();
    }


    private void deleteImg() {
        new Thread(() -> {
            try {
                String imageUrl = IMG_URL + userId + "/" + companyId;
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setDoInput(true);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    runOnUiThread(() -> {
                        ImageView imageView = findViewById(R.id.imgView);
                        imageView.setImageDrawable(null); // Clears the image
                        fetchInfo();
                    });
                } else {
                    Log.e("ImageDelete", "Server returned HTTP " + responseCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void openCompanyEdit() {
        Intent companyEditIntent = new Intent(this, CompanyEditActivity.class);
        companyEditIntent.putExtra("companyId", companyId);
        companyEditIntent.putExtra("userId", userId);
        editCompanyLauncher.launch(companyEditIntent);
    }
}

