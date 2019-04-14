package com.redhat.demo.optaplanner.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/simulation")
public class SimulationController {

    private static final Logger log = LoggerFactory.getLogger(SimulationController.class);

    @Autowired
    private SimulationService simulationService;

    public SimulationController() {
    }

    @GetMapping("/damageDistributionTypes")
    public DamageDistributionType[] getDamageDistributionTypes() {
        return DamageDistributionType.values();
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.OK)
    public void startSimulation(@RequestBody SimulationRequest simulationRequest) {
        double totalDamagePerSecond = simulationRequest.getTotalDamagePerSecond();
        DamageDistributionType damageDistributionType = simulationRequest.getDamageDistributionType();
        log.info("Starting simulation with totalDamagePerSecond ({}) and damageDistributionType ({}).",
                totalDamagePerSecond, damageDistributionType);
        simulationService.startSimulation(totalDamagePerSecond, damageDistributionType);
    }

    @PostMapping("/stop")
    @ResponseStatus(HttpStatus.OK)
    public void stopSimulation() {
        log.info("Stopping simulation");
        simulationService.stopSimulation();
    }

    @PostMapping (value = "/damage")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void damageMachine(@RequestBody DamageMachine damageMachine) {
        String damageAmountString = String.format("%.2f", damageMachine.getAmount());
        log.info("Damaging machine {} by amount of {}.", damageMachine.getMachineIndex(), damageAmountString);
        simulationService.damageMachine(damageMachine.getMachineIndex(), damageMachine.getAmount());
    }

    @PostMapping (value = "/heal")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void healMachine(@RequestBody HealMachine healMachine) {
        log.info("Healing a machine " + healMachine.getMachineIndex());
        simulationService.healMachine(healMachine.getMachineIndex());
    }
}
