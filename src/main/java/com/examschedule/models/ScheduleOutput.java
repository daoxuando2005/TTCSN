package com.examschedule.models;

import java.util.List;

/**
 * Represents the output schedule with assignments and fitness score.
 */
public class ScheduleOutput {
    private List<Assignment> schedule;
    private double fitness;

    public ScheduleOutput(List<Assignment> schedule, double fitness) {
        this.schedule = schedule;
        this.fitness = fitness;
    }

    public List<Assignment> getSchedule() {
        return schedule;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
