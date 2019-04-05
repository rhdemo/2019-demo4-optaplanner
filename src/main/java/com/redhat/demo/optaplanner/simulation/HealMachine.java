package com.redhat.demo.optaplanner.simulation;

public class HealMachine {

    private int machineIndex;

    @SuppressWarnings("unused")
    public HealMachine() {

    }

    public HealMachine(int machineIndex) {
        this.machineIndex = machineIndex;
    }

    public int getMachineIndex() {
        return machineIndex;
    }
}
