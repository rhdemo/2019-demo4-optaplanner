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

public class TravelSolverManagerTest {

    private TravelSolverManager travelSolverManager;

    @Before
    public void setup() {
        travelSolverManager = new TravelSolverManager();
        travelSolverManager.initSolver();
    }

    @Test
    public void solvingStarted() {
        AppConfiguration appConfiguration = new AppConfiguration();
        appConfiguration.readTravelDistanceMatrix();
        Machine[] machines = new Machine[appConfiguration.getMachinesOnlyLength()];
        for (int i = 0; i < machines.length; i++) {
            int x = appConfiguration.getMachineGridX(i);
            int y = appConfiguration.getMachineGridY(i);
            double[] machineIndexToTravelDistances = appConfiguration.getMachineIndexToTravelDistances(i);
            machines[i] = new Machine(i, x, y, machineIndexToTravelDistances, 0.1 * (double)i);
        }

        List<Mechanic> mechanics = new ArrayList<>();
        Mechanic mechanic = new Mechanic(0, appConfiguration.getMechanicSpeed(),
                appConfiguration.getFixDurationMillis(), appConfiguration.getThumbUpDurationMillis(),
                appConfiguration.getGateMachineIndex(), 0L);
        mechanics.add(mechanic);

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
