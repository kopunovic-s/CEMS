package com.example.companyloginapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import okhttp3.*;

public class ProductActivity extends AppCompatActivity {

    private int userId, companyId, departmentId;
    private String departmentName;
    private List<JSONObject> productList = new ArrayList<>();
    private ProductAdapter adapter;
    private Uri selectedImageUri;

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080";
    private static final int PICK_IMAGE_REQUEST = 101;

    private String tempProductName;
    private double tempCost, tempPrice;
    private int tempQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        companyId = intent.getIntExtra("companyID", -1);
        departmentId = intent.getIntExtra("departmentId", -1);
        departmentName = intent.getStringExtra("departmentName");

        Toolbar toolbar = findViewById(R.id.toolbar_product);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Products - " + departmentName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView productListView = findViewById(R.id.product_list);
        Button addProductButton = findViewById(R.id.add_product_button);
        Button analyticsButton = findViewById(R.id.analytics_button);

        adapter = new ProductAdapter(this, productList, new ProductAdapter.ProductSellCallback() {
            @Override
            public void onSellClicked(int productId) {
                sellProduct(productId);
            }

            @Override
            public void onDeleteClicked(int productId) {
                deleteProduct(productId);
            }
        });

        productListView.setAdapter(adapter);
        addProductButton.setOnClickListener(v -> showAddProductDialog());

        analyticsButton.setOnClickListener(v -> {
            Intent analyticsIntent = new Intent(ProductActivity.this, ProductAnalyticsActivity.class);
            analyticsIntent.putExtra("departmentId", departmentId);
            startActivity(analyticsIntent);
        });

        fetchProducts();
    }

    private void fetchProducts() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/inventory/get-inventory/" + departmentId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) response.append(scanner.nextLine());
                scanner.close();

                JSONArray products = new JSONArray(response.toString());
                productList.clear();

                for (int i = 0; i < products.length(); i++) {
                    productList.add(products.getJSONObject(i));
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to load products", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showAddProductDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        EditText nameInput = dialogView.findViewById(R.id.input_name);
        EditText costInput = dialogView.findViewById(R.id.input_cost);
        EditText priceInput = dialogView.findViewById(R.id.input_price);
        EditText quantityInput = dialogView.findViewById(R.id.input_quantity);
        ImageView imagePreview = new ImageView(this);
        imagePreview.setPadding(0, 20, 0, 20);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.addView(dialogView);
        container.addView(imagePreview);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Add Product")
                .setView(container)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String costStr = costInput.getText().toString().trim();
                    String priceStr = priceInput.getText().toString().trim();
                    String quantityStr = quantityInput.getText().toString().trim();

                    if (name.isEmpty() || costStr.isEmpty() || priceStr.isEmpty() || quantityStr.isEmpty()) {
                        Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tempProductName = name;
                    tempCost = Double.parseDouble(costStr);
                    tempPrice = Double.parseDouble(priceStr);
                    tempQuantity = Integer.parseInt(quantityStr);

                    addProduct(tempProductName, tempCost, tempPrice, tempQuantity);
                })
                .setNeutralButton("Pick Image", (dialog, which) -> openImagePicker(imagePreview))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openImagePicker(ImageView preview) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Product Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
        }
    }

    private void addProduct(String name, double cost, double price, int quantity) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/inventory/buy-item/" + departmentId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("itemName", name);
                payload.put("cost", cost);
                payload.put("price", price);
                payload.put("quantity", quantity);
                payload.put("description", "");

                conn.getOutputStream().write(payload.toString().getBytes());

                if (conn.getResponseCode() == 200 || conn.getResponseCode() == 201) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNext()) response.append(scanner.nextLine());
                    scanner.close();
                    JSONObject newItem = new JSONObject(response.toString());
                    int itemId = newItem.getInt("id");

                    if (selectedImageUri != null) uploadImage(itemId);
                    updateExpenses(cost * quantity);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
                        fetchProducts();
                        selectedImageUri = null;
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error adding product", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void uploadImage(int itemId) {
        try {
            File imageFile = new File(getRealPathFromURI(selectedImageUri));
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", imageFile.getName(),
                            RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                    .build();

            Request request = new Request.Builder()
                    .url(BASE_URL + "/inventory/upload-image/" + itemId)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // Optional
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
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

    private void sellProduct(int productId) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/inventory/sell-item/" + productId + "/1");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");

                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Marked as sold!", Toast.LENGTH_SHORT).show();
                        fetchProducts();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to mark as sold", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error selling product", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void deleteProduct(int productId) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/inventory/delete-item/" + productId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");

                if (conn.getResponseCode() == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                        fetchProducts();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to delete product", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error deleting product", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateExpenses(double amount) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/sales/add-expense/" + companyId + "/" + amount);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.getResponseCode();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}