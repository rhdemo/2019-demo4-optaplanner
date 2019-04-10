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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;

import com.redhat.demo.optaplanner.config.AppConfiguration;
import com.redhat.demo.optaplanner.simulation.SimulationService;
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

    @Autowired
    private AppConfiguration appConfiguration;
    @Autowired
    private UpstreamConnector upstreamConnector;
    @Autowired
    private DownstreamConnector downstreamConnector;
    @Autowired
    private TravelSolverManager solverManager;
    @Autowired
    private SimulationService simulationService;

    private AtomicInteger mechanicAdditionCount = new AtomicInteger(0);

    private long timeMillis = 0L; // This class is the keeper of time

    private List<Mechanic> mechanics = new ArrayList<>();
    private Machine[] machines;

    private boolean dispatchPaused;

    @PostConstruct
    public void beanInit() {
        initializeGame();
    }

    private void initializeGame() {
        dispatchPaused = true;
        mechanics.clear();
        machines = new Machine[appConfiguration.getMachinesAndGateLength()];
        double[] machineHealths = upstreamConnector.fetchMachineHealths();
        for (int i = 0; i < machines.length; i++) {
            int x = appConfiguration.getMachineGridX(i);
            int y = appConfiguration.getMachineGridY(i);
            double[] machineIndexToTravelDistances = appConfiguration.getMachineIndexToTravelDistances(i);
            if (i == appConfiguration.getGateMachineIndex()) {
                machines[i] = Machine.createGate(i, x, y, machineIndexToTravelDistances);
            } else {
                machines[i] = Machine.createMachine(i, x, y, machineIndexToTravelDistances, machineHealths[i]);
            }
        }

        for (int i = 0; i < appConfiguration.getInitialMechanicsSize(); i++) {
            Mechanic mechanic = createMechanic();
            mechanics.add(mechanic);
            upstreamConnector.mechanicAdded(mechanic, timeMillis);
        }
        solverManager.startSolver(machines, mechanics);
    }

    public void pauseGame() {
        log.info("Pauze dispatched");
        this.dispatchPaused = true;
        upstreamConnector.setDispatchStatus(!dispatchPaused);
    }

    public void resumeGame() {
        log.info("Resume dispatched");
        this.dispatchPaused = false;
        upstreamConnector.setDispatchStatus(!dispatchPaused);
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
    public void initializeDownstream() {
        downstreamConnector.connect(machines, mechanics, timeMillis);
    }

    @Override
    public void reset() {
        simulationService.init();
        solverManager.stopSolver(1L, TimeUnit.SECONDS);
        healAllMachines();
        initializeGame();
        initializeDownstream();
    }

    private void healAllMachines() {
        for (int i = 0; i < appConfiguration.getMachinesOnlyLength(); i++) {
            upstreamConnector.resetMachineHealth(machines[i].getMachineIndex());
        }
    }

    @Scheduled(fixedDelay = AppConfiguration.TIME_TICK_MILLIS)
    public void tick() {
        timeMillis += AppConfiguration.TIME_TICK_MILLIS;

        updateMachineHealth();

        final boolean isAnyMachineDamaged = isAnyMachineDamaged();
        if (isAnyMachineDamaged) {
            boolean futureIndexesUpdated = solverManager.fetchAndUpdateFutureMachineIndexes(mechanics);

            if (futureIndexesUpdated) {
                sendFutureVisits();
            }
        }

        if (!dispatchPaused) {
            // Check mechanic fixed or departure events
            for (int i = 0; i < mechanics.size(); i++) {
                Mechanic mechanic = mechanics.get(i);
                if (timeMillis >= mechanic.getFocusDepartureTimeMillis() - appConfiguration.getThumbUpDurationMillis()) {
                    // TODO If it didn't already happen for this fix case...
                    int focusMachineIndex = mechanic.getFocusMachineIndex();
                    if (focusMachineIndex != appConfiguration.getGateMachineIndex()) {
                        upstreamConnector.resetMachineHealth(focusMachineIndex);
                    }
                }
                if (timeMillis >= mechanic.getFocusDepartureTimeMillis()) {
                    if (isAnyMachineDamaged) {
                        dispatchMechanic(mechanic);
                    } else {
                        dispatchMechanicToGate(mechanic);
                    }
                }
            }
        }

        handleMechanicAdditionsAndRemovals();
    }

    private void dispatchMechanicToGate(Mechanic mechanic) {
        mechanic.setFutureMachineIndexes(new int[] {});
        dispatchMechanicToMachine(mechanic, appConfiguration.getGateMachineIndex());
    }

    private void dispatchMechanic(Mechanic mechanic) {
        int[] futureMachineIndexes = mechanic.getFutureMachineIndexes();
        final int newFocusMachineIndex = futureMachineIndexes.length <= 0 ? mechanic.getFocusMachineIndex()
                : futureMachineIndexes[0];
        dispatchMechanicToMachine(mechanic, newFocusMachineIndex);
    }

    private void dispatchMechanicToMachine(Mechanic mechanic, final int newFocusMachineIndex) {
        final int oldFocusMachineIndex = mechanic.getFocusMachineIndex();
        mechanic.setFocusMachineIndex(newFocusMachineIndex);
        long travelTime = (long)
                (machines[oldFocusMachineIndex].getMachineIndexToTravelDistances()[newFocusMachineIndex]
                        / mechanic.getSpeed());

        log.debug("Dispatching a mechanic "
                + mechanic.getMechanicIndex()
                + " from old index "
                + oldFocusMachineIndex
                + " to a new index "
                + newFocusMachineIndex);
        mechanic.setOriginalMachineIndex(oldFocusMachineIndex);
        long focusTravelTimeMillis = timeMillis + travelTime;
        mechanic.setFocusTravelTimeMillis(focusTravelTimeMillis);

        solverManager.dispatchMechanic(mechanic);
        upstreamConnector.dispatchMechanic(mechanic, timeMillis);
        downstreamConnector.dispatchMechanic(mechanic, timeMillis);
    }

    private void updateMachineHealth() {
        double[] machineHealths = upstreamConnector.fetchMachineHealths();
        for (int i = 0; i < machineHealths.length; i++) {
            machines[i].setHealth(machineHealths[i]);
        }
        if (timeMillis % AppConfiguration.OPTA_MACHINE_HEALTH_REFRESH_RATE == 0L) {
            solverManager.updateMachineHealths(machines);
        }

        downstreamConnector.updateMachinesHealths(machines);
    }

    private boolean isAnyMachineDamaged() {
        return Arrays.stream(machines).anyMatch(machine -> machine.isDamaged());
    }

    private void handleMechanicAdditionsAndRemovals() {
        int mechanicAddition = mechanicAdditionCount.getAndSet(0);
        if (mechanicAddition > 0) {
            for (int i = 0; i < mechanicAddition; i++) {
                Mechanic mechanic = createMechanic();
                mechanics.add(mechanic);
                solverManager.addMechanic(mechanic);
                upstreamConnector.mechanicAdded(mechanic, timeMillis);
                downstreamConnector.mechanicAdded(mechanic, timeMillis);
            }
        } else if (mechanicAddition < 0) {
            final int mechanicRemoval = - mechanicAddition;
            final int mechanicsSize = mechanics.size();
            final int boundary = Math.min(mechanicRemoval, mechanicsSize);

            if (mechanicsSize == 1) { // the last mechanic cannot disappear
                return;
            }

            for (int i = mechanicsSize - 1; mechanicsSize - i <= boundary; i--) {
                Mechanic removedMechanic = mechanics.remove(i);
                solverManager.removeMechanic(removedMechanic.getMechanicIndex());
                upstreamConnector.mechanicRemoved(removedMechanic);
                downstreamConnector.mechanicRemoved(removedMechanic);
            }
        }
    }

    private Mechanic createMechanic() {
        return new Mechanic(
                mechanics.size(),
                appConfiguration.getMechanicSpeed(),
                appConfiguration.getFixDurationMillis(), appConfiguration.getThumbUpDurationMillis(),
                appConfiguration.getGateMachineIndex(),
                appConfiguration.getGateMachineIndex(),
                timeMillis);
    }

    private void sendFutureVisits() {
        int futureVisitsLength = appConfiguration.getVisibleFutureIndexesLimit();
        mechanics.forEach(mechanic -> {
            int [] futureVisits = mechanic.getFutureMachineIndexes().length < futureVisitsLength ?
                    mechanic.getFutureMachineIndexes() : Arrays.copyOf(mechanic.getFutureMachineIndexes(), futureVisitsLength);

            downstreamConnector.sendFutureVisits(mechanic.getMechanicIndex(), futureVisits);
            upstreamConnector.sendFutureVisits(mechanic.getMechanicIndex(), futureVisits);
        });
    }

}
