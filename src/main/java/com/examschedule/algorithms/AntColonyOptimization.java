package com.examschedule.algorithms;

import com.examschedule.models.*;
import com.examschedule.utils.ScheduleFitness;
import com.examschedule.utils.ExamSplitter;
import java.util.*;

/**
 * Ant Colony Optimization algorithm for exam scheduling.
 */
public class AntColonyOptimization {
    private ScheduleData data;
    private ScheduleFitness fitness;
    private Random random;

    // ACO parameters
    private int numAnts;
    private int maxIterations;
    private double alpha;      // Pheromone influence
    private double beta;       // Heuristic influence
    private double evaporation; // Pheromone evaporation rate
    private double pheromoneDeposit;

    // Pheromone matrix: τ[exam_index][timeslot_index][room_index]
    private double[][][] pheromone;

    // Heuristic matrix: η[exam_index][timeslot_index][room_index]
    private double[][][] heuristic;

    private List<Assignment> bestSchedule;
    private double bestFitness;

    /**
     * Initializes ACO solver.
     */
    public AntColonyOptimization(ScheduleData data, int numAnts, int maxIterations) {
        this.data = data;
        this.fitness = new ScheduleFitness(data);
        this.random = new Random();
        this.numAnts = numAnts;
        this.maxIterations = maxIterations;

        // ACO parameters
        this.alpha = 0.5;
        this.beta = 1;
        this.evaporation = 1;
        this.pheromoneDeposit = 0.1;

        this.bestSchedule = new ArrayList<>();
        this.bestFitness = Double.MAX_VALUE;

        initializeMatrices();
    }

    /**
     * Initializes pheromone and heuristic matrices.
     */
    private void initializeMatrices() {
        int numExams = data.getExams().size();
        int numTimeslots = data.getTimeslots().size();
        int numRooms = data.getRooms().size();

        pheromone = new double[numExams][numTimeslots][numRooms];
        heuristic = new double[numExams][numTimeslots][numRooms];

        // Initialize pheromone with small value
        for (int i = 0; i < numExams; i++) {
            for (int j = 0; j < numTimeslots; j++) {
                for (int k = 0; k < numRooms; k++) {
                    pheromone[i][j][k] = 1.0;
                    Exam exam = data.getExams().get(i);
                    Room room = data.getRooms().get(k);
                    
                    double timeslotPreference = (double)(numTimeslots - j) / numTimeslots * 2.0;
                    
                    if (exam.getStudentCount() <= room.getCapacity()) {
                        // Better heuristic for better-fit rooms
                        double utilization = (double) exam.getStudentCount() / room.getCapacity();
                        heuristic[i][j][k] = utilization * timeslotPreference;
                    } else {
                        heuristic[i][j][k] = 0.0; // Invalid option
                    }
                }
            }
        }
        System.out.println("[ACO] Matrices initialized");
    }

    /**
     * Runs the ACO algorithm.
     */
    public ScheduleOutput solve() {
        System.out.println("[ACO] Starting optimization with " + numAnts + " ants, " + maxIterations + " iterations");

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            List<List<Assignment>> antSolutions = new ArrayList<>();

            // Each ant constructs a solution
            for (int ant = 0; ant < numAnts; ant++) {
                List<Assignment> schedule = constructSchedule();
                antSolutions.add(schedule);

                double schedulesFitness = fitness.calculateFitness(schedule);
                if (schedulesFitness < bestFitness) {
                    bestFitness = schedulesFitness;
                    bestSchedule = new ArrayList<>(schedule);
                }
            }

            // Update pheromone
            updatePheromone(antSolutions);

            if ((iteration + 1) % 10 == 0) {
                System.out.println("[ACO] Iteration " + (iteration + 1) + "/" + maxIterations + " - Best fitness: " + bestFitness);
            }
        }

