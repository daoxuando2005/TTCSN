package com.examschedule.models;

/**
 * Dùng để biểu thị việc phân công một kỳ thi (hoặc một phần của kỳ thi) vào một khung thời gian và phòng thi.
 */
public class Assignment {
    private String examId;
    private String timeslot;
    private String room;
    private int studentCount; 

    public Assignment(String examId, String timeslot, String room, int studentCount) {
        this.examId = examId;
        this.timeslot = timeslot;
        this.room = room;
        this.studentCount = studentCount;
    }

    public String getExamId() {
        return examId;
    }

    public String getTimeslot() {
        return timeslot;
    }

    public String getRoom() {
        return room;
    }

    public int getStudentCount() {
        return studentCount;
    }

    @Override
    public String toString() {
        return "Assignment{" + "examId='" + examId + '\'' + ", timeslot='" + timeslot + '\'' + ", room='" + room + '\'' + ", students=" + studentCount + '}';
    }
}