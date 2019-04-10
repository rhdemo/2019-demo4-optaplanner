package com.redhat.demo.optaplanner.upstream.utils;

public class OptaPlannerConfig {
    private boolean dispatchActive;
    private boolean simulationActive;

    public OptaPlannerConfig() {
    }

    public OptaPlannerConfig(boolean dispatchActive, boolean simulationActive) {
        this.dispatchActive = dispatchActive;
        this.simulationActive = simulationActive;
    }

    public boolean isDispatchActive() {
        return dispatchActive;
    }

    public void setDispatchActive(boolean dispatchActive) {
        this.dispatchActive = dispatchActive;
    }

    public boolean isSimulationActive() {
        return simulationActive;
    }

    public void setSimulationActive(boolean simulationActive) {
        this.simulationActive = simulationActive;
    }
}
