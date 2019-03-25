package com.redhat.demo.optaplanner.simulation;

import javax.annotation.PostConstruct;

import com.redhat.demo.optaplanner.AppConstants;
import com.redhat.demo.optaplanner.config.AppConfiguration;
import com.redhat.demo.optaplanner.upstream.UpstreamConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SimulationService {

    @Autowired
    private UpstreamConnector upstreamConnector;

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

        for (int i = 0; i < AppConstants.MACHINES_LENGTH; i++) {
            upstreamConnector.damageMachine(i, Math.random() / 1000);
        }
    }

    public void startSimulation() {
        isSimulationOn = true;
    }

    public void stopSimulation() {
        isSimulationOn = false;
    }
}
