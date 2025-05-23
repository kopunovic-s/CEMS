package com.example.companyloginapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity {

    private EditText email, password, companyName, companyId;
    private Button loginButton;
    private TextView signupLink;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        companyName = findViewById(R.id.company_name);
        companyId = findViewById(R.id.company_id);
        loginButton = findViewById(R.id.login_button);
        signupLink = findViewById(R.id.signup_link);

        requestQueue = Volley.newRequestQueue(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String userEmail = email.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String userCompanyName = companyName.getText().toString().trim();
        String userCompanyId = companyId.getText().toString().trim();

        if (userEmail.isEmpty() || userPassword.isEmpty() || userCompanyName.isEmpty() || userCompanyId.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://coms-3090-024.class.las.iastate.edu:8080/auth/login";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", userEmail);
            jsonBody.put("password", userPassword);
            jsonBody.put("companyName", userCompanyName);
            jsonBody.put("companyId", Integer.parseInt(userCompanyId));
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        } catch (NumberFormatException e) {
            Toast.makeText(MainActivity.this, "Company ID must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int userId = response.getInt("id");
                            String firstName = response.getString("firstName");
                            String lastName = response.getString("lastName");
                            String email = response.getString("email");
                            String role = response.getString("role");

                            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(MainActivity.this, DashBoardActivity.class);
                            intent.putExtra("userId", userId);
                            intent.putExtra("firstName", firstName);
                            intent.putExtra("lastName", lastName);
                            intent.putExtra("email", email);
                            intent.putExtra("role", role);
                            intent.putExtra("companyName", userCompanyName);
                            intent.putExtra("companyId", Integer.parseInt(userCompanyId));
                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "JSON Parsing Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Login Failed. Check credentials.", Toast.LENGTH_SHORT).show();
                    }
                });

        requestQueue.add(jsonRequest);
    }
}
