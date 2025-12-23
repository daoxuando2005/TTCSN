package com.examschedule.utils;

import com.examschedule.models.*;
import java.util.*;

/**
 * Đánh giá độ phù hợp (fitness) của một lịch thi.
 * Giá trị fitness càng thấp thì lịch thi càng tốt.
 */
public class ScheduleFitness {
    private ScheduleData data;
    private List<Assignment> assignments;

    // Trọng số phạt
    private static final int CAPACITY_VIOLATION_PENALTY = 1000;
    private static final int STUDENT_CONFLICT_PENALTY = 500;
    private static final int UNASSIGNED_EXAM_PENALTY = 100;
    private static final int ROOM_CONFLICT_PENALTY = 2000;

    public ScheduleFitness(ScheduleData data) {
        this.data = data;
    }

    /**
     * Tính toán điểm fitness cho một lịch thi cho trước.
     * @param assignments Danh sách các phân công môn thi
     * @return Giá trị fitness (càng nhỏ càng tốt)
     */
    public double calculateFitness(List<Assignment> assignments) {
        this.assignments = assignments;
        double fitness = 0;

        // Phạt vi phạm sức chứa phòng
        fitness += checkCapacityViolations();

        // Phạt xung đột sinh viên (một sinh viên thi nhiều môn cùng ca)
        fitness += checkStudentConflicts();

        // Phạt các môn chưa được xếp lịch
        fitness += checkUnassignedExams();

        // Phạt xung đột phòng thi
        fitness += checkRoomConflicts();

        fitness -= getTimeslotsUsed(assignments) * 10;
        fitness += calculateTimeslotEfficiencyPenalty(assignments);

        return Math.max(0, fitness);
    }

    /**
     * Kiểm tra vi phạm sức chứa phòng thi.
     */
    private double checkCapacityViolations() {
        double penalty = 0;
        for (Assignment assignment : assignments) {
            Room room = data.getRoomById(assignment.getRoom());
            if (room != null && assignment.getStudentCount() > room.getCapacity()) {
                penalty += CAPACITY_VIOLATION_PENALTY *
                        (assignment.getStudentCount() - room.getCapacity());
            }
        }
        return penalty;
    }

    /**
     * Kiểm tra xung đột lịch thi của sinh viên.
     */
    private double checkStudentConflicts() {
        double penalty = 0;
        Map<String, Set<String>> studentTimeslots = new HashMap<>();

        for (Assignment assignment : assignments) {
            Exam exam = data.getExamById(assignment.getExamId());
            String timeslot = assignment.getTimeslot();
            if (exam != null) {
                for (String studentId : exam.getStudents()) {
                    String key = studentId + "_" + timeslot;
                    studentTimeslots
                            .computeIfAbsent(key, k -> new HashSet<>())
                            .add(exam.getId());
                }
            }
        }

        for (Set<String> examsInSlot : studentTimeslots.values()) {
            if (examsInSlot.size() > 1) {
                penalty += STUDENT_CONFLICT_PENALTY *
                        (examsInSlot.size() - 1);
            }
        }
        return penalty;
    }

    /**
     * Kiểm tra xung đột phòng thi
     * (một phòng được sử dụng nhiều lần trong cùng một ca thi).
     */
    private double checkRoomConflicts() {
        double penalty = 0;
        Map<String, List<String>> roomTimeslotUsage = new HashMap<>();

        for (Assignment assignment : assignments) {
            String key = assignment.getTimeslot() + "_" + assignment.getRoom();
            roomTimeslotUsage
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(assignment.getExamId());
        }

        for (Map.Entry<String, List<String>> entry : roomTimeslotUsage.entrySet()) {
            if (entry.getValue().size() > 1) {
                // Phạt nặng khi xảy ra xung đột phòng
                penalty += ROOM_CONFLICT_PENALTY *
                        (entry.getValue().size() - 1);
                System.out.println("[WARNING] Room conflict detected: " +
                        entry.getKey() + " used by exams: " +
                        entry.getValue());
            }
        }
        return penalty;
    }

