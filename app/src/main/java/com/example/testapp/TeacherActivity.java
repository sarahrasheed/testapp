package com.example.testapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
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

public class TeacherActivity extends AppCompatActivity {
    private ListView teacherListView;
    private TeacherAdapter adapter;
    private List<Teacher> teachers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        // Initialize views
        teacherListView = findViewById(R.id.teacherListView);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        // Set up adapter
        adapter = new TeacherAdapter(this, teachers);
        teacherListView.setAdapter(adapter);

        // Set click listeners
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



        // Load student data
        loadTeachers();
    }

    private void loadTeachers() {
        String url = "http://10.0.2.2/school_api/get_teachers.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            teachers.clear();
                            JSONArray teachersArray = response.getJSONArray("teachers");
                            for (int i = 0; i < teachersArray.length(); i++) {
                                JSONObject teacherObj = teachersArray.getJSONObject(i);
                                teachers.add(new Teacher(
                                        teacherObj.getInt("id"),
                                        teacherObj.getString("name"),
                                        teacherObj.getString("email"),
                                        teacherObj.getString("password")
                                ));
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            showError(response.optString("message", "Failed to load teachers"));
                        }
                    } catch (JSONException e) {
                        showError("Error parsing teacher data");
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

    public void deleteTeacher(int teacherId) {
        String url = "http://10.0.2.2/school_api/delete_teacher.php";

        // Show loading indicator
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting teacher...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            // Create JSON payload
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("teacher_id", teacherId); // Must match PHP expected parameter

            // Log the request for debugging
            Log.d("DELETE_REQUEST", "Endpoint: " + url);
            Log.d("DELETE_REQUEST", "Teacher ID: " + teacherId);
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
                                    loadTeachers(); // Refresh the list
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