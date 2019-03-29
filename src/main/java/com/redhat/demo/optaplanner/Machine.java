package com.redhat.demo.optaplanner;

public class Machine {

    public static Machine createMachine(int machineIndex,
                                        int x,
                                        int y,
                                        double[] machineIndexToTravelDistances,
                                        double health) {
        return new Machine(machineIndex, x, y, machineIndexToTravelDistances, health);
    }

    public static Machine createGate(int machineIndex,
                                     int x,
                                     int y,
                                     double[] machineIndexToTravelDistances) {
        return new Machine(machineIndex, x, y, machineIndexToTravelDistances);
    }

    private int machineIndex;
    private int x;
    private int y;
    private double[] machineIndexToTravelDistances;

    private boolean gate;
    private double health;

    private Machine(int machineIndex, int x, int y, double[] machineIndexToTravelDistances) {
        this(machineIndex, x, y, machineIndexToTravelDistances, true, Double.NaN);
    }

    private Machine(int machineIndex, int x, int y, double[] machineIndexToTravelDistances, double health) {
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
        this.health = Math.max(0.0, health);
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
