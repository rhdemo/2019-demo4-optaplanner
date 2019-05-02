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

    private long timeMillis; // This class is the keeper of time
    private long optaMachineHealthRefreshTimeMillis;

    private List<Mechanic> mechanics = new ArrayList<>();
    private Machine[] machines;

    private boolean dispatchPaused;

    @PostConstruct
    public void beanInit() {
        initializeGame();
    }

    private void initializeGame() {
        dispatchPaused = true;
        upstreamConnector.setDispatchStatus(!dispatchPaused);

        clearMechanics();
        initMachines();

        timeMillis = System.currentTimeMillis();
        optaMachineHealthRefreshTimeMillis = timeMillis;

        initMechanics(timeMillis);

        solverManager.startSolver(machines, mechanics, timeMillis);
    }

    private void clearMechanics() {
        // Don't just remove the mechanics of this.mechanics:
        // there might be still mechanics in infinispan from previous pods
        upstreamConnector.clearMechanicsAndFutureVisits();
        mechanics.clear();
        mechanicAdditionCount.set(0);
    }

    private void initMachines() {
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
    }

    private void initMechanics(long timeMillis) {
        for (int i = 0; i < appConfiguration.getInitialMechanicsSize(); i++) {
            Mechanic mechanic = createMechanic();
            mechanics.add(mechanic);
            upstreamConnector.mechanicAdded(mechanic, timeMillis);
        }
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
     * To avoid a race condition on JsonMechanic[] mechanics, we forward it to the @Schedule thread
     */
    @Override
    public void addMechanic() {
        mechanicAdditionCount.getAndIncrement();
    }

    /**
     * To avoid a race condition on JsonMechanic[] mechanics, we forward it to the @Schedule thread
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
    // Synchronized because tick() and reset() must be mutually exclusive
    public synchronized void reset(boolean healAllMachines) {
        simulationService.init();
        solverManager.stopSolver(1L, TimeUnit.SECONDS);
        if (healAllMachines) {
            healAllMachines();
        }
        initializeGame();
        initializeDownstream();
    }

    /**
     *
     * New REST method: "prepareForOptaExplanation"
     * => Button in admin console
     *
     * void prepareForOptaExplanation() {
     *     if (any machine below 80% health) {
     *         heal to 80% health;
     *     }
     *     H = 20% (index 7)
     *     D = 40% (index 3)
     *     E = 60% (index 4)
     *
     *     // during demo Geof damages C
     * }
     */
    @Override
    public void setupForStage() {
        final double HEAL_THRESHOLD = 0.8;
        int d = 3;
        int e = 4;
        int h = 7;
        for (int i = 0; i < machines.length; i++) {
            double machineHealth = machines[i].getHealth();
            if (i == d || i == e || i == h) {
                continue;
            }
            if (machineHealth < HEAL_THRESHOLD) { //heal to 80%
                bringMachineToHealth(i, HEAL_THRESHOLD);
            }
        }

         // bring H to 20%
        bringMachineToHealth(h, 0.2);
         // bring D to 40%
        bringMachineToHealth(d, 0.4);
         // bring E to 60%
        bringMachineToHealth(e, 0.6);
    }

    private void bringMachineToHealth(int machineIndex, double targetHealth) {
        upstreamConnector.damageMachine(machineIndex, machines[machineIndex].getHealth() - targetHealth);
    }

    @Override
    public boolean isDispatchPaused() {
        return dispatchPaused;
    }

    private void healAllMachines() {
        for (int i = 0; i < appConfiguration.getMachinesOnlyLength(); i++) {
            upstreamConnector.resetMachineHealth(machines[i].getMachineIndex());
        }
    }

    @Scheduled(fixedRate = AppConfiguration.TIME_TICK_MILLIS)
    // Synchronized because tick() and reset() must be mutually exclusive
    public synchronized void tick() {
        timeMillis = System.currentTimeMillis();
        updateMachineHealth();
        updateFutureVisits();
        handleDispatches();
        handleMechanicAdditionsAndRemovals();
    }

    private void updateMachineHealth() {
        log.trace("  Start updating machine health.");
        double[] machineHealths = upstreamConnector.fetchMachineHealths();

        boolean machinesHealthDropped = false;
        for (int i = 0; i < machineHealths.length; i++) {
            if (machineHealths[i] < 1.0) {
                machinesHealthDropped = true;
            }
            machines[i].setHealth(machineHealths[i]);
        }
        if (timeMillis >= optaMachineHealthRefreshTimeMillis) {
            if (machinesHealthDropped) {
                solverManager.updateMachineHealths(machines);
            }
            optaMachineHealthRefreshTimeMillis = timeMillis + AppConfiguration.OPTA_MACHINE_HEALTH_REFRESH_RATE;
        }
        downstreamConnector.updateMachinesHealths(machines);
    }

    private void updateFutureVisits() {
        log.trace("  Start updating future visits.");
        solverManager.fetchAndUpdateFutureMachineIndexes(mechanics).forEach(mechanic -> {
            int futureVisitsLength = appConfiguration.getVisibleFutureIndexesLimit();
            int[] futureVisits = Arrays.stream(mechanic.getFutureMachineIndexes())
                    .filter(machineIndex -> machines[machineIndex].isDamaged())
                    .limit(futureVisitsLength).toArray();
            downstreamConnector.sendFutureVisits(mechanic.getMechanicIndex(), futureVisits);
            upstreamConnector.sendFutureVisits(mechanic.getMechanicIndex(), futureVisits);
        });
    }

    private void handleDispatches() {
        if (!dispatchPaused) {
            log.trace("  Start dispatching mechanics.");
            // Check mechanic fixed or departure events
            for (int i = 0; i < mechanics.size(); i++) {
                Mechanic mechanic = mechanics.get(i);
                if (timeMillis >= mechanic.getFocusDepartureTimeMillis() - appConfiguration.getThumbUpDurationMillis()
                        && !mechanic.isFocusFixed()) {
                    int focusMachineIndex = mechanic.getFocusMachineIndex();
                    if (focusMachineIndex != appConfiguration.getGateMachineIndex()) {
                        upstreamConnector.resetMachineHealth(focusMachineIndex);
                    }
                    mechanic.setFocusFixed(true);
                }
                if (timeMillis >= mechanic.getFocusDepartureTimeMillis()) {
                    if (isAnyFutureMachineDamaged(mechanic)) {
                        dispatchMechanic(mechanic);
                    } else {
                        dispatchMechanicToGate(mechanic);
                    }
                    mechanic.setFocusFixed(false);
                }
            }
        }
    }

    private boolean isAnyFutureMachineDamaged(Mechanic mechanic) {
        return Arrays.stream(mechanic.getFutureMachineIndexes())
                .anyMatch(machineIndex -> machines[machineIndex].isDamaged());
    }

    private void dispatchMechanic(Mechanic mechanic) {
        int[] futureMachineIndexes = mechanic.getFutureMachineIndexes();
        final int newFocusMachineIndex = futureMachineIndexes.length <= 0 ? mechanic.getFocusMachineIndex()
                : futureMachineIndexes[0];
        dispatchMechanicToMachine(mechanic, newFocusMachineIndex);
    }

    private void dispatchMechanicToGate(Mechanic mechanic) {
        mechanic.setFutureMachineIndexes(new int[] {});
        final int gateIndex = appConfiguration.getGateMachineIndex();

        // don't send the mechanic to a gate if he is already there
        if (mechanic.getFocusMachineIndex() != gateIndex) {
            dispatchMechanicToMachine(mechanic, appConfiguration.getGateMachineIndex());
        }
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

        solverManager.dispatchMechanic(mechanic, timeMillis);
        upstreamConnector.dispatchMechanic(mechanic, timeMillis);
        downstreamConnector.dispatchMechanic(mechanic, timeMillis);
    }

    private void handleMechanicAdditionsAndRemovals() {
        log.trace("  Start adding/removing mechanics.");
        int mechanicAddition = mechanicAdditionCount.getAndSet(0);
        if (mechanicAddition > 0) {
            for (int i = 0; i < mechanicAddition; i++) {
                if (mechanics.size() >= appConfiguration.getMaximumMechanicsSize()) {
                    // Do not add more mechanics beyond the limit
                    log.warn("Mechanic addition ignored, because there are already {} mechanics.",
                            appConfiguration.getMaximumMechanicsSize());
                    break;
                }
                Mechanic mechanic = createMechanic();
                mechanics.add(mechanic);
                solverManager.addMechanic(mechanic, timeMillis);
                upstreamConnector.mechanicAdded(mechanic, timeMillis);
                downstreamConnector.mechanicAdded(mechanic, timeMillis);
            }
        } else if (mechanicAddition < 0) {
            final int mechanicRemoval = - mechanicAddition;
            final int mechanicsSize = mechanics.size();
            final int boundary = Math.min(mechanicRemoval, mechanicsSize);

            if (mechanicsSize == 0) {
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

}
