package com.example.testapp;

// SchoolClass.java (separate file in your models package)
public class SchoolClass {
    private int id;
    private String className;

    public SchoolClass(int id, String className) {
        this.id = id;
        this.className = className;
    }

    public int getId() { return id; }
    public String getClassName() { return className; }

    @Override
    public String toString() {
        return className; // What displays in the spinner
    }
}