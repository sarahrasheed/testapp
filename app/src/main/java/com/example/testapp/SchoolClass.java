package com.example.testapp;

// SchoolClass.java (separate file in your models package)
public class SchoolClass {
    private int id;
    private String name;

    public SchoolClass(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name; // This determines what's displayed in the Spinner
    }
}