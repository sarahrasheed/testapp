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

public class TeacherAdapter extends ArrayAdapter<Teacher> {
    private final Context context;
    private final List<Teacher> teachers;

    public TeacherAdapter(Context context, List<Teacher> teachers) {
        super(context, R.layout.teacher_list_item, teachers);
        this.context = context;
        this.teachers = teachers;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.teacher_list_item, parent, false);
        }

        Teacher currentTeacher = teachers.get(position); // Renamed to avoid conflict

        // Initialize views
        TextView tvName = convertView.findViewById(R.id.tvTeacherName);
        TextView tvId = convertView.findViewById(R.id.tvTeacherId);
        ImageButton btnEdit = convertView.findViewById(R.id.btnEdit);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDelete);

        // Set data
        tvName.setText(currentTeacher.getName());
        tvId.setText("ID: " + currentTeacher.getId());

        // Set button click listeners
        btnEdit.setOnClickListener(v -> {
            // Get the parent activity
            TeacherActivity activity = (TeacherActivity) context;

            // Call the showEditDialog method directly
            activity.showEditDialog(currentTeacher);
        });

        btnDelete.setOnClickListener(v -> {
            if (context instanceof TeacherActivity) {
                ((TeacherActivity) context).deleteTeacher(currentTeacher.getId());
            }
        });

        return convertView;
    }
}