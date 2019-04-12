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

package com.redhat.demo.optaplanner.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.redhat.demo.optaplanner.Machine;
import com.redhat.demo.optaplanner.Mechanic;
import com.redhat.demo.optaplanner.solver.domain.OptaConfiguration;
import com.redhat.demo.optaplanner.solver.domain.OptaMachine;
import com.redhat.demo.optaplanner.solver.domain.OptaMechanic;
import com.redhat.demo.optaplanner.solver.domain.OptaSolution;
import com.redhat.demo.optaplanner.solver.domain.OptaVisit;
import com.redhat.demo.optaplanner.solver.domain.OptaVisitOrMechanic;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.domain.variable.listener.support.VariableListenerSupport;
import org.optaplanner.core.impl.score.director.AbstractScoreDirector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TravelSolverManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TravelSolverManager.class);

    public static final String SOLVER_CONFIG = "com/redhat/demo/optaplanner/solver/travelingMechanicSolverConfig.xml";

    private ExecutorService executorService;
    private SolverFactory<OptaSolution> solverFactory;
    private Solver<OptaSolution> solver;

    private AtomicReference<OptaSolution> bestSolutionReference = new AtomicReference<>();

    public TravelSolverManager() {
        solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIG, TravelSolverManager.class.getClassLoader());
    }

    @PostConstruct
    protected void initSolver() {
        executorService = Executors.newFixedThreadPool(1);
        solver = solverFactory.buildSolver();
        solver.addEventListener(bestSolutionEvent -> {
            OptaSolution solution = bestSolutionEvent.getNewBestSolution();
            if (LOGGER.isTraceEnabled()) {
                StringBuilder s = new StringBuilder();
                for (OptaMechanic mechanic : solution.getMechanicList()) {
                    s.append("Mech-").append(mechanic.getMechanicIndex())
                            .append("[").append(mechanic.getFixOffsetMillis()).append("ms");
                    for (OptaVisit visit = mechanic.getNext(); visit != null; visit = visit.getNext()) {
                        s.append(" ").append(visit.getFixOffsetMillis()).append("ms@").append(visit.getMachineIndex());
                    }
                    s.append("] - ");
                }
                LOGGER.trace("New best solution with score ({}): {}", solution.getScore().toShortString(), s);
            }
            bestSolutionReference.set(solution);
        });
    }

    @PreDestroy
    protected void preDestroy() {
        executorService.shutdownNow();
    }

    public void stopSolver(long timeout, TimeUnit timeoutUnit) {
        final long WAIT_STEP_MILLIS = 100L;

        TimeUnit millisTimeUnit = TimeUnit.MILLISECONDS;
        long timeoutMillis = millisTimeUnit.convert(timeout, timeoutUnit);
        long waitingMillis = 0L;
        solver.terminateEarly();

        while (solver.isSolving()) {
            sleep(WAIT_STEP_MILLIS);
            waitingMillis += WAIT_STEP_MILLIS;

            if (waitingMillis > timeoutMillis) {
                LOGGER.error("Waiting for a solver to terminate timed out");
                break;
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            LOGGER.error("Interrupted waiting.", ex);
        }
    }

    public void startSolver(Machine[] machines, List<Mechanic> mechanics, long timeMillis) {
        OptaConfiguration optaConfiguration = new OptaConfiguration(timeMillis);
        List<OptaMachine> optaMachineList = Arrays.stream(machines)
                .map(machine -> new OptaMachine(
                        machine.getMachineIndex(), machine.getMachineIndexToTravelDistances(), machine.getHealth(), machine.isGate()))
                .collect(Collectors.toList());
        List<OptaMechanic> mechanicList = mechanics.stream()
                .map(mechanic -> {
                    OptaMachine focusMachine = optaMachineList.get(mechanic.getFocusMachineIndex());
                    return new OptaMechanic(
                            mechanic.getMechanicIndex(), optaConfiguration, mechanic.getSpeed(),
                            mechanic.getFixDurationMillis(), mechanic.getThumbUpDurationMillis(),
                            focusMachine, mechanic.getFocusDepartureTimeMillis());
                })
                .collect(Collectors.toList());
        OptaMechanic dummyMechanic = OptaMechanic.createDummy(optaConfiguration);
        List<OptaVisit> visitList = optaMachineList.stream()
                .filter(optaMachine -> !optaMachine.isGate())
                .map(optaMachine -> new OptaVisit(optaMachine.getMachineIndex(), optaMachine))
                .collect(Collectors.toList());
        OptaSolution solution = new OptaSolution(optaConfiguration, optaMachineList, mechanicList, dummyMechanic, visitList);

        executorService.submit(() -> {
            try {
                solver.solve(solution);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        });
    }

    /**
     * @param mechanics never null
     * @return true if updated
     */
    public boolean fetchAndUpdateFutureMachineIndexes(List<Mechanic> mechanics) {
        OptaSolution bestSolution = bestSolutionReference.getAndSet(null);
        if (bestSolution == null) {
            return false;
        }
        if (!bestSolution.getScore().isFeasible()) {
            return false;
        }
        if (bestSolution.getMechanicList().size() != mechanics.size()) {
            // The best solution is stale
            return false;
        }
        for (int i = 0; i < mechanics.size(); i++) {
            Mechanic mechanic = mechanics.get(i);
            OptaMechanic optaMechanic = bestSolution.getMechanicList().get(i);
            if (optaMechanic.getFocusMachine().getMachineIndex() != mechanic.getFocusMachineIndex()
                || optaMechanic.getFocusDepartureTimeMillis() != mechanic.getFocusDepartureTimeMillis()) {
                // The best solution is stale
                return false;
            }
        }
        // The best solution isn't stale (except maybe for machine healths, but that's ok)
        for (int i = 0; i < mechanics.size(); i++) {
            Mechanic mechanic = mechanics.get(i);
            OptaMechanic optaMechanic = bestSolution.getMechanicList().get(i);
            List<Integer> futureMachineIndexList = new ArrayList<>(bestSolution.getMachineList().size());
            OptaVisit next = optaMechanic.getNext();
            while (next != null) {
                futureMachineIndexList.add(next.getMachineIndex());
                next = next.getNext();
            }
            int[] futureMachineIndexes = futureMachineIndexList.stream().mapToInt(Integer::intValue).toArray();
            mechanic.setFutureMachineIndexes(futureMachineIndexes);
            LOGGER.debug("Future machine indexes updated for a mechanic "
                                       + mechanic.getMechanicIndex()
                                       + " with the first one being a machine: "
                                       + (futureMachineIndexes.length == 0 ? "empty" : futureMachineIndexes[0]));
        }
        return true;
    }

    public void updateMachineHealths(Machine[] machines) {
        // Avoid race condition by extracting the heaths (Double is immutable, Machine is not)
        Double[] healths = Arrays.stream(machines)
                .filter(machine -> !machine.isGate())
                .map(Machine::getHealth).toArray(Double[]::new);
        solver.addProblemFactChange(scoreDirector -> {
            OptaSolution workingSolution = scoreDirector.getWorkingSolution();
            // A SolutionCloner doesn't clone problem fact lists, shallow clone to ensure changes are only applied to the workingSolution
            List<OptaMachine> machineList = new ArrayList<>(workingSolution.getMachineList());
            workingSolution.setMachineList(machineList);
            // Ideally we should clone the actual OptaMachine instances and rewire OptaMechanic and OptaVisits accordingly
            // to avoid corrupting previous best solutions, but we ignore their health anyway, so no problem.

            for (int i = 0; i < healths.length; i++) {
                OptaMachine workingMachine = workingSolution.getMachineList().get(i);
                scoreDirector.beforeProblemPropertyChanged(workingMachine);
                workingMachine.setHealth(healths[i]);
                scoreDirector.afterProblemPropertyChanged(workingMachine);
            }
            scoreDirector.triggerVariableListeners();
        });
    }

    public void addMechanic(Mechanic mechanic, long timeMillis) {
        final int mechanicIndex = mechanic.getMechanicIndex();
        final int focusMachineIndex = mechanic.getFocusMachineIndex();
        final double speed = mechanic.getSpeed();
        final long fixDurationMillis = mechanic.getFixDurationMillis();
        final long thumbUpDurationMillis = mechanic.getThumbUpDurationMillis();
        final long focusDepartureTimeMillis = mechanic.getFocusDepartureTimeMillis();

        solver.addProblemFactChange(scoreDirector -> {
            OptaSolution solution = scoreDirector.getWorkingSolution();

            OptaConfiguration optaConfiguration = solution.getConfiguration();
            scoreDirector.beforeProblemPropertyChanged(optaConfiguration);
            optaConfiguration.setLastDispatchOfAnyMechanicTimeMillis(timeMillis);
            scoreDirector.afterProblemPropertyChanged(optaConfiguration);

            List<OptaMachine> machines = solution.getMachineList();
            OptaMachine focusMachine = machines.get(focusMachineIndex);
            if (!focusMachine.isGate()) {
                LOGGER.warn("A mechanic ({}) was added but didn't start at the gate. A new mechanic is supposed to show up there.", mechanicIndex);
                scoreDirector.beforeProblemPropertyChanged(focusMachine);
                focusMachine.setFocused(true);
                scoreDirector.afterProblemPropertyChanged(focusMachine);
            }
            // A SolutionCloner clones planning entity lists (such as mechanicList), so no need to clone the mechanicList here
            List<OptaMechanic> mechanicList = solution.getMechanicList();
            OptaMechanic optaMechanic = new OptaMechanic(mechanicIndex,
                                                         solution.getConfiguration(),
                                                         speed,
                                                         fixDurationMillis,
                                                         thumbUpDurationMillis,
                                                         focusMachine,
                                                         focusDepartureTimeMillis);
            scoreDirector.beforeEntityAdded(optaMechanic);
            mechanicList.add(optaMechanic);
            scoreDirector.afterEntityAdded(optaMechanic);
            scoreDirector.triggerVariableListeners();
            // Workaround for https://issues.jboss.org/browse/PLANNER-1477
            ((VariableListenerSupport) ((AbstractScoreDirector) scoreDirector).getSupplyManager()).triggerAllVariableListeners();
        });
    }

    public void removeMechanic(int mechanicIndex) {
        solver.addProblemFactChange(scoreDirector -> {
            OptaSolution solution = scoreDirector.getWorkingSolution();
            // A SolutionCloner clones planning entity lists (such as mechanicList), so no need to clone the mechanicList here
            List<OptaMechanic> mechanicList = solution.getMechanicList();
            OptaMechanic mechanic = mechanicList.get(mechanicIndex);

            OptaMachine oldFocusMachine = mechanic.getFocusMachine();
            scoreDirector.beforeProblemPropertyChanged(oldFocusMachine);
            oldFocusMachine.setFocused(false);
            scoreDirector.afterProblemPropertyChanged(oldFocusMachine);

            OptaVisit visit = mechanic.getNext();
            while (visit != null) {
                scoreDirector.beforeVariableChanged(visit, "previous");
                visit.setPrevious(null);
                scoreDirector.afterVariableChanged(visit, "previous");
                visit = visit.getNext();
            }

            scoreDirector.beforeEntityRemoved(mechanic);
            mechanicList.remove(mechanicIndex);
            scoreDirector.afterEntityRemoved(mechanic);

            scoreDirector.triggerVariableListeners();
        });
    }

    public void dispatchMechanic(Mechanic mechanic, long timeMillis) {
        final int focusMachineIndex = mechanic.getFocusMachineIndex();
        final int mechanicIndex = mechanic.getMechanicIndex();
        final long focusDepartureTimeMillis = mechanic.getFocusDepartureTimeMillis();

        solver.addProblemFactChange(scoreDirector -> {
            OptaSolution solution = scoreDirector.getWorkingSolution();
            OptaMachine newFocusMachine = solution.getMachineList().get(focusMachineIndex);

            OptaConfiguration optaConfiguration = solution.getConfiguration();
            scoreDirector.beforeProblemPropertyChanged(optaConfiguration);
            optaConfiguration.setLastDispatchOfAnyMechanicTimeMillis(timeMillis);
            scoreDirector.afterProblemPropertyChanged(optaConfiguration);

            OptaMechanic optaMechanic = solution.getMechanicList().get(mechanicIndex);
            OptaMachine oldFocusMachine = optaMechanic.getFocusMachine();
            scoreDirector.beforeProblemPropertyChanged(oldFocusMachine);
            oldFocusMachine.setFocused(false);
            scoreDirector.afterProblemPropertyChanged(oldFocusMachine);

            scoreDirector.beforeProblemPropertyChanged(optaMechanic);
            optaMechanic.setFocusMachine(newFocusMachine);
            optaMechanic.setFocusDepartureTimeMillis(focusDepartureTimeMillis);
            scoreDirector.afterProblemPropertyChanged(optaMechanic);

            if (!newFocusMachine.isGate()) {
                scoreDirector.beforeProblemPropertyChanged(newFocusMachine);
                newFocusMachine.setFocused(true);
                scoreDirector.afterProblemPropertyChanged(newFocusMachine);
            }

            if (!newFocusMachine.isGate()) {
                OptaVisit visit = solution.getVisitList().get(newFocusMachine.getMachineIndex());
                OptaVisitOrMechanic previous = visit.getPrevious();
                OptaVisit next = visit.getNext();
                if (next != null) {
                    scoreDirector.beforeVariableChanged(next, "previous");
                    next.setPrevious(previous);
                    scoreDirector.afterVariableChanged(next, "previous");
                }
                scoreDirector.beforeVariableChanged(visit, "previous");
                visit.setPrevious(null);
                scoreDirector.afterVariableChanged(visit, "previous");
            }
            scoreDirector.triggerVariableListeners();
            // Workaround for https://issues.jboss.org/browse/PLANNER-1477
            ((VariableListenerSupport) ((AbstractScoreDirector) scoreDirector).getSupplyManager()).triggerAllVariableListeners();
        });
    }
}
