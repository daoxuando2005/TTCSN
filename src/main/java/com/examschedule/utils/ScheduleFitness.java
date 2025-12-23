package com.examschedule.utils;

import com.examschedule.models.*;
import java.util.*;


/**
 * Evaluates the fitness (quality) of an exam schedule.
 * Lower fitness scores are better.
 */
public class ScheduleFitness {
    private ScheduleData data;
    private List<Assignment> assignments;

    // Penalty weights
    private static final int CAPACITY_VIOLATION_PENALTY = 1000;
    private static final int STUDENT_CONFLICT_PENALTY = 500;
    private static final int UNASSIGNED_EXAM_PENALTY = 100;
    private static final int ROOM_CONFLICT_PENALTY = 2000;

    public ScheduleFitness(ScheduleData data) {
        this.data = data;
    }

    /**
     * Calculates the fitness score for a given schedule.
     * @param assignments List of exam assignments
     * @return Fitness score (lower is better)
     */
    public double calculateFitness(List<Assignment> assignments) {
        this.assignments = assignments;
        double fitness = 0;

        // Penalty for capacity violations
        fitness += checkCapacityViolations();

        // Penalty for student conflicts (same student in multiple exams at same timeslot)
        fitness += checkStudentConflicts();

        // Penalty for unassigned exams
        fitness += checkUnassignedExams();

        fitness += checkRoomConflicts();

        fitness -= getTimeslotsUsed(assignments) * 10;
        fitness += calculateTimeslotEfficiencyPenalty(assignments);

        return Math.max(0, fitness);
    }

    /**
     * Checks if room capacity is violated.
     */
    private double checkCapacityViolations() {
        double penalty = 0;
        for (Assignment assignment : assignments) {
            Room room = data.getRoomById(assignment.getRoom());
            if (room != null && assignment.getStudentCount() > room.getCapacity()) {
                penalty += CAPACITY_VIOLATION_PENALTY * (assignment.getStudentCount() - room.getCapacity());
            }
        }
        return penalty;
    }

    /**
     * Checks for student scheduling conflicts.
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
                    studentTimeslots.computeIfAbsent(key, k -> new HashSet<>()).add(exam.getId());
                }
            }
        }

        for (Set<String> examsInSlot : studentTimeslots.values()) {
            if (examsInSlot.size() > 1) {
                penalty += STUDENT_CONFLICT_PENALTY * (examsInSlot.size() - 1);
            }
        }
        return penalty;
    }

    /**
     * Checks for room conflicts (same room used multiple times in same timeslot).
     */
    private double checkRoomConflicts() {
        double penalty = 0;
        Map<String, List<String>> roomTimeslotUsage = new HashMap<>();

        for (Assignment assignment : assignments) {
            String key = assignment.getTimeslot() + "_" + assignment.getRoom();
            roomTimeslotUsage.computeIfAbsent(key, k -> new ArrayList<>()).add(assignment.getExamId());
        }

        for (Map.Entry<String, List<String>> entry : roomTimeslotUsage.entrySet()) {
            if (entry.getValue().size() > 1) {
                // Heavy penalty for room conflicts
                penalty += ROOM_CONFLICT_PENALTY * (entry.getValue().size() - 1);
                System.out.println("[WARNING] Room conflict detected: " + entry.getKey() + 
                                 " used by exams: " + entry.getValue());
            }
        }
        return penalty;
    }

    /**
     * Checks for unassigned exams.
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
     * Gets the number of timeslots used.
     */
    private int getTimeslotsUsed() {
        Set<String> usedTimeslots = new HashSet<>();
        for (Assignment assignment : assignments) {
            usedTimeslots.add(assignment.getTimeslot());
        }
        return usedTimeslots.size();
    }

    /**
     * Gets the number of timeslots used from a given assignment list.
     */
    private int getTimeslotsUsed(List<Assignment> assignmentList) {
        Set<String> usedTimeslots = new HashSet<>();
        for (Assignment assignment : assignmentList) {
            usedTimeslots.add(assignment.getTimeslot());
        }
        return usedTimeslots.size();
    }

    /**
     * Calculates penalty for inefficient timeslot usage
     * Penalizes using later timeslots when earlier ones have free rooms
     */
    private double calculateTimeslotEfficiencyPenalty(List<Assignment> assignments) {
        double penalty = 0;
        int numTimeslots = data.getTimeslots().size();
        int numRooms = data.getRooms().size();
        
        // Count assignments per timeslot
        Map<String, Integer> timeslotCounts = new HashMap<>();
        for (Assignment assignment : assignments) {
            timeslotCounts.merge(assignment.getTimeslot(), 1, Integer::sum);
        }
        
        // Check if later timeslots are used when earlier ones are not full
        List<String> timeslots = data.getTimeslots();
        for (int i = 0; i < timeslots.size(); i++) {
            String currentTimeslot = timeslots.get(i);
            int currentCount = timeslotCounts.getOrDefault(currentTimeslot, 0);
            
            // If this timeslot is not full but later timeslots are used, penalize heavily
            if (currentCount < numRooms) {
                for (int j = i + 1; j < timeslots.size(); j++) {
                    String laterTimeslot = timeslots.get(j);
                    int laterCount = timeslotCounts.getOrDefault(laterTimeslot, 0);
                    if (laterCount > 0) {
                        // Heavy penalty for skipping to later timeslots
                        penalty += 200 * laterCount * (j - i);
                    }
                }
            }
        }
        
        return penalty;
    }

    /**
     * Prints detailed schedule analysis.
     */
    public void printScheduleAnalysis(List<Assignment> assignments, double fitness) {
        System.out.println("\n========== SCHEDULE ANALYSIS ==========");
        System.out.println("Total Fitness Score: " + fitness);
        System.out.println("Assignments: " + assignments.size() + " / " + data.getExams().size());
        System.out.println("Timeslots Used: " + getTimeslotsUsed(assignments) + " / " + data.getTimeslots().size());

        Map<String, Integer> timeslotLoad = new HashMap<>();
        for (Assignment assignment : assignments) {
            timeslotLoad.merge(assignment.getTimeslot(), 1, Integer::sum);
        }
        System.out.println("Timeslot Distribution: " + timeslotLoad);
        
        Map<String, List<String>> roomTimeslotUsage = new HashMap<>();
        for (Assignment assignment : assignments) {
            String key = assignment.getTimeslot() + "_" + assignment.getRoom();
            roomTimeslotUsage.computeIfAbsent(key, k -> new ArrayList<>()).add(assignment.getExamId());
        }
        
        int conflictCount = 0;
        for (Map.Entry<String, List<String>> entry : roomTimeslotUsage.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflictCount++;
            }
        }
        System.out.println("Room Conflicts: " + conflictCount);
        System.out.println("========================================\n");
    }
}
