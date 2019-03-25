package com.redhat.demo.optaplanner;

public class Mechanic {
    private int mechanicIndex;

    private int focusMachineIndex;
    private long focusTravelTimeMillis;
    private long focusFixTimeMillis;
    private double speed;

    private int[] futureMachineIndexes;

    public Mechanic(int mechanicIndex, int focusMachineIndex, long focusTravelTimeMillis, long focusFixTimeMillis) {
        this.mechanicIndex = mechanicIndex;
        this.focusMachineIndex = focusMachineIndex;
        this.focusTravelTimeMillis = focusTravelTimeMillis;
        this.focusFixTimeMillis = focusFixTimeMillis;
        this.futureMachineIndexes = new int[0];
    }

    public int getMechanicIndex() {
        return mechanicIndex;
    }

    public void setMechanicIndex(int mechanicIndex) {
        this.mechanicIndex = mechanicIndex;
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

    public long getFocusFixTimeMillis() {
        return focusFixTimeMillis;
    }

    public void setFocusFixTimeMillis(long focusFixTimeMillis) {
        this.focusFixTimeMillis = focusFixTimeMillis;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int[] getFutureMachineIndexes() {
        return futureMachineIndexes;
    }

    public void setFutureMachineIndexes(int[] futureMachineIndexes) {
        this.futureMachineIndexes = futureMachineIndexes;
    }

    public long getFocusDepartureTimeMillis() {
        return focusFixTimeMillis + AppConstants.BREATHING_TIME_MILLIS;
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
