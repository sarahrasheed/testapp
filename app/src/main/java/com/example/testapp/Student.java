package com.example.testapp;

public class Student {
    private int id;
    private String name;
    private String email;
    private int classId;

    public Student(int id, String name, String email, int classId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.classId = classId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getClassId() { return classId; }
}