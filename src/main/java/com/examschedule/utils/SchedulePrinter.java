package com.examschedule.utils;

import com.examschedule.models.*;
import java.util.*;
import java.util.stream.Collectors;

public class SchedulePrinter {

    public static void printScheduleTable(List<Assignment> schedule, ScheduleData data) {
        if (schedule == null || schedule.isEmpty()) {
            System.out.println("No schedule available.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        LỊCH THI                                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝\n");

        int examWidth = 20;
        int studentCountWidth = 18;
        int timeslotWidth = 18;
        int roomWidth = 15;

        printLine(examWidth, studentCountWidth, timeslotWidth, roomWidth);
        System.out.printf("│ %-" + (examWidth-2) + "s │ %-" + (studentCountWidth-2) + "s │ %-"
                + (timeslotWidth-2) + "s │ %-" + (roomWidth-2) + "s │\n",
                "Môn Thi", "Số Sinh Viên", "Thời Gian", "Phòng");
        printLine(examWidth, studentCountWidth, timeslotWidth, roomWidth);

        List<Assignment> sortedSchedule = schedule.stream()
                .sorted(Comparator.comparing(Assignment::getTimeslot)
                        .thenComparing(Assignment::getExamId))
                .collect(Collectors.toList());

        int totalStudents = 0;
        Set<String> uniqueTimeslots = new HashSet<>();
        Set<String> uniqueRooms = new HashSet<>();
        Map<String, Integer> examTotalStudents = new HashMap<>();

        for (Assignment assignment : sortedSchedule) {
            String examId = assignment.getExamId();
            int numStudents = assignment.getStudentCount();
            String timeslot = assignment.getTimeslot();
            String room = assignment.getRoom();

            totalStudents += numStudents;
            uniqueTimeslots.add(timeslot);
            uniqueRooms.add(room);
            examTotalStudents.merge(examId, numStudents, Integer::sum);

            System.out.printf("│ %-" + (examWidth-2) + "s │ %-" + (studentCountWidth-2) + "d │ %-"
                    + (timeslotWidth-2) + "s │ %-" + (roomWidth-2) + "s │\n",
                    examId, numStudents, timeslot, room);
        }

        printLine(examWidth, studentCountWidth, timeslotWidth, roomWidth);
        
        System.out.println("\n📊 THỐNG KÊ:");
        System.out.println("  • Tổng số môn thi: " + sortedSchedule.size());
        System.out.println("  • Tổng số sinh viên: " + totalStudents);
        System.out.println("  • Số thời gian biểu: " + uniqueTimeslots.size());
        System.out.println("  • Số phòng thi: " + uniqueRooms.size());
        System.out.println();
    }

    private static void printLine(int examWidth, int studentCountWidth, int timeslotWidth, int roomWidth) {
        System.out.print("╟");
        System.out.print("─".repeat(examWidth+2));
        System.out.print("┼");
        System.out.print("─".repeat(studentCountWidth+3));
        System.out.print("┼");
        System.out.print("─".repeat(timeslotWidth+3));
        System.out.print("┼");
        System.out.print("─".repeat(roomWidth+3));
        System.out.println("╢");
    }
}