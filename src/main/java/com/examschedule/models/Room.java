package com.examschedule.models;

public class Room {
    private String id;
    private int capacity;

    public Room(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return "Room{" + "id='" + id + '\'' + ", capacity=" + capacity + '}';
    }
}
