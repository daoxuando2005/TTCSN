# Exam Scheduling System using Ant Colony Optimization (ACO)

A complete Java implementation of an exam scheduling system that uses the Ant Colony Optimization algorithm to create optimal exam schedules while satisfying multiple constraints.

## Overview

This system solves the exam timetabling problem by:
- Ensuring no student has conflicting exams
- Respecting room capacity constraints
- Minimizing the total number of timeslots needed
- Distributing exams evenly across available timeslots

## Project Structure

\`\`\`
├── src/
│   ├── models/
│   │   ├── Exam.java              # Exam entity
│   │   ├── Student.java            # Student entity
│   │   ├── Room.java               # Room entity
│   │   ├── Assignment.java         # Assignment of exam to timeslot & room
│   │   ├── ScheduleData.java       # Container for all input data
│   │   └── ScheduleOutput.java     # Output schedule with fitness
│   ├── utils/
│   │   ├── DataLoader.java        # JSON data loader
│   │   └── ScheduleFitness.java   # Fitness evaluation
│   ├── algorithms/
│   │   └── AntColonyOptimization.java  # ACO algorithm
│   └── Main.java                   # Entry point
├── input.json                      # Input data file
├── schedule_output.json           # Generated output file
├── pom.xml                        # Maven configuration
└── README.md                      # This file
\`\`\`

## Algorithm Details

### Ant Colony Optimization (ACO)

The algorithm uses:
- **Pheromone Matrix (τ)**: τ[exam][timeslot][room] - strength of previous solutions
- **Heuristic Matrix (η)**: η[exam][timeslot][room] - desirability of assignment
- **Roulette Wheel Selection**: Probability proportional to τ^α × η^β

Key parameters:
- `alpha = 1.0`: Pheromone influence weight
- `beta = 2.0`: Heuristic influence weight
- `evaporation = 0.1`: Pheromone evaporation rate per iteration
- `numAnts = 20`: Number of ants per iteration
- `maxIterations = 100`: Number of optimization iterations

### Fitness Function

The fitness evaluates schedules based on:
1. **Room Capacity Violations**: Penalty = 1000 × excess students
2. **Student Conflicts**: Penalty = 500 × number of conflicting exams
3. **Unassigned Exams**: Penalty = 100 per exam
4. **Timeslot Efficiency**: Bonus = 10 × timeslots used

Lower fitness scores indicate better schedules.

## Input Format

The `input.json` file contains:
\`\`\`json
{
  "exams": [
    {"id": "E1", "students": ["S1", "S2", "S3"]},
    ...
  ],
  "students": [
    {"id": "S1"}, ...
  ],
  "rooms": [
    {"id": "R1", "capacity": 50}, ...
  ],
  "timeslots": [
    "Monday_08:00", ...
  ]
}
\`\`\`

## Running the Program

### Using Maven:
\`\`\`bash
# Compile
mvn clean compile

# Run
mvn exec:java -Dexec.mainClass="Main"
\`\`\`

### Using javac directly:
\`\`\`bash
# Compile
mkdir -p bin
javac -cp ".:lib/*" -d bin src/**/*.java

# Run
java -cp "bin:lib/*" Main
\`\`\`

### Download Dependencies:
If using javac, download org.json library:
\`\`\`bash
wget https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar -O lib/json.jar
\`\`\`

## Output

The program generates:

### Console Output:
- Data loading information
- ACO iteration progress
- Final schedule analysis (fitness, assignments, timeslot distribution)

### schedule_output.json:
\`\`\`json
{
  "schedule": [
    {"exam": "E1", "room": "R1", "timeslot": "Monday_08:00"},
    {"exam": "E2", "room": "R2", "timeslot": "Monday_10:00"},
    ...
  ],
  "fitness": 45.5
}
\`\`\`

## Customization

### Adjusting ACO Parameters (in Main.java):
\`\`\`java
int numAnts = 20;           // Increase for better exploration
int maxIterations = 100;    // More iterations for better solutions
\`\`\`

### Adjusting Fitness Penalties (in ScheduleFitness.java):
\`\`\`java
private static final int CAPACITY_VIOLATION_PENALTY = 1000;
private static final int STUDENT_CONFLICT_PENALTY = 500;
private static final int UNASSIGNED_EXAM_PENALTY = 100;
\`\`\`

### Adjusting ACO Hyperparameters (in AntColonyOptimization.java):
\`\`\`java
this.alpha = 1.0;           // Pheromone influence
this.beta = 2.0;            // Heuristic influence
this.evaporation = 0.1;     // Evaporation rate
this.pheromoneDeposit = 1.0; // Pheromone deposit amount
\`\`\`

## Example

With the provided input.json (6 exams, 10 students, 3 rooms, 5 timeslots):
\`\`\`
[ACO] Starting optimization with 20 ants, 100 iterations
[ACO] Iteration 10/100 - Best fitness: 85.3
[ACO] Iteration 20/100 - Best fitness: 42.1
...
[ACO] Optimization completed

========== SCHEDULE ANALYSIS ==========
Total Fitness Score: 35.2
Assignments: 6 / 6
Timeslots Used: 3 / 5
Timeslot Distribution: {Monday_08:00=2, Monday_10:00=2, Tuesday_08:00=2}
\`\`\`

## Performance Considerations

- **Time Complexity**: O(numAnts × maxIterations × numExams × numTimeslots × numRooms)
- **Space Complexity**: O(numExams × numTimeslots × numRooms) for pheromone matrix
- For large problems, adjust `maxIterations` and `numAnts` to balance quality vs speed

## Features

✓ Complete OOP design with clear separation of concerns  
✓ Comprehensive fitness evaluation with multiple constraint handling  
✓ Pheromone-based exploration and exploitation balance  
✓ Efficient roulette wheel selection  
✓ JSON input/output support  
✓ Detailed console logging and analysis  
✓ No infinite loops - bounded iterations  
✓ Full Java documentation  

## Requirements

- Java 11 or higher
- Maven 3.6+ (optional, for Maven builds)
- org.json library (included in pom.xml)

## License

Open source for educational purposes.
