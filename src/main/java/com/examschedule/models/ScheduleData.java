package com.examschedule.models;

import java.util.List;

/**
 * Chứa toàn bộ dữ liệu cho lịch thi
 */
public class ScheduleData {
    private List<Exam> exams;
    private List<Student> students;
    private List<Room> rooms;
    private List<String> timeslots;

    public ScheduleData(List<Exam> exams, List<Student> students, List<Room> rooms, List<String> timeslots) {
        this.exams = exams;
        this.students = students;
        this.rooms = rooms;
        this.timeslots = timeslots;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public List<Student> getStudents() {
        return students;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<String> getTimeslots() {
        return timeslots;
    }

    public Exam getExamById(String id) {
        return exams.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
    }

    public Room getRoomById(String id) {
        return rooms.stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "ScheduleData{" + "exams=" + exams.size() + ", students=" + students.size() + ", rooms=" + rooms.size() + ", timeslots=" + timeslots.size() + '}';
    }
}
