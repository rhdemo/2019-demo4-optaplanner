package com.redhat.demo.optaplanner;

public class Machine {

    private int machineIndex;
    private long[] toMachineIndexTravelTimeMillis;
    private double health;

    public Machine() {
    }

    public Machine(int machineIndex, long[] toMachineIndexTravelTimeMillis, double health) {
        this.machineIndex = machineIndex;
        this.toMachineIndexTravelTimeMillis = toMachineIndexTravelTimeMillis;
        this.health = health;
    }

    public int getMachineIndex() {
        return machineIndex;
    }

    public void setMachineIndex(int machineIndex) {
        this.machineIndex = machineIndex;
    }

    public long[] getToMachineIndexTravelTimeMillis() {
        return toMachineIndexTravelTimeMillis;
    }

    public void setToMachineIndexTravelTimeMillis(long[] toMachineIndexTravelTimeMillis) {
        this.toMachineIndexTravelTimeMillis = toMachineIndexTravelTimeMillis;
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