    /**
     * Kiểm tra các môn thi chưa được xếp lịch.
     */
    private double checkUnassignedExams() {
        Set<String> assignedExams = new HashSet<>();
        for (Assignment assignment : assignments) {
            assignedExams.add(assignment.getExamId());
        }

        double penalty = 0;
        for (Exam exam : data.getExams()) {
            if (!assignedExams.contains(exam.getId())) {
                penalty += UNASSIGNED_EXAM_PENALTY;
            }
        }
        return penalty;
    }

    /**
     * Lấy số lượng ca thi đã được sử dụng.
     */
    private int getTimeslotsUsed() {
        Set<String> usedTimeslots = new HashSet<>();
        for (Assignment assignment : assignments) {
            usedTimeslots.add(assignment.getTimeslot());
        }
        return usedTimeslots.size();
    }

    /**
     * Lấy số lượng ca thi đã sử dụng từ danh sách phân công cho trước.
     */
    private int getTimeslotsUsed(List<Assignment> assignmentList) {
        Set<String> usedTimeslots = new HashSet<>();
        for (Assignment assignment : assignmentList) {
            usedTimeslots.add(assignment.getTimeslot());
        }
        return usedTimeslots.size();
    }

    /**
     * Tính mức phạt cho việc sử dụng ca thi không hiệu quả.
     * Phạt khi sử dụng ca thi muộn trong khi các ca sớm vẫn còn phòng trống.
     */
    private double calculateTimeslotEfficiencyPenalty(List<Assignment> assignments) {
        double penalty = 0;
        int numTimeslots = data.getTimeslots().size();
        int numRooms = data.getRooms().size();
        
        // Đếm số lượng phân công theo từng ca thi
        Map<String, Integer> timeslotCounts = new HashMap<>();
        for (Assignment assignment : assignments) {
            timeslotCounts.merge(
                    assignment.getTimeslot(), 1, Integer::sum);
        }
        
        // Kiểm tra việc sử dụng ca muộn khi ca sớm chưa đầy
        List<String> timeslots = data.getTimeslots();
        for (int i = 0; i < timeslots.size(); i++) {
            String currentTimeslot = timeslots.get(i);
            int currentCount =
                    timeslotCounts.getOrDefault(currentTimeslot, 0);
            
            // Nếu ca hiện tại chưa đầy nhưng ca sau đã được dùng thì phạt nặng
            if (currentCount < numRooms) {
                for (int j = i + 1; j < timeslots.size(); j++) {
                    String laterTimeslot = timeslots.get(j);
                    int laterCount =
                            timeslotCounts.getOrDefault(laterTimeslot, 0);
                    if (laterCount > 0) {
                        // Phạt nặng việc bỏ qua ca sớm để dùng ca muộn
                        penalty += 200 * laterCount * (j - i);
                    }
                }
            }
        }
        
        return penalty;
    }

    /**
     * In ra phân tích chi tiết của lịch thi.
     */
    public void printScheduleAnalysis(List<Assignment> assignments, double fitness) {
        System.out.println("\n========== SCHEDULE ANALYSIS ==========");
        System.out.println("Total Fitness Score: " + fitness);
        System.out.println("Assignments: " + assignments.size() +
                " / " + data.getExams().size());
        System.out.println("Timeslots Used: " +
                getTimeslotsUsed(assignments) +
                " / " + data.getTimeslots().size());

        Map<String, Integer> timeslotLoad = new HashMap<>();
        for (Assignment assignment : assignments) {
            timeslotLoad.merge(
                    assignment.getTimeslot(), 1, Integer::sum);
        }
        System.out.println("Timeslot Distribution: " + timeslotLoad);
        
        Map<String, List<String>> roomTimeslotUsage = new HashMap<>();
        for (Assignment assignment : assignments) {
            String key =
                    assignment.getTimeslot() + "_" + assignment.getRoom();
            roomTimeslotUsage
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(assignment.getExamId());
        }
        
        int conflictCount = 0;
        for (Map.Entry<String, List<String>> entry :
                roomTimeslotUsage.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflictCount++;
            }
        }
        System.out.println("Room Conflicts: " + conflictCount);
        System.out.println("========================================\n");
    }
}
