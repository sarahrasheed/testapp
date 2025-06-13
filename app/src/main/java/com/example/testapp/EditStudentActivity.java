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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditStudentActivity extends AppCompatActivity {
    private EditText etName, etEmail;
    private Spinner spinnerClass;
    private Student currentStudent;
    private List<SchoolClass> classes = new ArrayList<>();
    private int studentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student);

        // Initialize views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        spinnerClass = findViewById(R.id.spinnerClass);
        Button btnSave = findViewById(R.id.btnSave);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Get student data from intent
        try {
            // In EditStudentActivity.java
            studentId = getIntent().getIntExtra("STUDENT_ID", -1);
            String name = getIntent().getStringExtra("STUDENT_NAME");
            String email = getIntent().getStringExtra("STUDENT_EMAIL");
            int classId = getIntent().getIntExtra("STUDENT_CLASS_ID", -1);

            if (studentId == -1 || classId == -1) {
                throw new Exception("Invalid student data");
            }

            etName.setText(name);
            etEmail.setText(email);

        } catch (Exception e) {
            Toast.makeText(this, "Error loading student data", Toast.LENGTH_SHORT).show();
            Log.e("EDIT_ACTIVITY", "Intent data error", e);
            finish();
            return;
        }
        // Load classes
        loadClasses();

        // Set listeners
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> updateStudent());
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
        spinnerClass.setAdapter(adapter);
    }

    private void updateStudent() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        SchoolClass selectedClass = (SchoolClass) spinnerClass.getSelectedItem();

        if (name.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter valid data", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/school_api/edit_student.php";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("student_id", studentId);
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("class_id", selectedClass.getId());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                setResult(RESULT_OK);
                                finish();
                            }
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Log.e("UPDATE_ERROR", "JSON parse error", e);
                        }
                    },
                    error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            Log.e("UPDATE_ERROR", "JSON creation error", e);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }
}