package com.redhat.demo.optaplanner;

public class Mechanic {

    private int mechanicIndex;
    private double speed; // In pixels per millisecond

    private int focusMachineIndex;
    private long focusTravelTimeMillis;
    private long focusFixTimeMillis;

    private int[] futureMachineIndexes;

    public Mechanic(int mechanicIndex, double speed,
            int focusMachineIndex, long focusTravelTimeMillis, long focusFixTimeMillis) {
        this.mechanicIndex = mechanicIndex;
        this.speed = speed;
        this.focusMachineIndex = focusMachineIndex;
        this.focusTravelTimeMillis = focusTravelTimeMillis;
        this.focusFixTimeMillis = focusFixTimeMillis;
        this.futureMachineIndexes = new int[0];
    }

    public int getMechanicIndex() {
        return mechanicIndex;
    }

    public double getSpeed() {
        return speed;
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
