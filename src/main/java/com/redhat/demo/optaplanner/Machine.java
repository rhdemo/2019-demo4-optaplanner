package com.redhat.demo.optaplanner;

public class Machine {

    private int machineIndex;
    private int x;
    private int y;
    private double[] machineIndexToTravelDistances;

    private boolean gate;
    private double health;

    public Machine() {
    }

    public Machine(int machineIndex, int x, int y, double[] machineIndexToTravelDistances, boolean gate) {
        this(machineIndex, x, y, machineIndexToTravelDistances, gate, Double.NaN);
        if (!gate) {
            throw new IllegalArgumentException("The parameter gate must be true.");
        }
    }

    public Machine(int machineIndex, int x, int y, double[] machineIndexToTravelDistances, double health) {
        this(machineIndex, x, y, machineIndexToTravelDistances, false, health);
    }

    private Machine(int machineIndex, int x, int y, double[] machineIndexToTravelDistances, boolean gate, double health) {
        this.machineIndex = machineIndex;
        this.x = x;
        this.y = y;
        this.machineIndexToTravelDistances = machineIndexToTravelDistances;
        this.gate = gate;
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

    public boolean isGate() {
        return gate;
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
