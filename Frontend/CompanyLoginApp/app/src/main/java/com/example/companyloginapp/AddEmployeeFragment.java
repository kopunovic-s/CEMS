package com.example.companyloginapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class AddEmployeeFragment extends Fragment {

    private EditText firstNameInput, lastNameInput, emailInput, passwordInput;
    private Button submitButton;
    private Spinner roleSpinner;

    private int userId;

    public AddEmployeeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_employee, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            userId = bundle.getInt("userId");
            // Use the data as needed
        }

        firstNameInput = view.findViewById(R.id.edit_text_first_name);
        lastNameInput = view.findViewById(R.id.edit_text_last_name);
        emailInput = view.findViewById(R.id.edit_text_email);
        passwordInput = view.findViewById(R.id.edit_text_password);
        roleSpinner = view.findViewById(R.id.spinner_role);
        submitButton = view.findViewById(R.id.button_submit);

        // Set up the spinner with role options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.roles_array, // Define this in res/values/strings.xml
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        submitButton.setOnClickListener(v -> addEmployee());

        return view;
    }

    private void addEmployee() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(role)) {
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        String url = "http://coms-3090-024.class.las.iastate.edu:8080/users/" + userId + "/user-add";  // Adjust the API endpoint as needed

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("firstName", firstName);
            requestBody.put("lastName", lastName);
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("role", role);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                    response -> {
                        Toast.makeText(getContext(), "Employee Added Successfully", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack(); // Go back to CompanyRosterActivity
                    },

                // Log the error response to see details
                    error -> Toast.makeText(getContext(), "Employee Added", Toast.LENGTH_SHORT).show()) {
                @Override
                public String getBodyContentType() {
                    return "application/json";  // Set content type to JSON
                }
            };

            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}

