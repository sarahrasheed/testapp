package com.example.testapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class SubjectAdapter extends ArrayAdapter<Subject> {
    public SubjectAdapter(Context context, List<Subject> subjects) {
        super(context, R.layout.item_subject, subjects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_subject, parent, false);
        }

        Subject subject = getItem(position);
        TextView tvName = convertView.findViewById(R.id.tvSubjectName);
        tvName.setText(subject.getName());

        return convertView;
    }
}