package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.ValueFormatter;


import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class InventorySummaryActivity extends AppCompatActivity {

    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080";
    private int companyId;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_summary);

        barChart = findViewById(R.id.bar_chart);

        Intent intent = getIntent();
        companyId = intent.getIntExtra("companyID", -1);

        fetchSummaryData();
    }

    private void fetchSummaryData() {
        new Thread(() -> {
            try {
                URL url = new URL(BASE_URL + "/sales/get-summary/" + companyId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) response.append(scanner.nextLine());
                scanner.close();

                JSONObject summary = new JSONObject(response.toString());
                float totalExpenses = (float) summary.getDouble("totalExpenses");
                float totalIncome = (float) summary.getDouble("totalIncome");

                runOnUiThread(() -> setupChart(totalExpenses, totalIncome));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to load summary", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void setupChart(float expenses, float income) {
        BarEntry incomeEntry = new BarEntry(0, income);
        BarEntry expenseEntry = new BarEntry(1, expenses);

        BarDataSet dataSet = new BarDataSet(java.util.Arrays.asList(incomeEntry, expenseEntry), "Company Summary");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(16f);

        BarData data = new BarData(dataSet);

        barChart.setData(data);
        barChart.getDescription().setText("Income vs. Expenses");
        barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0f) return "Income";
                if (value == 1f) return "Expenses";
                return "";
            }
        });

        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setGranularityEnabled(true);
        barChart.invalidate(); // refresh chart
    }
}
