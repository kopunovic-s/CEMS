package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity {

    private EditText email, password, confirmPassword, companyName, firstName, lastName;
    private Button signUpButton;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Link UI elements
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        confirmPassword = findViewById(R.id.signup_confirm_password);
        companyName = findViewById(R.id.signup_company_name);
        firstName = findViewById(R.id.signup_firstname);
        lastName = findViewById(R.id.signup_lastname);
        signUpButton = findViewById(R.id.signup_button);

        requestQueue = Volley.newRequestQueue(this);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });
    }

    private void signUpUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String userConfirmPassword = confirmPassword.getText().toString().trim();
        String userCompanyName = companyName.getText().toString().trim();
        String userFirstName = firstName.getText().toString().trim();
        String userLastName = lastName.getText().toString().trim();

        if (userEmail.isEmpty() || userPassword.isEmpty() || userConfirmPassword.isEmpty() ||
                userCompanyName.isEmpty() || userFirstName.isEmpty() || userLastName.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!userPassword.equals(userConfirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send user details to the backend for registration (without Company ID)
        String url = "http://coms-3090-024.class.las.iastate.edu:8080/auth/register";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", userEmail);
            jsonBody.put("password", userPassword);
            jsonBody.put("companyName", userCompanyName);
            jsonBody.put("firstName", userFirstName);
            jsonBody.put("lastName", userLastName);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(SignUpActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Toast.makeText(SignUpActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();

                        // Navigate back to Login screen after successful sign-up
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Prevents user from coming back to sign-up screen
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SignUpActivity.this, "Sign Up Failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonRequest);
    }
}
