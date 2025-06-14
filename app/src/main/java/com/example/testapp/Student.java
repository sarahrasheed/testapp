package com.example.testapp;

import java.io.Serializable;

public class Student implements Serializable {
    private int id;
    private String name;
    private String email;
    private String password;
    private int classId;

    public Student(int id, String name, String email, String password, int classId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.classId = classId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public int getClassId() { return classId; }
}