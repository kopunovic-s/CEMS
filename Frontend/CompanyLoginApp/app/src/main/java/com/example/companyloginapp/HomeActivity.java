package com.example.companyloginapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        welcomeText = findViewById(R.id.welcome_text);

        // Retrieve user details from intent
        String firstName = getIntent().getStringExtra("firstName");
        String role = getIntent().getStringExtra("role");

        welcomeText.setText("Welcome, " + firstName + " (" + role + ")");
    }
}