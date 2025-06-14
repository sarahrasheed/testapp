package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        // Initialize buttons
        Button btnStudent = findViewById(R.id.btnStudent);
        Button btnTeachers = findViewById(R.id.btnTeachers);
        Button btnManageSubjects = findViewById(R.id.btnManageSubjects);
        Button btnBuildSchedules = findViewById(R.id.btnBuildSchedules);
//        Button btnViewStudent = findViewById(R.id.btnViewStudent);

        // Set click listeners
        btnStudent.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrarActivity.this, StudentActivity.class);
            startActivity(intent);
        });

//        btnViewStudent.setOnClickListener(v -> {
//            Intent intent = new Intent(RegistrarActivity.this, StudentListActivity.class);
//            startActivity(intent);
//        });

        btnTeachers.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrarActivity.this, TeacherActivity.class);
            startActivity(intent);
        });

        btnManageSubjects.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrarActivity.this, SubjectListActivity.class);
            startActivity(intent);
        });

        btnBuildSchedules.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrarActivity.this, ScheduleBuilderActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Any refresh logic can go here
    }
}