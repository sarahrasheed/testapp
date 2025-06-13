package com.example.testapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

public class SubjectListActivity extends AppCompatActivity {
    private ListView listView;
    private SubjectAdapter adapter;
    private List<Subject> subjects = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_list);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());

        listView = findViewById(R.id.subjectListView);
        adapter = new SubjectAdapter(this, subjects);
        listView.setAdapter(adapter);

        Button btnAddSubject = findViewById(R.id.btnAddSubject);
        btnAddSubject.setOnClickListener(v -> showAddSubjectDialog());

        loadSubjects();
    }

    private void loadSubjects() {
        String url = "http://10.0.2.2/school_api/get_subjects.php";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            subjects.clear();
                            JSONArray subjectsArray = response.getJSONArray("subjects");
                            for (int i = 0; i < subjectsArray.length(); i++) {
                                JSONObject obj = subjectsArray.getJSONObject(i);
                                subjects.add(new Subject(obj.getInt("id"), obj.getString("name")));
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        Log.e("SUBJECTS", "Error parsing response", e);
                    }
                },
                error -> Log.e("SUBJECTS", "Network error", error)
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Subject");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String subjectName = input.getText().toString().trim();
            if (!subjectName.isEmpty()) {
                addSubject(subjectName);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addSubject(String name) {
        String url = "http://10.0.2.2/school_api/add_subject.php";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("subject_name", name);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                loadSubjects(); // Refresh list
                            }
                        } catch (JSONException e) {
                            Log.e("ADD_SUBJECT", "Error parsing response", e);
                        }
                    },
                    error -> Log.e("ADD_SUBJECT", "Network error", error)
            );

            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            Log.e("ADD_SUBJECT", "JSON error", e);
        }
    }

    private void showAddSubjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Subject");

        // Inflate custom layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null);
        builder.setView(dialogView);

        EditText etSubjectName = dialogView.findViewById(R.id.etSubjectName);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String subjectName = etSubjectName.getText().toString().trim();
            if (!subjectName.isEmpty()) {
                addSubjectToDatabase(subjectName);
            } else {
                Toast.makeText(this, "Subject name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Make buttons visible after show()
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(ContextCompat.getColor(this, R.color.violet_blue));
        }
    }

    private void addSubjectToDatabase(String subjectName) {
        String url = "http://10.0.2.2/school_api/add_subject.php";

        // Show loading indicator
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Adding subject...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("subject_name", subjectName);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        progressDialog.dismiss();
                        try {
                            Log.d("ADD_SUBJECT", "Response: " + response.toString());
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(this, "Subject added successfully", Toast.LENGTH_SHORT).show();
                                loadSubjects(); // Refresh the list
                            } else {
                                String errorMsg = response.optString("message", "Addition failed");
                                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e("ADD_SUBJECT", "JSON parsing error", e);
                            Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        progressDialog.dismiss();
                        String errorMsg = "Network error";
                        if (error.networkResponse != null) {
                            errorMsg += " (Code: " + error.networkResponse.statusCode + ")";
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("ADD_SUBJECT", "Volley error", error);
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            // Set timeout to 15 seconds
            request.setRetryPolicy(new DefaultRetryPolicy(
                    15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            Volley.newRequestQueue(this).add(request);

        } catch (JSONException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
            Log.e("ADD_SUBJECT", "JSON exception", e);
        }
    }
}