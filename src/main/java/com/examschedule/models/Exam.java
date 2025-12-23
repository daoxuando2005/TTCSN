package com.examschedule.models;

import java.util.List;

public class Exam {
    private String id;
    private List<String> students;

    public Exam(String id, List<String> students) {
        this.id = id;
        this.students = students;
    }

    public String getId() {
        return id;
    }

    public List<String> getStudents() {
        return students;
    }

    public int getStudentCount() {
        return students.size();
    }

    @Override
    public String toString() {
        return "Exam{" + "id='" + id + '\'' + ", studentCount=" + students.size() + '}';
    }
}
