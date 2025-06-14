package com.example.testapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
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

public class StudentActivity extends AppCompatActivity {
    private ListView studentListView;
    private StudentAdapter adapter;
    private List<Student> students = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        // Initialize views
        studentListView = findViewById(R.id.studentListView);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Set up adapter
        adapter = new StudentAdapter(this, students);
        studentListView.setAdapter(adapter);

        // Set click listeners
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Button btnAddStudent = findViewById(R.id.btnAddStudent);
        btnAddStudent.setOnClickListener(v -> showAddStudentDialog());

        // Load student data
        loadStudents();
    }

    private void loadStudents() {
        String url = "http://10.0.2.2/school_api/get_students.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            students.clear();
                            JSONArray studentsArray = response.getJSONArray("students");
                            for (int i = 0; i < studentsArray.length(); i++) {
                                JSONObject studentObj = studentsArray.getJSONObject(i);
                                students.add(new Student(
                                        studentObj.getInt("id"),
                                        studentObj.getString("name"),
                                        studentObj.getString("email"),
                                        studentObj.getString("password"),
                                        studentObj.getInt("class_id")
                                ));
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            showError(response.optString("message", "Failed to load students"));
                        }
                    } catch (JSONException e) {
                        showError("Error parsing student data");
                        Log.e("API", "JSON parsing error", e);
                    }
                },
                error -> {
                    String errorMsg = "Network error";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg = new String(error.networkResponse.data);
                    }
                    showError(errorMsg);
                    Log.e("API", "Network error", error);
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Volley.newRequestQueue(this).add(request);
    }

    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Student");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_student, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.etName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Spinner spinnerClass = view.findViewById(R.id.spinnerClass);

        // Load classes from database
        loadClasses(spinnerClass);

        // Auto-generate email
        etName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString().trim().toLowerCase();
                if (!name.isEmpty()) {
                    etEmail.setText(name.replaceAll("\\s+", ".") + "@student.com");
                }
            }
        });

        // Create dialog first
        AlertDialog dialog = builder.create();

        // Set button click listeners after dialog creation
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", (DialogInterface.OnClickListener) null);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (dialog1, which) -> dialog1.dismiss());

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                SchoolClass selectedClass = (SchoolClass) spinnerClass.getSelectedItem();
                if (selectedClass == null || selectedClass.getId() == -1) {
                    Toast.makeText(this, "Please select a class", Toast.LENGTH_SHORT).show();
                    return;
                }

                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (name.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Name and password are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Only dismiss dialog after successful operation
                addStudent(name, email, password, selectedClass.getId(), dialog);
            });
        });

        dialog.show();
    }
    private void loadClasses(Spinner spinner) {
        String url = "http://10.0.2.2/school_api/get_classes.php";

        List<SchoolClass> classes = new ArrayList<>();
        classes.add(new SchoolClass(-1, "Select Class")); // Default option

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject classObj = response.getJSONObject(i);
                            classes.add(new SchoolClass(
                                    classObj.getInt("class_id"),
                                    classObj.getString("name")
                            ));
                        }

                        ArrayAdapter<SchoolClass> adapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                classes
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error loading classes", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }
    private void addStudent(String name, String email, String password, int class_id, AlertDialog dialog) {
        String url = "http://10.0.2.2/school_api/add_student.php";

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding student...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("class_id", class_id);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        progressDialog.dismiss();
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss(); // Dismiss only after success
                                loadStudents(); // Refresh the list
                            } else {
                                Toast.makeText(this, "Failed to add student: " +
                                        response.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "Error parsing response", Toast.LENGTH_LONG).show();
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Network error: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_LONG).show();
        }
    }

    public void showEditDialog(Student student) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Student");

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_student, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.etName);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        Spinner spinnerClass = view.findViewById(R.id.spinnerClass);

        // Set current values
        etName.setText(student.getName());
        etEmail.setText(student.getEmail());
        etPassword.setText(student.getPassword());

        // Load classes and set current selection
        loadClasses(spinnerClass, student.getClassId());

        builder.setPositiveButton("Update", (dialog, which) -> {
            SchoolClass selectedClass = (SchoolClass) spinnerClass.getSelectedItem();
            String updatedName = etName.getText().toString().trim();
            String updatedEmail = etEmail.getText().toString().trim();
            String updatedPassword = etPassword.getText().toString().trim();

            if (updatedName.isEmpty() || updatedEmail.isEmpty() || updatedPassword.isEmpty() || selectedClass == null) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            updateStudent(
                    student.getId(),
                    updatedName,
                    updatedEmail,
                    updatedPassword,
                    selectedClass.getId()  // Pass the class ID
            );
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void loadClasses(Spinner spinner, int currentClassId) {
        String url = "http://10.0.2.2/school_api/get_classes.php";

        List<SchoolClass> classes = new ArrayList<>();
        classes.add(new SchoolClass(-1, "Select Class"));
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        int selectedPosition = 0;

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject classObj = response.getJSONObject(i);
                            int classId = classObj.getInt("class_id");
                            String className = classObj.getString("name");

                            classes.add(new SchoolClass(classId, className));

                            // Remember position if this is the student's current class
                            if (classId == currentClassId) {
                                selectedPosition = i;
                            }
                        }

                        ArrayAdapter<SchoolClass> adapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                classes
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                        spinner.setSelection(selectedPosition);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error loading classes", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void updateStudent(int student_id, String name, String email, String password, int class_id) {
        String url = "http://10.0.2.2/school_api/edit_student.php";

        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating student...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // Create request body
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("student_id", student_id);
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("class_id", class_id);


            // Debug log
            Log.d("UPDATE_STUDENT", "Sending: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        progressDialog.dismiss();
                        try {
                            Log.d("UPDATE_RESPONSE", response.toString());
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(this, "Student updated successfully", Toast.LENGTH_SHORT).show();
                                loadStudents(); // Refresh the list
                            } else {
                                Toast.makeText(this, "Update failed: " + response.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "Error parsing response", Toast.LENGTH_LONG).show();
                            Log.e("UPDATE_ERROR", "JSON error", e);
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        String errorMsg = "Network error";
                        if (error.networkResponse != null) {
                            errorMsg += " (Status: " + error.networkResponse.statusCode + ")";
                            try {
                                String responseBody = new String(error.networkResponse.data, "UTF-8");
                                errorMsg += ": " + responseBody;
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("UPDATE_ERROR", errorMsg, error);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    try {
                        return jsonBody.toString().getBytes("utf-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e("UPDATE_ERROR", "Encoding error", e);
                        return null;
                    }
                }
            };

            // Set timeout and retry policy
            request.setRetryPolicy(new DefaultRetryPolicy(
                    15000, // 15 seconds timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_LONG).show();
            Log.e("UPDATE_ERROR", "JSON creation error", e);
        }
    }

    public void deleteStudent(int studentId) {
        String url = "http://10.0.2.2/school_api/delete_student.php";

        // Show loading indicator
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting student...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // Create JSON payload
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("student_id", studentId); // Must match PHP expected parameter

            // Log the request for debugging
            Log.d("DELETE_REQUEST", "Endpoint: " + url);
            Log.d("DELETE_REQUEST", "Student ID: " + studentId);
            Log.d("DELETE_REQUEST", "Request Body: " + jsonBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        progressDialog.dismiss();
                        try {
                            Log.d("DELETE_RESPONSE", "Raw Response: " + response.toString());

                            if (response.has("status") && response.getString("status").equals("success")) {
                                // Success case
                                runOnUiThread(() -> {
                                    Toast.makeText(this, "teacher deleted successfully", Toast.LENGTH_SHORT).show();
                                    loadStudents(); // Refresh the list
                                });
                            } else {
                                // Server returned error
                                String errorMsg = response.optString("message", "Unknown server error");
                                Log.e("DELETE_ERROR", "Server error: " + errorMsg);
                                runOnUiThread(() ->
                                        Toast.makeText(this, "Delete failed: " + errorMsg, Toast.LENGTH_LONG).show()
                                );
                            }
                        } catch (JSONException e) {
                            Log.e("DELETE_ERROR", "JSON parsing error", e);
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Error parsing server response", Toast.LENGTH_LONG).show()
                            );
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        String errorMsg = "Network error";

                        // Detailed error logging
                        if (error.networkResponse != null) {
                            errorMsg = "HTTP " + error.networkResponse.statusCode;
                            try {
                                String responseBody = new String(error.networkResponse.data, "UTF-8");
                                errorMsg += " - " + responseBody;
                                Log.e("DELETE_ERROR", "Full error response: " + responseBody);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        } else if (error.getMessage() != null) {
                            errorMsg += ": " + error.getMessage();
                        }

                        Log.e("DELETE_ERROR", errorMsg, error);
                        String finalErrorMsg = errorMsg;
                        runOnUiThread(() ->
                                Toast.makeText(this, "Delete failed: " + finalErrorMsg, Toast.LENGTH_LONG).show()
                        );
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json");
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    try {
                        return jsonBody.toString().getBytes("utf-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.e("DELETE_ERROR", "Encoding error", e);
                        return null;
                    }
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            // Set retry policy
            request.setRetryPolicy(new DefaultRetryPolicy(
                    15000, // 15 seconds timeout
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            // Add to request queue
            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            progressDialog.dismiss();
            Log.e("DELETE_ERROR", "JSON creation error", e);
            Toast.makeText(this, "Error creating delete request", Toast.LENGTH_LONG).show();
        }
    }

    private void showError(String message) {
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }
}