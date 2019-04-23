package com.redhat.demo.optaplanner.upstream;

import com.redhat.demo.optaplanner.Mechanic;
import com.redhat.demo.optaplanner.SpringProfiles;
import com.redhat.demo.optaplanner.config.AppConfiguration;
import com.redhat.demo.optaplanner.upstream.utils.GameConfigListener;
import org.infinispan.client.hotrod.exceptions.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
@Profile(SpringProfiles.PRODUCTION)
public class ReconnectingInfinispanConnector implements UpstreamConnector {

    private static final long RECONNECT_WAIT_MILLIS = 3_000L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReconnectingInfinispanConnector.class);

    @Autowired
    private AppConfiguration appConfiguration;

    @Autowired
    private GameConfigListener gameConfigListener;

    private InfinispanConnector infinispanConnector;

    @PostConstruct
    public void connect() {
        boolean successfullyConnected = internalConnect();

        while (!successfullyConnected) { // Infinispan is not available when OptaPlanner is starting.
            successfullyConnected = reconnect();
        }
    }

    private boolean internalConnect() {
        try {
            infinispanConnector = new InfinispanConnector(appConfiguration.getMachinesOnlyLength(), gameConfigListener);
            return true;
        } catch (TransportException ex) {
            LOGGER.debug("Unable to connect to Infinispan server.",  ex);
            return false;
        }
    }

    @PreDestroy
    public void disconnect() {
        if (infinispanConnector != null) {
            infinispanConnector.disconnect();
        }
    }

    @Override
    public double[] fetchMachineHealths() {
        try {
            return infinispanConnector.fetchMachineHealths();
        } catch (TransportException | InfinispanException ex) {
             reconnect();
             return infinispanConnector.fetchMachineHealths();
        }
    }

    @Override
    public void resetMachineHealth(int machineIndex) {
        try {
            infinispanConnector.resetMachineHealth(machineIndex);
        } catch (TransportException | InfinispanException ex) {
            reconnect();
            infinispanConnector.resetMachineHealth(machineIndex);
        }
    }

    @Override
    public void dispatchMechanic(Mechanic mechanic, long currentTimeMillis) {
        try {
            infinispanConnector.dispatchMechanic(mechanic, currentTimeMillis);
        } catch (TransportException ex) {
            reconnect();
            infinispanConnector.dispatchMechanic(mechanic, currentTimeMillis);
        }
    }

    @Override
    public void mechanicAdded(Mechanic mechanic, long currentTimeMillis) {
        try {
            infinispanConnector.mechanicAdded(mechanic, currentTimeMillis);
        } catch (TransportException ex) {
            reconnect();
            infinispanConnector.mechanicAdded(mechanic, currentTimeMillis);
        }
    }

    @Override
    public void mechanicRemoved(Mechanic mechanic) {
        try {
            infinispanConnector.mechanicRemoved(mechanic);
        } catch (TransportException ex) {
            reconnect();
            infinispanConnector.mechanicRemoved(mechanic);
        }
    }

    @Override
    public void damageMachine(int machineIndex, double damage) {
        try {
            infinispanConnector.damageMachine(machineIndex, damage);
        } catch (TransportException | InfinispanException ex) {
            reconnect();
            infinispanConnector.damageMachine(machineIndex, damage);
        }
    }

    @Override
    public void sendFutureVisits(int mechanicIndex, int[] futureMachineIndexes) {
        try {
            infinispanConnector.sendFutureVisits(mechanicIndex, futureMachineIndexes);
        } catch (TransportException ex) {
            reconnect();
            infinispanConnector.sendFutureVisits(mechanicIndex, futureMachineIndexes);
        }
    }

    @Override
    public void setDispatchStatus(boolean isDispatchActive) {
        try {
            infinispanConnector.setDispatchStatus(isDispatchActive);
        } catch (TransportException | InfinispanException ex) {
            reconnect();
            infinispanConnector.setDispatchStatus(isDispatchActive);
        }
    }

    @Override
    public void setSimulationStatus(boolean isSimulationActive) {
        try {
            infinispanConnector.setSimulationStatus(isSimulationActive);
        } catch (TransportException | InfinispanException ex) {
            reconnect();
            infinispanConnector.setSimulationStatus(isSimulationActive);
        }
    }

    private boolean reconnect() {
        LOGGER.error("Unable to contact Infinispan server. Reconnecting.");
        disconnect();
        sleep(RECONNECT_WAIT_MILLIS);
        return internalConnect();
    }

    private void sleep(long waitMillis) {
        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException ex) {
            LOGGER.warn("Interrupted waiting before reconnecting to Infinispan.", ex);
            Thread.currentThread().interrupt();
        }
    }
}
