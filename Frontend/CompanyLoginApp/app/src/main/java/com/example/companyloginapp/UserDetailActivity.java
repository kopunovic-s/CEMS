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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class  UserDetailActivity extends AppCompatActivity {

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080/users/";
    private final String IMG_URL = "http://coms-3090-024.class.las.iastate.edu:8080/images/user/";
    private int companyId, userId, userIdToSee;
    private Button deleteBtn, editBtn;
    private TextView detailText;
    private ActivityResultLauncher<Intent> editUserLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        // Allow network operations on the main thread (not recommended for production)
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_user_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get user data from intent
        Intent intent = getIntent();
        companyId = intent.getIntExtra("companyId", -1);
        userId = intent.getIntExtra("userId", -1);
        userIdToSee = intent.getIntExtra("userIdToSee", -1);


        deleteBtn = findViewById(R.id.delete_button);
        editBtn = findViewById(R.id.edit_button);

        detailText = findViewById(R.id.detail_text);

        ImageView imageView = findViewById(R.id.imgView);
        String imageUrl = IMG_URL + userId + "/" + userIdToSee;


        fetchInfo();
        loadImg();

        deleteBtn.setOnClickListener(v -> deleteEmployee());
        editBtn.setOnClickListener(v -> openUserEdit());

        editUserLauncher = registerForActivityResult(
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
                URL url = new URL(BASE_URL + userId + "/" + userIdToSee);
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

                // Base64 image handling
//                String base64Image = infoObject.optString("profileImage", null);
//                if (base64Image != null && !base64Image.isEmpty() && !base64Image.equals("null")) {
//                    byte[] decodedBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
//                    runOnUiThread(() -> {
//                        ImageView imageView = findViewById(R.id.imgView);
//                        imageView.setImageBitmap(bitmap);
//                    });
//                }

                // Map for user-friendly labels
                Map<String, String> labelMap = new HashMap<>();
                labelMap.put("firstName", "First Name");
                labelMap.put("lastName", "Last Name");
                labelMap.put("email", "Email");
                labelMap.put("role", "Role");
                labelMap.put("hourlyRate", "Hourly Rate");
                labelMap.put("streetAddress", "Street Address");
                labelMap.put("city", "City");
                labelMap.put("state", "State");
                labelMap.put("zipCode", "ZIP Code");
                labelMap.put("country", "Country");
                labelMap.put("profileImage", "Profile Image");
                labelMap.put("id", "User ID");
                labelMap.put("companyName", "Company Name");
                labelMap.put("companyId", "Company ID");

                // Fields to exclude from display
                Set<String> excludeFields = new HashSet<>(Arrays.asList("password", "ssn", "profileImage"));

                Iterator<String> keys = infoObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (excludeFields.contains(key)) continue;

                    Object value = infoObject.get(key);
                    String label = labelMap.getOrDefault(key, key);

                    if ("hourlyRate".equals(key)) {
                        if (value == null || value.equals(JSONObject.NULL)) {
                            value = "0.0";
                        }
                    }

                    displayText.append("<b>").append(label).append(":</b> ").append(value).append("<br>");
                }

                // Update UI
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
            HttpURLConnection conn = null;
            Scanner scanner = null;
            try {
                URL url = new URL(BASE_URL + userId + "/" + userIdToSee);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                scanner = new Scanner(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                JSONObject infoObject = new JSONObject(response.toString());
                String imageData = infoObject.optString("profileImage", null);

                if (imageData != null && !imageData.isEmpty() && !imageData.equals("null")) {
                    byte[] decodedBytes;
                    if (imageData.startsWith("data:image")) {
                        String base64Image = imageData.substring(imageData.indexOf(",") + 1);
                        decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    } else {
                        decodedBytes = Base64.decode(imageData, Base64.DEFAULT);
                    }

                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    runOnUiThread(() -> {
                        ImageView imageView = findViewById(R.id.imgView);
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(R.drawable.user_placeholder);
                        }
                    });

                } else {
                    runOnUiThread(() -> {
                        ImageView imageView = findViewById(R.id.imgView);
                        imageView.setImageResource(R.drawable.user_placeholder);
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() -> {
                    ImageView imageView = findViewById(R.id.imgView);
                    imageView.setImageResource(R.drawable.user_placeholder);
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (scanner != null) scanner.close();
                if (conn != null) conn.disconnect();
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

    private void deleteEmployee() {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-024.class.las.iastate.edu:8080/users/" + userId + "/user-delete/" + userIdToSee);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Employee deleted successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish(); // Return to Company Roster Activity
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



    private void openUserEdit() {
        Intent companyEditIntent = new Intent(this, UserEditActivity.class);
        companyEditIntent.putExtra("companyId", companyId);
        companyEditIntent.putExtra("userId", userId);
        companyEditIntent.putExtra("userIdToSee", userIdToSee);
        editUserLauncher.launch(companyEditIntent);
    }
}

