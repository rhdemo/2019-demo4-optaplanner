package com.redhat.demo.optaplanner.simulation;

public class DamageMachine {

    private int machineIndex;
    private double amount;

    @SuppressWarnings("unused")
    public DamageMachine() {}

    public DamageMachine(int machineIndex, double amount) {
        this.machineIndex = machineIndex;
        this.amount = amount;
    }

    public int getMachineIndex() {
        return machineIndex;
    }

    public double getAmount() {
        return amount;
    }
}
