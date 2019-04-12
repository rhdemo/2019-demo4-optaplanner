package com.redhat.demo.optaplanner.solver;

import java.util.ArrayList;
import java.util.List;

import com.redhat.demo.optaplanner.Machine;
import com.redhat.demo.optaplanner.Mechanic;
import com.redhat.demo.optaplanner.config.AppConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class TravelSolverManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TravelSolverManagerTest.class);
    // TODO Don't do this, use OptaConditions <jiraURL>
    private static final long SLEEP_TIME = 1000;

    private TravelSolverManager travelSolverManager;

    private AppConfiguration appConfiguration;

    @Before
    public void setup() {
        appConfiguration = new AppConfiguration();
        appConfiguration.readTravelDistanceMatrix();

        travelSolverManager = new TravelSolverManager();
        travelSolverManager.initSolver();
    }

    @Test(timeout = 10_000L)
    public void solvingWithAddingAndRemovingMechanic() throws InterruptedException {
        Machine[] machines = prepareMachines(1.0);
        List<Mechanic> mechanics = new ArrayList<>();
        Mechanic mechanicA = prepareMechanic(0);
        mechanics.add(mechanicA);

        travelSolverManager.startSolver(machines, mechanics, 0L);

        LOGGER.debug("Damage a machineA.");
        final int indexMachineA = 4;
        machines[indexMachineA].setHealth(0.3);
        travelSolverManager.updateMachineHealths(machines);

        LOGGER.debug("Check that mechanicA is going to fix the machineA.");
        Thread.sleep(SLEEP_TIME);
        travelSolverManager.fetchAndUpdateFutureMachineIndexes(mechanics);
        Assertions.assertThat(mechanicA.getFutureMachineIndexes()).startsWith(indexMachineA);

        LOGGER.debug("Damage a machineB.");
        final int indexMachineB = 5;
        machines[indexMachineB].setHealth(0.4);
        travelSolverManager.updateMachineHealths(machines);

        LOGGER.debug("Add a mechanicB.");
        Mechanic mechanicB = prepareMechanic(1);
        mechanics.add(mechanicB);
        travelSolverManager.addMechanic(mechanicB, 0L);

        LOGGER.debug("Check that mechanicB is going to fix either the machineA or machineB.");
        mechanicA.setFutureMachineIndexes(null);
        mechanicB.setFutureMachineIndexes(null);
        Thread.sleep(SLEEP_TIME);
        travelSolverManager.fetchAndUpdateFutureMachineIndexes(mechanics);
        assertAnyMechanicIsFixingMachine(mechanics, indexMachineA);
        assertAnyMechanicIsFixingMachine(mechanics, indexMachineB);

        LOGGER.debug("Remove the mechanicB.");
        mechanicB.setFutureMachineIndexes(null);
        mechanics.remove(mechanicB);
        travelSolverManager.removeMechanic(mechanicB.getMechanicIndex());

        LOGGER.debug("The mechanicA should be fixing the broken machines.");
        Thread.sleep(SLEEP_TIME);
        mechanicA.setFutureMachineIndexes(null);
        travelSolverManager.fetchAndUpdateFutureMachineIndexes(mechanics);
        Assertions.assertThat(mechanicA.getFutureMachineIndexes()).containsAnyOf(indexMachineA, indexMachineB);

        // TODO Having no mechanics is not supported by OptaPlanner, see https://issues.jboss.org/browse/PLANNER-776
//        LOGGER.debug("Remove the mechanicA too.");
//        mechanicA.setFutureMachineIndexes(null);
//        mechanics.remove(mechanicA);
//        travelSolverManager.removeMechanic(mechanicA.getMechanicIndex());
//
//        LOGGER.debug("No machines should be visited by any mechanic.");
//        Thread.sleep(SLEEP_TIME);
//        travelSolverManager.fetchAndUpdateFutureMachineIndexes(mechanics);
//        Assertions.assertThat(mechanicB.getFutureMachineIndexes()).isEmpty();
//        Thread.sleep(SLEEP_TIME * 5);
    }

    private void assertAnyMechanicIsFixingMachine(List<Mechanic> mechanics, int machineIndex) {
        boolean machineIndexFound = mechanics.stream().anyMatch(mechanic -> {
            boolean found = false;
            int[] futureIndexes = mechanic.getFutureMachineIndexes();
            if (futureIndexes == null) {
                return false;
            }
            for (int j = 0; j < futureIndexes.length; j++) {
                if (futureIndexes[j] == machineIndex) {
                    found = true;
                    break;
                }
            }
            return found;
        });

        Assertions.assertThat(machineIndexFound)
                .withFailMessage("No mechanic is fixing machine %d", machineIndex)
                .isTrue();
    }

    private Machine[] prepareMachines(double initialHealth) {
        Machine[] machines = new Machine[appConfiguration.getMachinesAndGateLength()];
        for (int i = 0; i < machines.length; i++) {
            int x = appConfiguration.getMachineGridX(i);
            int y = appConfiguration.getMachineGridY(i);
            double[] machineIndexToTravelDistances = appConfiguration.getMachineIndexToTravelDistances(i);

            if (i == appConfiguration.getGateMachineIndex()) {
                machines[i] = Machine.createGate(i, x, y, machineIndexToTravelDistances);
            } else {
                machines[i] = Machine.createMachine(i, x, y, machineIndexToTravelDistances, initialHealth);
            }
        }

        return machines;
    }

    private Mechanic prepareMechanic(int mechanicIndex) {
        return new Mechanic(mechanicIndex,
                            appConfiguration.getMechanicSpeed(),
                            appConfiguration.getFixDurationMillis(),
                            appConfiguration.getThumbUpDurationMillis(),
                            appConfiguration.getGateMachineIndex(),
                            appConfiguration.getGateMachineIndex(),
                            0L);
    }

    @After
    public void tearDown() {
        travelSolverManager.preDestroy();
    }

}
