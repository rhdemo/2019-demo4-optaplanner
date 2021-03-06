package com.redhat.demo.optaplanner.simulation;

import java.util.Random;
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

    private double[] damagePerSecondPerMachine;
    private boolean simulating;

    public SimulationService() {
    }

    @PostConstruct
    public void init() {
        simulating = false;
        upstreamConnector.setSimulationStatus(simulating);
    }

    @Scheduled(fixedRate = AppConfiguration.TIME_TICK_MILLIS)
    public void damageMachines() {
        if (!simulating) {
            return;
        }
        int machinesOnlyLength = appConfiguration.getMachinesOnlyLength();
        for (int machineIndex = 0; machineIndex < machinesOnlyLength; machineIndex++) {
            double damagePerTick = damagePerSecondPerMachine[machineIndex] / ((double) (1000L / AppConfiguration.TIME_TICK_MILLIS));
            // Multiply the random() per 2.0 so the average is damagePerTick
            upstreamConnector.damageMachine(machineIndex, Math.random() * 2.0 * damagePerTick);
        }
    }

    public void startSimulation(double totalDamagePerSecond, DamageDistributionType damageDistributionType) {
        int machinesOnlyLength = appConfiguration.getMachinesOnlyLength();
        damagePerSecondPerMachine = new double[machinesOnlyLength];
        Random random = new Random();
        for (int machineIndex = 0; machineIndex < machinesOnlyLength; machineIndex++) {
            double damagePerSecond;
            switch (damageDistributionType) {
                case UNIFORM:
                    damagePerSecond = totalDamagePerSecond / ((double) machinesOnlyLength);
                    break;
                case GAUSS:
                    double damagePerSecondBase = totalDamagePerSecond / ((double) machinesOnlyLength);
                    double deviation = random.nextGaussian() * damagePerSecondBase / 2.0;
                    damagePerSecond = damagePerSecondBase + deviation;
                    if (damagePerSecond < 0) {
                        damagePerSecond = 0;
                    } else if (damagePerSecond > damagePerSecondBase * 2.0) {
                        damagePerSecond = damagePerSecondBase * 2.0;
                    }
                    break;
                case DOUBLE_DISTRIBUTION_ON_MACHINE_C_AND_J:
                    final int machineC = 2;
                    final int machineJ = 9;
                    // Add 2 because of machine C and J
                    damagePerSecond = totalDamagePerSecond / (((double) machinesOnlyLength) + 2.0);
                    if (machineIndex == machineC || machineIndex == machineJ) {
                        damagePerSecond *= 2.0;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported damageDistributionType (" + damageDistributionType + ").");
            }
            damagePerSecondPerMachine[machineIndex] = damagePerSecond;
        }
        simulating = true;
        upstreamConnector.setSimulationStatus(simulating);
    }

    public void stopSimulation() {
        simulating = false;
        upstreamConnector.setSimulationStatus(simulating);
    }

    public boolean isSimulating() {
        return simulating;
    }

    public void damageMachine(int machineIndex, double amount) {
        upstreamConnector.damageMachine(machineIndex, amount);
    }

    public void healMachine(int machineIndex) {
        upstreamConnector.resetMachineHealth(machineIndex);
    }

}
