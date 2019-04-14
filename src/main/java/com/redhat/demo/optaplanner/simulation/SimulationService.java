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
    }

    @Scheduled(fixedDelay = AppConfiguration.TIME_TICK_MILLIS)
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
                    damagePerSecond = totalDamagePerSecond / ((double) machinesOnlyLength);
                    damagePerSecond += Math.min(0, Math.max( damagePerSecond * 2.0,
                            random.nextGaussian() * damagePerSecond / 2.0));
                    break;
                case DOUBLE_DISTRIBUTION_ON_MACHINE_D_AND_I:
                    damagePerSecond = totalDamagePerSecond / (((double) machinesOnlyLength) + 2.0);
                    if (machineIndex == 3 || machineIndex == 8) {
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

    public void damageMachine(int machineIndex, double amount) {
        upstreamConnector.damageMachine(machineIndex, amount);
    }

    public void healMachine(int machineIndex) {
        upstreamConnector.resetMachineHealth(machineIndex);
    }

}
