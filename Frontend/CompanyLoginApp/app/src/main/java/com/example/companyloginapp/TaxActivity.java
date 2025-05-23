package com.example.companyloginapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class TaxActivity extends AppCompatActivity {

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080/w2";
    private int userId, targetUserId;
    private String userRole;
    private EditText inputYear;
    private TextView taxInfoText;
    private Button fetchButton, downloadPdfButton;
    private String selectedYear;
    private JSONObject currentW2Data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tax);

        inputYear = findViewById(R.id.input_tax_year);
        taxInfoText = findViewById(R.id.tax_info_text);
        taxInfoText.setMovementMethod(new ScrollingMovementMethod());

        fetchButton = findViewById(R.id.button_fetch_tax);
        downloadPdfButton = findViewById(R.id.button_download_pdf);

        Intent intent = getIntent();
        userId = intent.getIntExtra("userId", -1);
        targetUserId = intent.getIntExtra("targetUserId", userId);
        userRole = intent.getStringExtra("role");

        if (!userRole.equals("EXECUTIVE") && !userRole.equals("OWNER")) {
            targetUserId = userId;
        }

        fetchButton.setOnClickListener(v -> {
            selectedYear = inputYear.getText().toString().trim();
            if (selectedYear.isEmpty()) {
                Toast.makeText(this, "Please enter a year", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchW2Info(userId, targetUserId, selectedYear);
        });

        downloadPdfButton.setOnClickListener(v -> {
            if (selectedYear == null || selectedYear.isEmpty()) {
                Toast.makeText(this, "Fetch W-2 info first", Toast.LENGTH_SHORT).show();
                return;
            }
            downloadAndOpenPdf();
        });
    }

    private void downloadAndOpenPdf() {
        new Thread(() -> {
            try {
                String fileUrl = BASE_URL + "/" + userId + "/" + targetUserId + "/" + selectedYear + "/pdf";
                URL url = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                File file = new File(getExternalFilesDir(null), "W2_" + selectedYear + ".pdf");

                InputStream input = conn.getInputStream();
                FileOutputStream output = new FileOutputStream(file);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }

                input.close();
                output.close();

                runOnUiThread(() -> {
                    Toast.makeText(this, "PDF saved: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    openPdf(file);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void openPdf(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchW2Info(int currentUserId, int targetUserId, String year) {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/" + currentUserId + "/" + targetUserId + "/" + year);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject data = new JSONObject(response.toString());
                currentW2Data = data;

                String formatted = formatW2Data(data);
                runOnUiThread(() -> taxInfoText.setText(formatted));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to fetch W-2: Make sure user/company info is complete", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private String formatW2Data(JSONObject obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("Year: ").append(obj.optInt("year")).append("\n\n");

        sb.append("Employee Name: ").append(obj.optString("employeeFirstName"))
                .append(" ").append(obj.optString("employeeLastName")).append("\n");
        sb.append("SSN: ").append(obj.optString("employeeSsn")).append("\n");
        sb.append("Address: ").append(obj.optString("employeeAddress")).append(", ")
                .append(obj.optString("employeeCity")).append(", ")
                .append(obj.optString("employeeState")).append(" ")
                .append(obj.optString("employeeZip")).append("\n\n");

        sb.append("Employer: ").append(obj.optString("employerName")).append("\n");
        sb.append("EIN: ").append(obj.optString("employerEin")).append("\n");
        sb.append("Employer Address: ").append(obj.optString("employerAddress")).append(", ")
                .append(obj.optString("employerCity")).append(", ")
                .append(obj.optString("employerState")).append(" ")
                .append(obj.optString("employerZip")).append("\n\n");

        sb.append("Wages: $").append(obj.optDouble("wagesTipsOtherComp")).append("\n");
        sb.append("Federal Tax: $").append(obj.optDouble("federalIncomeTax")).append("\n");
        sb.append("Social Security Wages: $").append(obj.optDouble("socialSecurityWages")).append("\n");
        sb.append("Social Security Tax: $").append(obj.optDouble("socialSecurityTax")).append("\n");
        sb.append("Medicare Wages: $").append(obj.optDouble("medicareWages")).append("\n");
        sb.append("Medicare Tax: $").append(obj.optDouble("medicareTax")).append("\n");
        sb.append("State Wages: $").append(obj.optDouble("stateWages")).append("\n");
        sb.append("State Tax: $").append(obj.optDouble("stateIncomeTax")).append("\n");

        return sb.toString();
    }
}
