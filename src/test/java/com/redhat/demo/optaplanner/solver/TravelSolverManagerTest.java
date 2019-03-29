package com.redhat.demo.optaplanner.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import com.redhat.demo.optaplanner.Machine;
import com.redhat.demo.optaplanner.Mechanic;
import com.redhat.demo.optaplanner.config.AppConfiguration;
import com.redhat.demo.optaplanner.solver.domain.OptaSolution;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.optaplanner.core.api.solver.event.SolverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class TravelSolverManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TravelSolverManagerTest.class);

    private TravelSolverManager travelSolverManager;

    private AppConfiguration appConfiguration;

    @Before
    public void setup() {
        appConfiguration = new AppConfiguration();
        appConfiguration.readTravelDistanceMatrix();

        travelSolverManager = new TravelSolverManager();
        travelSolverManager.initSolver();
    }

    @Test(timeout = 10000L)
    public void solvingWithAddingAndRemovingMechanic() {
        Machine[] machines = prepareMachines(1.0);
        List<Mechanic> mechanics = new ArrayList<>();
        Mechanic mechanicA = prepareMechanic(0);
        mechanics.add(mechanicA);

        ResettableAwaitingListener newSolutionAvailable = new ResettableAwaitingListener();
        travelSolverManager.registerEventListener(newSolutionAvailable);

        travelSolverManager.startSolver(machines, mechanics);

        LOGGER.debug("Damage a machineA.");
        final int indexMachineA = 4;
        machines[indexMachineA].setHealth(0.3);
        travelSolverManager.updateMachineHealths(machines);

        LOGGER.debug("Check that mechanicA is going to fix the machineA.");
        newSolutionAvailable.await();
        travelSolverManager.fetchAndUpdateFutureMachineIndexes(mechanics);
        Assertions.assertThat(mechanicA.getFutureMachineIndexes()).startsWith(indexMachineA);
        newSolutionAvailable.reset();

        LOGGER.debug("Damage a machineB.");
        final int indexMachineB = 5;
        machines[indexMachineB].setHealth(0.4);
        travelSolverManager.updateMachineHealths(machines);

        LOGGER.debug("Add a mechanicB.");
        Mechanic mechanicB = prepareMechanic(1);
        mechanics.add(mechanicB);
        travelSolverManager.addMechanic(mechanicB);

        LOGGER.debug("Check that mechanicB is going to fix either the machineA or machineB.");
        mechanicA.setFutureMachineIndexes(null);
        mechanicB.setFutureMachineIndexes(null);
        newSolutionAvailable.await();
        travelSolverManager.fetchAndUpdateFutureMachineIndexes(mechanics);

        assertAnyMechanicIsFixingMachine(mechanics, indexMachineA);
        assertAnyMechanicIsFixingMachine(mechanics, indexMachineB);
        newSolutionAvailable.reset();

        LOGGER.debug("Remove the mechanicB.");
        mechanicB.setFutureMachineIndexes(null);
        travelSolverManager.removeMechanic(mechanicB.getMechanicIndex());

        LOGGER.debug("The mechanicA should be fixing the broken machines.");
        newSolutionAvailable.await();
        mechanicA.setFutureMachineIndexes(null);
        travelSolverManager.fetchAndUpdateFutureMachineIndexes(mechanics);
        Assertions.assertThat(mechanicA.getFutureMachineIndexes()).containsAnyOf(indexMachineA, indexMachineB);
        newSolutionAvailable.reset();

        LOGGER.debug("Remove the mechanicA too.");
        mechanicA.setFutureMachineIndexes(null);
        travelSolverManager.removeMechanic(mechanicA.getMechanicIndex());

        LOGGER.debug("No machines should be visited by any mechanic.");
        newSolutionAvailable.await();
        travelSolverManager.fetchAndUpdateFutureMachineIndexes(mechanics);
        Assertions.assertThat(mechanicB.getFutureMachineIndexes()).isEmpty();
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

    private static class ResettableAwaitingListener implements SolverEventListener<OptaSolution> {

        private CyclicBarrier barrier = new CyclicBarrier(2);

        void reset() {
            this.barrier.reset();
        }

        void await() {
            try {
                this.barrier.await();
            } catch (BrokenBarrierException | InterruptedException ex) {
                return;
            }
        }

        @Override
        public void bestSolutionChanged(BestSolutionChangedEvent<OptaSolution> bestSolutionEvent) {

            if (bestSolutionEvent.getNewBestSolution().getMechanicList()
                    .stream()
                    // We are interested only in solutions where each mechanic already has a list of visits.
                    .allMatch(optaMechanic -> optaMechanic.getNext() != null)) {
                await();
            }
        }
    }
}
