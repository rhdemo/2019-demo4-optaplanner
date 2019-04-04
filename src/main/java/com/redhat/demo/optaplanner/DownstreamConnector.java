package com.redhat.demo.optaplanner;

public interface DownstreamConnector {

    void dispatchMechanic(Mechanic mechanic, long currentMillis);

    void mechanicAdded(Mechanic mechanic, long currentMillis);

    void mechanicRemoved(Mechanic mechanic);

    void updateMachinesHealths(Machine[] machines);

    void sendMachineLocations(Machine[] machines);
}
