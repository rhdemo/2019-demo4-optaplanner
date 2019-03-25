package com.redhat.demo.optaplanner;

public class Machine {

    private int machineIndex;
    private int x;
    private int y;
    private double[] machineIndexToTravelDistances;
    private double health;

    public Machine() {
    }

    public Machine(int machineIndex, int x, int y, double[] machineIndexToTravelDistances, double health) {
        this.machineIndex = machineIndex;
        this.x = x;
        this.y = y;
        this.machineIndexToTravelDistances = machineIndexToTravelDistances;
        this.health = health;
    }

    public int getMachineIndex() {
        return machineIndex;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public double[] getMachineIndexToTravelDistances() {
        return machineIndexToTravelDistances;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Machine)) {
            return false;
        }
        Machine machine = (Machine) o;
        return machineIndex == machine.machineIndex;
    }

    @Override
    public int hashCode() {
        return machineIndex;
    }
}
