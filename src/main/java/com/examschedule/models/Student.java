package com.examschedule.models;

/**
 * Represents a student.
 */
public class Student {
    private String id;

    public Student(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Student{" + "id='" + id + '\'' + '}';
    }
}
