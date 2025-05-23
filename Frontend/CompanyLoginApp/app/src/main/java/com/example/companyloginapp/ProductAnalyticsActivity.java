package com.example.companyloginapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ProductAnalyticsActivity extends AppCompatActivity {

    private int departmentId;
    private final String BASE_URL = "http://coms-3090-024.class.las.iastate.edu:8080";
    private BarChart barChartIncome, barChartExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_analytics);

        departmentId = getIntent().getIntExtra("departmentId", -1);

        barChartIncome = findViewById(R.id.barChartIncome);
        barChartExpense = findViewById(R.id.barChartExpense);

        fetchDepartmentData();
    }

    private void fetchDepartmentData() {
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

                float totalIncome = 0f;
                float totalExpense = 0f;

                for (int i = 0; i < products.length(); i++) {
                    JSONObject product = products.getJSONObject(i);
                    int quantity = product.getInt("quantity");
                    float cost = (float) product.getDouble("cost");
                    float price = (float) product.getDouble("price");
                    boolean available = product.getBoolean("available");

                    // Expenses = current quantity * cost
                    totalExpense += cost * quantity;

                    // Approximate sold income:
                    // If the item is no longer available and quantity == 0, assume 1 item sold
                    if (!available && quantity == 0) {
                        totalIncome += price;
                    }
                }

                float finalIncome = totalIncome;
                float finalExpense = totalExpense;

                runOnUiThread(() -> renderBarGraphs(finalIncome, finalExpense));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to fetch analytics", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void renderBarGraphs(float income, float expense) {
        ArrayList<BarEntry> incomeEntries = new ArrayList<>();
        incomeEntries.add(new BarEntry(0, income));
        BarDataSet incomeSet = new BarDataSet(incomeEntries, "Income");
        incomeSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barChartIncome.setData(new BarData(incomeSet));
        barChartIncome.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartIncome.getDescription().setText("Total Income");
        barChartIncome.invalidate();

        ArrayList<BarEntry> expenseEntries = new ArrayList<>();
        expenseEntries.add(new BarEntry(0, expense));
        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Expenses");
        expenseSet.setColors(ColorTemplate.COLORFUL_COLORS);
        barChartExpense.setData(new BarData(expenseSet));
        barChartExpense.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartExpense.getDescription().setText("Total Expenses");
        barChartExpense.invalidate();
    }
}
