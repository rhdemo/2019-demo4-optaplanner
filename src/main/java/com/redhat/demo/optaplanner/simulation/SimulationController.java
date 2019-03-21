package com.redhat.demo.optaplanner.simulation;

import javax.annotation.PostConstruct;

import com.redhat.demo.optaplanner.AppConstants;
import com.redhat.demo.optaplanner.upstream.UpstreamConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/simulation")
public class SimulationController {

    private static final Logger log = LoggerFactory.getLogger(SimulationController.class);

    @Autowired
    private UpstreamConnector upstreamConnector;

    private boolean isSimulationOn;

    public SimulationController() {
    }

    @PostConstruct
    public void init() {
        isSimulationOn = false;
    }

    @PostMapping("/start")
    String startSimulation() {
        log.info("Starting simulation");
        isSimulationOn = true;
        return "Simulation started";
    }

    @PostMapping("/stop")
    String stopSimulation() {
        log.info("Stopping simulation");
        isSimulationOn = false;
        return "Simulation stopped";
    }

    @Scheduled(fixedDelay = AppConstants.TIME_TICK_MILLIS)
    public void counterSimulation() {
        if (!isSimulationOn) {
            return;
        }
    }
}
