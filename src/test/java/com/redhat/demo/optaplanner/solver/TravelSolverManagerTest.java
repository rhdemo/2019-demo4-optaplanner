package com.redhat.demo.optaplanner.solver;

import java.util.ArrayList;
import java.util.List;

import com.redhat.demo.optaplanner.AppConstants;
import com.redhat.demo.optaplanner.Machine;
import com.redhat.demo.optaplanner.Mechanic;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TravelSolverManagerTest {

    private TravelSolverManager travelSolverManager;

    @Before
    public void setup() {
        travelSolverManager = new TravelSolverManager();
        travelSolverManager.initSolver();
    }

    @Test
    public void solutionStarted() {
        Machine[] machines = new Machine[AppConstants.MACHINES_LENGTH];
        for (int i = 0; i < machines.length; i++) {
            long[] toMachineIndexTravelTimeMillis = AppConstants.TRAVEL_TIME_MILLIS_MATRIX[i];
            machines[i] = new Machine(i, toMachineIndexTravelTimeMillis, 0.1 * (double)i);
        }

        List<Mechanic> mechanics = new ArrayList<>();
        mechanics.add(new Mechanic(0, machines.length - 1, 0L));

        travelSolverManager.startSolver(machines, mechanics);
        travelSolverManager.fetchAndUpdateFutureMachineIndexes(mechanics);

        Assertions.assertThat(mechanics)
                .hasSize(1)
                .extracting(Mechanic::getFocusMachineIndex)
                .isNotNull()
                .isNotEqualTo(machines.length - 1);
    }

    @After
    public void tearDown() {
        travelSolverManager.preDestroy();
    }
}
