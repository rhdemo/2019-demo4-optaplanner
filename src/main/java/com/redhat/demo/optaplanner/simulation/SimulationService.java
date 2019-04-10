package com.redhat.demo.optaplanner.simulation;

import javax.annotation.PostConstruct;

import com.redhat.demo.optaplanner.config.AppConfiguration;
import com.redhat.demo.optaplanner.upstream.UpstreamConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SimulationService {

    @Autowired
    private AppConfiguration appConfiguration;
    @Autowired
    private UpstreamConnector upstreamConnector;

    @Autowired
    private AppConfiguration configuration;

    private boolean isSimulationOn;

    public SimulationService() {
    }

    @PostConstruct
    public void init() {
        isSimulationOn = false;
    }

    @Scheduled(fixedDelay = AppConfiguration.TIME_TICK_MILLIS)
    public void damageMachines() {
        if (!isSimulationOn) {
            return;
        }
        int machinesOnlyLength = appConfiguration.getMachinesOnlyLength();
        for (int i = 0; i < machinesOnlyLength; i++) {
            upstreamConnector.damageMachine(i, Math.random() / 1000);
        }
    }

    public void startSimulation() {
        isSimulationOn = true;
        upstreamConnector.setSimulationStatus(isSimulationOn);
    }

    public void stopSimulation() {
        isSimulationOn = false;
        upstreamConnector.setSimulationStatus(isSimulationOn);
    }

    public void damageMachine(int machineIndex) {
        upstreamConnector.damageMachine(machineIndex, configuration.getManualDamageValue());
    }

    public void damageMachine(int machineIndex, double amount) {
        upstreamConnector.damageMachine(machineIndex, amount);
    }

    public void healMachine(int machineIndex) {
        upstreamConnector.resetMachineHealth(machineIndex);
    }
}
