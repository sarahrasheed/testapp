package com.example.testapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddStudentActivity extends AppCompatActivity {

    private EditText etStudentId, etFullName, etEmail;
    private Spinner spinnerProgram;
    private static final String ADD_STUDENT_URL = "http://10.0.2.2/school_api/add_student.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        // Initialize views
        etStudentId = findViewById(R.id.etStudentId);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        spinnerProgram = findViewById(R.id.spinnerProgram);
        Button btnSubmit = findViewById(R.id.btnSubmit);

        // Setup spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.classes_array,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProgram.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> {
            if (validateInput()) {
                addStudentToDatabase();
            }
        });
    }

    private boolean validateInput() {
        if (etStudentId.getText().toString().isEmpty()) {
            etStudentId.setError("Student ID required");
            return false;
        }
        if (etFullName.getText().toString().isEmpty()) {
            etFullName.setError("Full name required");
            return false;
        }
        if (etEmail.getText().toString().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
            etEmail.setError("Valid email required");
            return false;
        }
        return true;
    }

    private void addStudentToDatabase() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding student...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                ADD_STUDENT_URL,
                response -> {
                    progressDialog.dismiss();
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            Toast.makeText(this, "Student added successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity on success
                        } else {
                            Toast.makeText(this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    if (error.networkResponse != null) {
                        Log.e("VolleyError", "Status code: " + error.networkResponse.statusCode);
                    }
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    error.printStackTrace(); // Check Logcat for full details
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("student_id", etStudentId.getText().toString());
                params.put("name", etFullName.getText().toString());
                params.put("email", etEmail.getText().toString());
                // Map spinner position to class_id (1 for 1A, 2 for 2B, etc.)
                params.put("class_id", String.valueOf(spinnerProgram.getSelectedItemPosition() + 1));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}