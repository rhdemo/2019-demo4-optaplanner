package com.redhat.demo.optaplanner;

import java.util.List;

public interface DownstreamConnector {

    void dispatchMechanic(Mechanic mechanic, long currentMillis);

    void mechanicAdded(Mechanic mechanic, long currentMillis);

    void mechanicRemoved(Mechanic mechanic);

    void updateMachinesHealths(Machine[] machines);

    void connect(Machine[] machines, List<Mechanic> mechanics, long currentMillis);
}
