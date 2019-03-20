package com.redhat.demo.optaplanner;

public class Mechanic {
    private int mechanicIndex;

    private int focusMachineIndex;
    private long focusDepartureTimeMillis;

    private int[] futureMachineIndexes;

    public Mechanic(int mechanicIndex, int focusMachineIndex, long focusDepartureTimeMillis) {
        this.mechanicIndex = mechanicIndex;
        this.focusMachineIndex = focusMachineIndex;
        this.focusDepartureTimeMillis = focusDepartureTimeMillis;
        futureMachineIndexes = new int[0];
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

    public long getFocusDepartureTimeMillis() {
        return focusDepartureTimeMillis;
    }

    public void setFocusDepartureTimeMillis(long focusDepartureTimeMillis) {
        this.focusDepartureTimeMillis = focusDepartureTimeMillis;
    }

    public int[] getFutureMachineIndexes() {
        return futureMachineIndexes;
    }

    public void setFutureMachineIndexes(int[] futureMachineIndexes) {
        this.futureMachineIndexes = futureMachineIndexes;
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
