package com.examschedule.utils;

import com.examschedule.models.*;
import java.util.*;

public class ExamSplitter {

    public static List<Assignment> splitExamIntoRooms(Exam exam, String timeslot, 
                                                      List<Room> availableRooms) {
        List<Assignment> assignments = new ArrayList<>();
        int totalStudents = exam.getStudentCount();
        
        List<Room> sortedRooms = new ArrayList<>(availableRooms);
        sortedRooms.sort((r1, r2) -> Integer.compare(r2.getCapacity(), r1.getCapacity()));
        
        if (totalStudents <= sortedRooms.get(0).getCapacity()) {
            assignments.add(new Assignment(exam.getId(), timeslot, sortedRooms.get(0).getId(), totalStudents));
            return assignments;
        }
        
        int minRoomsNeeded = (totalStudents + sortedRooms.get(0).getCapacity() - 1) / sortedRooms.get(0).getCapacity();
        
        List<Room> usedRooms = sortedRooms.subList(0, Math.min(minRoomsNeeded, sortedRooms.size()));
        
        int numRoomsUsed = usedRooms.size();
        int baseStudentsPerRoom = totalStudents / numRoomsUsed;
        int extraStudents = totalStudents % numRoomsUsed;
        
        for (int i = 0; i < usedRooms.size(); i++) {
            Room room = usedRooms.get(i);
            
            int studentsInThisRoom = baseStudentsPerRoom + (i < extraStudents ? 1 : 0);
            
            if (studentsInThisRoom > room.getCapacity()) {
                studentsInThisRoom = room.getCapacity();
            }
            
            if (studentsInThisRoom > 0) {
                assignments.add(new Assignment(exam.getId(), timeslot, room.getId(), studentsInThisRoom));
            }
        }
        
        return assignments;
    }

    public static int getTotalCapacity(List<Room> rooms) {
        return rooms.stream().mapToInt(Room::getCapacity).sum();
    }

    public static boolean canFitAllExams(List<Exam> exams, List<Room> rooms) {
        int totalStudents = exams.stream().mapToInt(Exam::getStudentCount).sum();
        return getTotalCapacity(rooms) >= totalStudents;
    }
}