        System.out.println("[ACO] Optimization completed");
        return new ScheduleOutput(bestSchedule, bestFitness);
    }

    /**
     * Constructs a schedule for one ant using roulette wheel selection with exam splitting.
     * Modified to track room usage per timeslot and prevent room conflicts
     */
    private List<Assignment> constructSchedule() {
        List<Assignment> schedule = new ArrayList<>();
        
        Map<String, Set<String>> usedRoomsPerTimeslot = new HashMap<>();
        for (String timeslot : data.getTimeslots()) {
            usedRoomsPerTimeslot.put(timeslot, new HashSet<>());
        }
        
        List<Integer> examOrder = new ArrayList<>();
        for (int i = 0; i < data.getExams().size(); i++) {
            examOrder.add(i);
        }
        Collections.shuffle(examOrder);

        for (int examIdx : examOrder) {
            Exam exam = data.getExams().get(examIdx);
            
            int[] assignment = selectAssignment(examIdx, usedRoomsPerTimeslot);
            String timeslot = data.getTimeslots().get(assignment[0]);
            
            Room selectedRoom = data.getRooms().get(assignment[1]);
            if (exam.getStudentCount() <= selectedRoom.getCapacity()) {
                // Fits in single room
                schedule.add(new Assignment(exam.getId(), timeslot, selectedRoom.getId(), exam.getStudentCount()));
                usedRoomsPerTimeslot.get(timeslot).add(selectedRoom.getId());
            } else {
                Set<String> usedRooms = usedRoomsPerTimeslot.get(timeslot);
                List<Room> availableRooms = new ArrayList<>();
                for (Room room : data.getRooms()) {
                    if (!usedRooms.contains(room.getId())) {
                        availableRooms.add(room);
                    }
                }
                
                if (availableRooms.isEmpty()) {
                    // Find a timeslot with available rooms
                    for (String alternateTimeslot : data.getTimeslots()) {
                        Set<String> altUsedRooms = usedRoomsPerTimeslot.get(alternateTimeslot);
                        availableRooms.clear();
                        for (Room room : data.getRooms()) {
                            if (!altUsedRooms.contains(room.getId())) {
                                availableRooms.add(room);
                            }
                        }
                        if (!availableRooms.isEmpty()) {
                            timeslot = alternateTimeslot;
                            break;
                        }
                    }
                }
                
                if (!availableRooms.isEmpty()) {
                    List<Assignment> splitAssignments = ExamSplitter.splitExamIntoRooms(exam, timeslot, availableRooms);
                    schedule.addAll(splitAssignments);
                    for (Assignment assign : splitAssignments) {
                        usedRoomsPerTimeslot.get(timeslot).add(assign.getRoom());
                    }
                } 
            }
        }

        return schedule;
    }

    /**
     * Selects timeslot and room using roulette wheel selection.
     * Modified to avoid rooms that are already used in a timeslot
     */
    private int[] selectAssignment(int examIdx, Map<String, Set<String>> usedRoomsPerTimeslot) {
        int numTimeslots = data.getTimeslots().size();
        int numRooms = data.getRooms().size();
        Exam exam = data.getExams().get(examIdx);

        double[] probabilities = new double[numTimeslots * numRooms];
        double totalProbability = 0;

        for (int t = 0; t < numTimeslots; t++) {
            String timeslot = data.getTimeslots().get(t);
            Set<String> usedRooms = usedRoomsPerTimeslot.get(timeslot);
            
            for (int r = 0; r < numRooms; r++) {
                Room room = data.getRooms().get(r);
                
                if (!usedRooms.contains(room.getId()) && exam.getStudentCount() <= room.getCapacity()) {
                    int idx = t * numRooms + r;
                    double pheromoneValue = Math.pow(pheromone[examIdx][t][r], alpha);
                    double heuristicValue = Math.pow(heuristic[examIdx][t][r], beta);
                    
                    double timeslotBias = Math.pow(10.0, numTimeslots - t);
                    
                    probabilities[idx] = pheromoneValue * heuristicValue * timeslotBias;
                    totalProbability += probabilities[idx];
                } else {
                    probabilities[t * numRooms + r] = 0;
                }
            }
        }

        // If no valid assignments found, find any available room in any timeslot
        if (totalProbability == 0) {
            
            for (int t = 0; t < numTimeslots; t++) {
                String timeslot = data.getTimeslots().get(t);
                Set<String> usedRooms = usedRoomsPerTimeslot.get(timeslot);
                
                for (int r = 0; r < numRooms; r++) {
                    Room room = data.getRooms().get(r);
                    if (!usedRooms.contains(room.getId())) {
                        return new int[]{t, r};
                    }
                }
            }
            
            // Last resort: return random available room
            System.out.println("[WARNING] All rooms busy, returning first available");
            return new int[]{0, 0};
        }

        // Roulette wheel selection
        double spin = random.nextDouble() * totalProbability;
        double accumulated = 0;
        for (int i = 0; i < probabilities.length; i++) {
            accumulated += probabilities[i];
            if (spin <= accumulated) {
                return new int[]{i / numRooms, i % numRooms};
            }
        }

        return new int[]{random.nextInt(numTimeslots), random.nextInt(numRooms)};
    }

    /**
     * Updates pheromone based on ant solutions (evaporation + deposition).
     */
    private void updatePheromone(List<List<Assignment>> antSolutions) {
        // Evaporation
        int numExams = data.getExams().size();
        int numTimeslots = data.getTimeslots().size();
        int numRooms = data.getRooms().size();

        for (int i = 0; i < numExams; i++) {
            for (int j = 0; j < numTimeslots; j++) {
                for (int k = 0; k < numRooms; k++) {
                    pheromone[i][j][k] *= (1 - evaporation);
                }
            }
        }

        // Pheromone deposition from best solutions
        for (List<Assignment> solution : antSolutions) {
            double solutionFitness = fitness.calculateFitness(solution);
            // Only deposit pheromone for good solutions
            if (solutionFitness < bestFitness * 1.5) {
                depositPheromone(solution, 1.0 / (1 + solutionFitness));
            }
        }
    }

    /**
     * Deposits pheromone for a solution.
     */
    private void depositPheromone(List<Assignment> solution, double amount) {
        for (Assignment assignment : solution) {
            Exam exam = data.getExamById(assignment.getExamId());
            int examIdx = data.getExams().indexOf(exam);
            int timeslotIdx = data.getTimeslots().indexOf(assignment.getTimeslot());
            Room room = data.getRoomById(assignment.getRoom());
            int roomIdx = data.getRooms().indexOf(room);

            if (examIdx >= 0 && timeslotIdx >= 0 && roomIdx >= 0) {
                pheromone[examIdx][timeslotIdx][roomIdx] += amount * pheromoneDeposit;
            }
        }
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public List<Assignment> getBestSchedule() {
        return bestSchedule;
    }
}
