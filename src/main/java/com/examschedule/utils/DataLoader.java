package com.examschedule.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.examschedule.models.Exam;
import com.examschedule.models.Room;
import com.examschedule.models.ScheduleData;
import com.examschedule.models.Student;

public class DataLoader {
    public static ScheduleData loadFromJSON(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject jsonObject = new JSONObject(content);

            List<Exam> exams = loadExams(jsonObject.getJSONArray("exams"));
            List<Student> students = loadStudents(jsonObject.getJSONArray("students"));
            List<Room> rooms = loadRooms(jsonObject.getJSONArray("rooms"));
            List<String> timeslots = loadTimeslots(jsonObject.getJSONArray("timeslots"));

            System.out.println("[DataLoader] Successfully loaded schedule data");
            return new ScheduleData(exams, students, rooms, timeslots);
        } catch (Exception e) {
            System.err.println("[DataLoader] Error loading JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static List<Exam> loadExams(JSONArray examsArray) {
        List<Exam> exams = new ArrayList<>();
        for (int i = 0; i < examsArray.length(); i++) {
            JSONObject examObj = examsArray.getJSONObject(i);
            String id = examObj.getString("id");
            JSONArray studentsArray = examObj.getJSONArray("students");
            List<String> students = new ArrayList<>();
            for (int j = 0; j < studentsArray.length(); j++) {
                students.add(studentsArray.getString(j));
            }
            exams.add(new Exam(id, students));
        }
        System.out.println("[DataLoader] Loaded " + exams.size() + " exams");
        return exams;
    }

    private static List<Student> loadStudents(JSONArray studentsArray) {
        List<Student> students = new ArrayList<>();
        for (int i = 0; i < studentsArray.length(); i++) {
            JSONObject studentObj = studentsArray.getJSONObject(i);
            String id = studentObj.getString("id");
            students.add(new Student(id));
        }
        System.out.println("[DataLoader] Loaded " + students.size() + " students");
        return students;
    }

    private static List<Room> loadRooms(JSONArray roomsArray) {
        List<Room> rooms = new ArrayList<>();
        for (int i = 0; i < roomsArray.length(); i++) {
            JSONObject roomObj = roomsArray.getJSONObject(i);
            String id = roomObj.getString("id");
            int capacity = roomObj.getInt("capacity");
            rooms.add(new Room(id, capacity));
        }
        System.out.println("[DataLoader] Loaded " + rooms.size() + " rooms");
        return rooms;
    }

    private static List<String> loadTimeslots(JSONArray timeslotsArray) {
        List<String> timeslots = new ArrayList<>();
        for (int i = 0; i < timeslotsArray.length(); i++) {
            timeslots.add(timeslotsArray.getString(i));
        }
        System.out.println("[DataLoader] Loaded " + timeslots.size() + " timeslots");
        return timeslots;
    }
}