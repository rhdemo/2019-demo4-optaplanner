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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.redhat.demo.optaplanner.domain.JsonMachine;
import com.redhat.demo.optaplanner.domain.JsonMechanic;
import com.redhat.demo.optaplanner.solver.domain.OptaMachine;
import com.redhat.demo.optaplanner.solver.domain.OptaMechanic;
import com.redhat.demo.optaplanner.solver.domain.OptaSolution;
import com.redhat.demo.optaplanner.solver.domain.OptaVisit;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.stereotype.Service;

@Service
public class TravelSolverManager {

    private static final String SOLVER_CONFIG = "com/redhat/demo/optaplanner/solver/travelingMechanicSolverConfig.xml";

    private ExecutorService executorService;
    private SolverFactory<OptaSolution> solverFactory;
    private Solver<OptaSolution> solver;

    private AtomicReference<OptaSolution> bestSolutionReference = new AtomicReference<>();

    public TravelSolverManager() {
        solverFactory = SolverFactory.createFromXmlResource(SOLVER_CONFIG);
    }

    @PostConstruct
    private void initSolver() {
        executorService = Executors.newFixedThreadPool(1);
        solver = solverFactory.buildSolver();
        solver.addEventListener(bestSolutionEvent -> {
            bestSolutionReference.set(bestSolutionEvent.getNewBestSolution());
        });
    }

    @PreDestroy
    private void preDestroy() {
        executorService.shutdownNow();
    }

    public void startSolver(JsonMachine[] jsonMachines, JsonMechanic[] jsonMechanics) {
        List<OptaMachine> machineList = Arrays.stream(jsonMachines)
                .map(jsonMachine -> new OptaMachine(
                        jsonMachine.getMachineIndex(), jsonMachine.getToMachineIndexTravelTimeMillis()))
                .collect(Collectors.toList());
        List<OptaMechanic> mechanicList = Arrays.stream(jsonMechanics)
                .map(jsonMechanic -> {
                    OptaMachine focusMachine = machineList.get(jsonMechanic.getFocusMachineIndex());
                    return new OptaMechanic(
                            jsonMechanic.getMechanicIndex(), focusMachine, jsonMechanic.getFocusDepartureTimeMillis());
                })
                .collect(Collectors.toList());
        List<OptaVisit> visitList = machineList.stream()
                .map(machine -> new OptaVisit(machine.getMachineIndex(), machine))
                .collect(Collectors.toList());
        OptaSolution solution = new OptaSolution(machineList, mechanicList, visitList);
        executorService.submit(() -> solver.solve(solution));
    }

    public void fetchAndUpdateFutureMachineIndexes(JsonMechanic[] jsonMechanics) {
        OptaSolution bestSolution = bestSolutionReference.getAndSet(null);
        if (bestSolution == null) {
            return;
        }
        if (bestSolution.getMachineList().size() != jsonMechanics.length) {
            // The best solution is stale
            return;
        }
        for (int i = 0; i < jsonMechanics.length; i++) {
            JsonMechanic jsonMechanic = jsonMechanics[i];
            OptaMechanic mechanic = bestSolution.getMechanicList().get(i);
            if (mechanic.getFocusMachine().getMachineIndex() != jsonMechanic.getFocusMachineIndex()
                || mechanic.getFocusDepartureTimeMillis() != jsonMechanic.getFocusDepartureTimeMillis()) {
                // The best solution is stale
                return;
            }
        }
        // The best solution isn't stale (except maybe for machine healths, but that's ok)
        for (int i = 0; i < jsonMechanics.length; i++) {
            JsonMechanic jsonMechanic = jsonMechanics[i];
            OptaMechanic mechanic = bestSolution.getMechanicList().get(i);
            List<Integer> futureMachineIndexList = new ArrayList<>(bestSolution.getMachineList().size());
            OptaVisit next = mechanic.getNext();
            while (next != null) {
                futureMachineIndexList.add(next.getMachineIndex());
                next = next.getNext();
            }
            jsonMechanic.setFutureMachineIndexes(futureMachineIndexList.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    public void updateMachineHealths(JsonMachine[] jsonMachines) {
        solver.addProblemFactChange(scoreDirector -> {
            // TODO




        });
    }


    public void addMechanic(int mechanicIndex, OptaMachine focusMachine, long focusFinishTimeMillis) {
        solver.addProblemFactChange(scoreDirector -> {
            OptaSolution solution = scoreDirector.getWorkingSolution();
            // A SolutionCloner clones planning entity lists (such as mechanicList), so no need to clone the mechanicList here
            List<OptaMechanic> mechanicList = solution.getMechanicList();
            OptaMechanic mechanic = new OptaMechanic(mechanicIndex, focusMachine, focusFinishTimeMillis);
            scoreDirector.beforeEntityAdded(mechanic);
            mechanicList.add(mechanic);
            scoreDirector.afterEntityAdded(mechanic);
            scoreDirector.triggerVariableListeners();
        });
    }

    public void removeMechanic(int mechanicIndex) {
        solver.addProblemFactChange(scoreDirector -> {
            OptaSolution solution = scoreDirector.getWorkingSolution();
            // A SolutionCloner clones planning entity lists (such as mechanicList), so no need to clone the mechanicList here
            List<OptaMechanic> mechanicList = solution.getMechanicList();
            OptaMechanic mechanic = mechanicList.get(mechanicIndex);
            scoreDirector.beforeEntityAdded(mechanic);
            mechanicList.remove(mechanicIndex);
            scoreDirector.afterEntityAdded(mechanic);
            scoreDirector.triggerVariableListeners();
        });
    }
}
