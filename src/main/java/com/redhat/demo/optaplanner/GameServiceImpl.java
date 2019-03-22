/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.demo.optaplanner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import com.redhat.demo.optaplanner.solver.TravelSolverManager;
import com.redhat.demo.optaplanner.upstream.UpstreamConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GameServiceImpl implements GameService {

    private static final Logger log = LoggerFactory.getLogger(GameServiceImpl.class);

    // the last "machine" is the entry point to the factory
    private static final int ENTRY_POINT_INDEX = AppConstants.MACHINES_LENGTH - 1;
    private static final long ENTRY_POINT_MECHANIC_DELAY = 0L;
    @Autowired
    private UpstreamConnector upstreamConnector;

    @Autowired
    private DownstreamConnector downstreamConnector;

    @Autowired
    private TravelSolverManager solverManager;

    private AtomicInteger mechanicAdditionCount = new AtomicInteger(0);

    private long timeMillis = 0L; // This class is the keeper of time

    private List<Mechanic> mechanics = new ArrayList<>();
    private Machine[] machines;

    private boolean pauzed = false;

    @PostConstruct
    public void init() {
        machines = new Machine[AppConstants.MACHINES_LENGTH];
        double[] machineHealths = upstreamConnector.fetchMachineHealths();
        for (int i = 0; i < machines.length; i++) {
            long[] toMachineIndexTravelTimeMillis = AppConstants.TRAVEL_TIME_MILLIS_MATRIX[i];
            machines[i] = new Machine(i, toMachineIndexTravelTimeMillis, machineHealths[i]);
        }

        for (int i = 0; i < AppConstants.INIT_MECHANICS_LENGTH; i++) {
            mechanics.add(new Mechanic(i, i, timeMillis));
        }

        solverManager.startSolver(machines, mechanics);
    }

    public void pauseGame() {
        log.info("Pauze game");
        this.pauzed = true;
    }

    public void resumeGame() {
        log.info("Resume game");
        this.pauzed = false;
    }

    public boolean isGameRunning() {
        return !pauzed;
    }

    /**
     *  To avoid a race condition on JsonMechanic[] mechanics, we forward it to the @Schedule thread
     */
    @Override
    public void addMechanic() {
        mechanicAdditionCount.getAndIncrement();
    }

    /**
     *  To avoid a race condition on JsonMechanic[] mechanics, we forward it to the @Schedule thread
     */
    @Override
    public void removeMechanic() {
        mechanicAdditionCount.getAndDecrement();
    }

    @Override
    public List<Mechanic> getMechanics() {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public Machine[] getMachines() {
        return machines;
    }

    @Scheduled(fixedDelay = AppConstants.TIME_TICK_MILLIS)
    public void tick() {
        if (pauzed) {
            return;
        }
        timeMillis += AppConstants.TIME_TICK_MILLIS;

        // Update futureMachineIndexes first (it might affect mechanic dispatch events)
        solverManager.fetchAndUpdateFutureMachineIndexes(mechanics);

        // Update machine healths
        double[] machineHealths = upstreamConnector.fetchMachineHealths();
        for (int i = 0; i < machines.length; i++) {
            machines[i].setHealth(machineHealths[i]);
        }
        if (timeMillis % AppConstants.OPTA_MACHINE_HEALTH_REFRESH_RATE == 0L) {
            solverManager.updateMachineHealths(machines);
        }

        downstreamConnector.updateMachinesHealths(machines);

        // Check mechanic fixed or departure events
        for (int i = 0; i < mechanics.size(); i++) {
            Mechanic mechanic = mechanics.get(i);
            if (timeMillis >= mechanic.getFocusDepartureTimeMillis() - AppConstants.BREATHING_TIME_MILLIS) {
                // TODO If it didn't already happen for this fix case...
                upstreamConnector.resetMachineHealth(mechanic.getFocusMachineIndex());
                // Replace fixed OptaVisit with a new one
                // TODO add a new visit for the machine instead of resetting to keep the time correct - needs more refactoring due to the way visits are accessed (by machine id)
                solverManager.resetMachineVisit(mechanic.getFocusMachineIndex());
            }
            if (timeMillis >= mechanic.getFocusDepartureTimeMillis()) {
                int oldFocusMachineIndex = mechanic.getFocusMachineIndex();
                int[] futureMachineIndexes = mechanic.getFutureMachineIndexes();
                int newFocusMachineIndex = futureMachineIndexes.length <= 0 ? mechanic.getFocusMachineIndex()
                        : futureMachineIndexes[0];
                mechanic.setFocusMachineIndex(newFocusMachineIndex);
                long travelTime = machines[oldFocusMachineIndex]
                        .getToMachineIndexTravelTimeMillis()[newFocusMachineIndex];
                mechanic.setFocusDepartureTimeMillis(timeMillis + travelTime + AppConstants.FIX_TIME_MILLIS + AppConstants.BREATHING_TIME_MILLIS);

                downstreamConnector.dispatchMechanic(mechanic);
            }
        }

        handleMechanicAdditionsAndRemovals();
    }

    private void handleMechanicAdditionsAndRemovals() {
        int mechanicAddition = mechanicAdditionCount.getAndSet(0);
        if (mechanicAddition > 0) {

            for (int i = 0; i < mechanicAddition; i++) {
                Mechanic addedMechanic = new Mechanic(mechanics.size(), ENTRY_POINT_INDEX, ENTRY_POINT_MECHANIC_DELAY);
                mechanics.add(addedMechanic);
               // solverManager.addMechanic(mechanics.size() - 1);
                downstreamConnector.mechanicAdded(addedMechanic);
            }
        } else if (mechanicAddition < 0) {
            final int mechanicRemoval = - mechanicAddition;
            final int boundary = Math.min(mechanicRemoval, mechanics.size());
            for (int i = 0; i < boundary; i++) {
                Mechanic removedMechanic = mechanics.remove(i);
                solverManager.removeMechanic(removedMechanic.getMechanicIndex());
                downstreamConnector.mechanicRemoved(removedMechanic);
            }
        }
    }
}
