package com.redhat.demo.optaplanner.restapi;

public class UpdateMachineHealthsResponse extends AbstractResponse {
    private double[] machineHealths;

    public UpdateMachineHealthsResponse() {
        super(ResponseType.UPDATE_MACHINE_HEALTHS);
    }

    public UpdateMachineHealthsResponse(double[] machineHealths) {
        super(ResponseType.UPDATE_MACHINE_HEALTHS);
        this.machineHealths = machineHealths;
    }

    public double[] getMachineHealths() {
        return machineHealths;
    }

    public void setMachineHealths(double[] machineHealths) {
        this.machineHealths = machineHealths;
    }
}
