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
import java.util.stream.Collectors;

import com.redhat.demo.optaplanner.Machine;
import com.redhat.demo.optaplanner.Mechanic;
import com.redhat.demo.optaplanner.config.AppConfiguration;
import com.redhat.demo.optaplanner.solver.domain.OptaConfiguration;
import com.redhat.demo.optaplanner.solver.domain.OptaMachine;
import com.redhat.demo.optaplanner.solver.domain.OptaMechanic;
import com.redhat.demo.optaplanner.solver.domain.OptaSolution;
import com.redhat.demo.optaplanner.solver.domain.OptaVisit;
import com.redhat.demo.optaplanner.solver.domain.OptaVisitOrMechanic;
import com.redhat.demo.optaplanner.solver.domain.listener.FixTimeUpdatingVariableListener;
import org.junit.Before;
import org.junit.Test;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.test.impl.score.buildin.hardmediumsoftlong.HardMediumSoftLongScoreVerifier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TravelingMechanicScoreRulesTest {

    public static final int A = 0;
    public static final int B = 1;
    public static final int C = 2;
    public static final int D = 3;
    public static final int E = 4;
    public static final int F = 5;
    public static final int G = 6;
    public static final int H = 7;
    public static final int I = 8;
    public static final int J = 9;

    private AppConfiguration appConfiguration;

    private MyHardMediumSoftLongScoreVerifier<OptaSolution> scoreVerifier = new MyHardMediumSoftLongScoreVerifier<>(
            SolverFactory.createFromXmlResource(TravelSolverManager.SOLVER_CONFIG));

    @Before
    public void before() {
        appConfiguration = new AppConfiguration();
        appConfiguration.readTravelDistanceMatrix();
    }

    @Test
    public void onlyMachineJIsDamaged() {
        long timeMillis = 10_000L;
        OptaSolution solution1 = buildSolution(timeMillis, 1);
        solution1.getMachineList().get(J).setHealth(0.9);
        solution1.getMechanicList().get(0).setFocusMachine(solution1.getMachineList().get(C));
        OptaSolution solution2 = buildSolution(timeMillis, 1);
        solution2.getMachineList().get(J).setHealth(0.9);
        solution2.getMechanicList().get(0).setFocusMachine(solution2.getMachineList().get(C));

        putVisits(solution1, 0, B, J, H);
        putVisits(solution2, 0, J, B, H);
        scoreVerifier.assertScoresOrdered(solution1, solution2);
    }

    private OptaSolution buildSolution(long timeMillis, int initialMechanicsSize) {
        Machine[] machines = new Machine[appConfiguration.getMachinesAndGateLength()];
        for (int i = 0; i < machines.length; i++) {
            int x = appConfiguration.getMachineGridX(i);
            int y = appConfiguration.getMachineGridY(i);
            double[] machineIndexToTravelDistances = appConfiguration.getMachineIndexToTravelDistances(i);
            if (i == appConfiguration.getGateMachineIndex()) {
                machines[i] = Machine.createGate(i, x, y, machineIndexToTravelDistances);
            } else {
                machines[i] = Machine.createMachine(i, x, y, machineIndexToTravelDistances, 1.0);
            }
        }
        List<Mechanic> mechanics = new ArrayList<>();
        for (int i = 0; i < initialMechanicsSize; i++) {
            mechanics.add(new Mechanic(
                    mechanics.size(),
                    appConfiguration.getMechanicSpeed(),
                    appConfiguration.getFixDurationMillis(), appConfiguration.getThumbUpDurationMillis(),
                    appConfiguration.getGateMachineIndex(),
                    appConfiguration.getGateMachineIndex(),
                    timeMillis));
        }
        OptaConfiguration optaConfiguration = new OptaConfiguration(timeMillis);
        List<OptaMachine> machineList = Arrays.stream(machines)
                .map(machine -> {
                    OptaMachine optaMachine = new OptaMachine(
                            machine.getMachineIndex(), machine.getMachineIndexToTravelDistances(), machine.getHealth(), machine.isGate());
                    optaMachine.setHealth(machine.getHealth());
                    return optaMachine;
                })
                .collect(Collectors.toList());
        List<OptaMechanic> mechanicList = mechanics.stream()
                .map(mechanic -> {
                    OptaMachine focusMachine = machineList.get(mechanic.getFocusMachineIndex());
                    return new OptaMechanic(
                            mechanic.getMechanicIndex(), optaConfiguration, mechanic.getSpeed(),
                            mechanic.getFixDurationMillis(), mechanic.getThumbUpDurationMillis(),
                            focusMachine, mechanic.getFocusDepartureTimeMillis());
                })
                .collect(Collectors.toList());
        OptaMechanic dummyMechanic = OptaMechanic.createDummy(optaConfiguration);
        List<OptaVisit> visitList = machineList.stream()
                .filter(machine -> !machine.isGate())
                .map(machine -> new OptaVisit(machine.getMachineIndex(), machine))
                .collect(Collectors.toList());
        return new OptaSolution(optaConfiguration, machineList, mechanicList, visitList);
    }

    private void putVisits(OptaSolution solution, int mechanicIndex, int... futureVisitIndexes) {
        OptaMechanic mechanic = solution.getMechanicList().get(mechanicIndex);
        OptaVisitOrMechanic previous = mechanic;
        for (int i = 0; i < futureVisitIndexes.length; i++) {
            int futureVisitIndex = futureVisitIndexes[i];
            OptaVisit visit = solution.getVisitList().get(futureVisitIndex);
            previous.setNext(visit);
            visit.setPrevious(previous);
            visit.setMechanic(mechanic);
            previous = visit;
        }
        // TODO https://issues.jboss.org/browse/PLANNER-1316
        new FixTimeUpdatingVariableListener().afterVariableChanged(mock(ScoreDirector.class), solution.getVisitList().get(futureVisitIndexes[0]));
    }

    // TODO https://issues.jboss.org/browse/PLANNER-1476
    private class MyHardMediumSoftLongScoreVerifier<Solution_> extends HardMediumSoftLongScoreVerifier<Solution_> {

        public MyHardMediumSoftLongScoreVerifier(SolverFactory<Solution_> solverFactory) {
            super(solverFactory);
        }

        public void assertScoresOrdered(Solution_... solutions) {
            Score[] scores = Arrays.stream(solutions).map(solution -> {
                ScoreDirector<Solution_> scoreDirector = scoreDirectorFactory.buildScoreDirector();
                scoreDirector.setWorkingSolution(solution);
                return scoreDirector.calculateScore();
            }).toArray(Score[]::new);
            for (int i = 0; i < scores.length -1; i++) {
                int j = i + 1;
                if (scores[i].compareTo(scores[j]) >= 0) {
                    fail("The solution " + i + " with score (" + scores[i]
                            + ") must be worse than solution " + j + " with score (" + scores[j] + ").");
                }
            }
        }

    }
}
