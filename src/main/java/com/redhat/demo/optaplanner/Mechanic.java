package com.redhat.demo.optaplanner;

public class Mechanic {

    private int mechanicIndex;
    private double speed; // In pixels per millisecond
    private long fixDurationMillis;
    private long thumbUpDurationMillis;
    private int originalMachineIndex;
    private int focusMachineIndex;
    private long focusTravelTimeMillis;

    private int[] futureMachineIndexes;

    public Mechanic(int mechanicIndex,
                    double speed,
                    long fixDurationMillis,
                    long thumbUpDurationMillis,
                    int originalMachineIndex,
                    int focusMachineIndex,
                    long focusTravelTimeMillis) {
        this.mechanicIndex = mechanicIndex;
        this.speed = speed;
        this.fixDurationMillis = fixDurationMillis;
        this.thumbUpDurationMillis = thumbUpDurationMillis;
        this.originalMachineIndex = originalMachineIndex;
        this.focusMachineIndex = focusMachineIndex;
        this.focusTravelTimeMillis = focusTravelTimeMillis;
        this.futureMachineIndexes = new int[0];
    }

    public int getOriginalMachineIndex() {
        return originalMachineIndex;
    }

    public void setOriginalMachineIndex(int originalMachineIndex) {
        this.originalMachineIndex = originalMachineIndex;
    }

    public int getMechanicIndex() {
        return mechanicIndex;
    }

    public double getSpeed() {
        return speed;
    }

    public long getFixDurationMillis() {
        return fixDurationMillis;
    }

    public long getThumbUpDurationMillis() {
        return thumbUpDurationMillis;
    }

    public int getFocusMachineIndex() {
        return focusMachineIndex;
    }

    public void setFocusMachineIndex(int focusMachineIndex) {
        this.focusMachineIndex = focusMachineIndex;
    }

    public long getFocusTravelTimeMillis() {
        return focusTravelTimeMillis;
    }

    public void setFocusTravelTimeMillis(long focusTravelTimeMillis) {
        this.focusTravelTimeMillis = focusTravelTimeMillis;
    }

    public int[] getFutureMachineIndexes() {
        return futureMachineIndexes;
    }

    public void setFutureMachineIndexes(int[] futureMachineIndexes) {
        this.futureMachineIndexes = futureMachineIndexes;
    }

    public long getFocusFixTimeMillis() {
        return focusTravelTimeMillis + fixDurationMillis;
    }

    public long getFocusDepartureTimeMillis() {
        return getFocusFixTimeMillis() + thumbUpDurationMillis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Mechanic)) {
            return false;
        }
        Mechanic mechanic = (Mechanic) o;
        return mechanicIndex == mechanic.mechanicIndex;
    }

    @Override
    public int hashCode() {
        return mechanicIndex;
    }
}
