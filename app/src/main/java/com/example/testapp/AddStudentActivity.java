package com.example.testapp;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddStudentActivity extends AppCompatActivity {
    private EditText etFullName, etEmail;
    private Spinner spinnerProgram;
    private Button btnSubmit;
    private List<SchoolClass> classes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        spinnerProgram = findViewById(R.id.spinnerProgram);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadClasses();
        btnSubmit.setOnClickListener(v -> addStudent());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadClasses() {
        String url = "http://10.0.2.2/school_api/get_classes.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray classesArray = response.getJSONArray("classes");
                            for (int i = 0; i < classesArray.length(); i++) {
                                JSONObject classObj = classesArray.getJSONObject(i);
                                classes.add(new SchoolClass(
                                        classObj.getInt("id"),
                                        classObj.getString("name")
                                ));
                            }
                            setupProgramSpinner();
                        } else {
                            showError(response.optString("message", "Failed to load programs"));
                        }
                    } catch (JSONException e) {
                        showError("Error parsing program data");
                    }
                },
                error -> {
                    String errorMsg = "Network error";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    }
                    showError(errorMsg);
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);
    }

    private void setupProgramSpinner() {
        ArrayAdapter<SchoolClass> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                classes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProgram.setAdapter(adapter);
    }

    private void addStudent() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (!validateInputs(name, email)) return;

        SchoolClass selectedProgram = (SchoolClass) spinnerProgram.getSelectedItem();
        addStudentToDatabase(name, email, selectedProgram.getId());
    }

    private boolean validateInputs(String name, String email) {
        name = name.trim();
        email = email.trim();

        if (name.isEmpty() || name.length() > 100) {
            etFullName.setError("Name must be 1-100 characters");
            return false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.length() > 255) {
            etEmail.setError("Valid email required (max 255 chars)");
            return false;
        }

        return true;
    }

    private void addStudentToDatabase(String name, String email, int programId) {
        btnSubmit.setEnabled(false);

        // 1. Verify URL is correct
        String url = "http://10.0.2.2/school_api/add_student.php";
        Log.d("API", "Attempting to POST to: " + url);

        try {
            // 2. Create proper JSON payload
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("class_id", programId);

            Log.d("API", "Request payload: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        btnSubmit.setEnabled(true);
                        try {
                            Log.d("API", "Response: " + response.toString());
                            if (response.getString("status").equals("success")) {
                                etFullName.setText("");
                                etEmail.setText("");
                                Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                String errorMsg = response.optString("message", "Unknown server error");
                                Log.e("API", "Server error: " + errorMsg);
                                showError(errorMsg);
                            }
                        } catch (JSONException e) {
                            Log.e("API", "JSON parsing error", e);
                            showError("Invalid server response");
                        }
                    },
                    error -> {
                        btnSubmit.setEnabled(true);
                        String errorMsg = "Network error";

                        if (error.networkResponse != null) {
                            errorMsg = "HTTP " + error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data, "UTF-8");
                                Log.e("API", "Error response: " + responseBody);
                                errorMsg += " - " + responseBody;
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        } else if (error.getMessage() != null) {
                            errorMsg += ": " + error.getMessage();
                        }

                        Log.e("API", errorMsg, error);
                        showError(errorMsg);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json");
                    return headers;
                }
            };

            // 3. Set proper timeout and retry policy
            request.setRetryPolicy(new DefaultRetryPolicy(
                    15000, // 15 seconds timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            // 4. Add to request queue
            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            btnSubmit.setEnabled(true);
            showError("Error creating request data");
            Log.e("API", "JSON creation error", e);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }
}