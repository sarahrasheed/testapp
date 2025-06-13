package com.example.testapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {
    private final Context context;
    private final List<Student> students;

    public StudentAdapter(Context context, List<Student> students) {
        super(context, R.layout.student_list_item, students);
        this.context = context;
        this.students = students;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.student_list_item, parent, false);
        }

        Student currentStudent = students.get(position); // Renamed to avoid conflict

        // Initialize views
        TextView tvName = convertView.findViewById(R.id.tvStudentName);
        TextView tvId = convertView.findViewById(R.id.tvStudentId);
        ImageButton btnEdit = convertView.findViewById(R.id.btnEdit);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDelete);

        // Set data
        tvName.setText(currentStudent.getName());
        tvId.setText("ID: " + currentStudent.getId());

        // Set button click listeners
        btnEdit.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, EditStudentActivity.class);
                // Pass all required data
                intent.putExtra("STUDENT_ID", currentStudent.getId());
                intent.putExtra("STUDENT_NAME", currentStudent.getName());
                intent.putExtra("STUDENT_EMAIL", currentStudent.getEmail());
                intent.putExtra("STUDENT_CLASS_ID", currentStudent.getClassId());

                // Add debug logging
                Log.d("ADAPTER", "Passing student data: " +
                        "ID=" + currentStudent.getId() +
                        ", Name=" + currentStudent.getName());

                context.startActivity(intent);
            } catch (Exception e) {
                Log.e("ADAPTER", "Edit click error", e);
                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (context instanceof StudentListActivity) {
                ((StudentListActivity) context).deleteStudent(currentStudent.getId());
            }
        });

        return convertView;
    }
}