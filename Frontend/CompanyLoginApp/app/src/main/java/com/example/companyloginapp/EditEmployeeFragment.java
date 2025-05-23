package com.example.companyloginapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class EditEmployeeFragment extends DialogFragment {

    private EditText firstNameInput, lastNameInput, emailInput, passwordInput;
    private Button updateButton;
    private String employeeId;
    private int userId;
    private Spinner roleSpinner;
    private static final String[] ROLES = {"EXECUTIVE", "OWNER", "MANAGER", "EMPLOYEE"};
    private String currentPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_employee, container, false);

        firstNameInput = view.findViewById(R.id.edit_first_name);
        lastNameInput = view.findViewById(R.id.edit_last_name);
        emailInput = view.findViewById(R.id.edit_email);
        passwordInput = view.findViewById(R.id.edit_password);
        roleSpinner = view.findViewById(R.id.edit_role_spinner);
        updateButton = view.findViewById(R.id.update_employee_button);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, ROLES);
        roleSpinner.setAdapter(adapter);

        if (getArguments() != null) {
            employeeId = getArguments().getString("employeeId");
            userId = getArguments().getInt("userId");
            fetchEmployeeDetails(employeeId);
        }

        updateButton.setOnClickListener(v -> updateEmployee());

        return view;
    }

    private void fetchEmployeeDetails(String employeeId) {
        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-024.class.las.iastate.edu:8080/users/" + userId + "/" + employeeId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }

                JSONObject employee = new JSONObject(response.toString());
                String firstName = employee.getString("firstName");
                String lastName = employee.getString("lastName");
                String role = employee.getString("role");
                String email = employee.getString("email");
                currentPassword = employee.getString("password");

                getActivity().runOnUiThread(() -> {
                    firstNameInput.setText(firstName);
                    lastNameInput.setText(lastName);
                    emailInput.setText(email);
                    int roleIndex = java.util.Arrays.asList(ROLES).indexOf(role);
                    if (roleIndex >= 0) {
                        roleSpinner.setSelection(roleIndex);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Failed to load user details", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void updateEmployee() {
        String updatedFirstName = firstNameInput.getText().toString();
        String updatedLastName = lastNameInput.getText().toString();
        String updatedRole = roleSpinner.getSelectedItem().toString();
        String updatedPassword = passwordInput.getText().toString();
        String updatedEmail = emailInput.getText().toString();

        new Thread(() -> {
            try {
                URL url = new URL("http://coms-3090-024.class.las.iastate.edu:8080/users/" + userId + "/user-edit/" + employeeId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject updatedUser = new JSONObject();
                updatedUser.put("firstName", updatedFirstName);
                updatedUser.put("lastName", updatedLastName);
                updatedUser.put("role", updatedRole);
                updatedUser.put("email", updatedEmail);
                if (!updatedPassword.isEmpty()) {
                    updatedUser.put("password", updatedPassword);
                }
                else {
                    updatedUser.put("password", currentPassword);
                }

                conn.getOutputStream().write(updatedUser.toString().getBytes());

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Employee updated successfully", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    });
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Update failed", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Error updating employee", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}