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

package com.redhat.demo.optaplanner.solver.domain;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

@PlanningSolution
public class OptaSolution {

    @ProblemFactProperty
    private OptaConfiguration configuration;

    @ProblemFactCollectionProperty
    private List<OptaMachine> machineList;

    private List<OptaMechanic> mechanicList;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "visitRange")
    private List<OptaVisit> visitList;

    @PlanningScore
    private HardMediumSoftLongScore score;

    private OptaMechanic dummyMechanic;

    @SuppressWarnings("unused")
    private OptaSolution() {
    }

    public OptaSolution(OptaConfiguration configuration, List<OptaMachine> machineList, List<OptaMechanic> mechanicList, List<OptaVisit> visitList) {
        this.configuration = configuration;
        this.machineList = machineList;
        this.mechanicList = mechanicList;
        this.visitList = visitList;
        this.dummyMechanic = OptaMechanic.createDummy(configuration);
    }

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "mechanicRange")
    // TODO Workaround for https://issues.jboss.org/browse/PLANNER-776 if mechanicList is empty
    public List<OptaMechanic> getMechanicsAndDummies() {
        List<OptaMechanic> mechanicsAndDummies = new ArrayList<>(mechanicList);
        mechanicsAndDummies.add(dummyMechanic);
        return mechanicsAndDummies;
    }

    // ************************************************************************
    // Getter and setters boilerplate
    // ************************************************************************

    public OptaConfiguration getConfiguration() {
        return configuration;
    }

    public List<OptaMachine> getMachineList() {
        return machineList;
    }

    public void setMachineList(List<OptaMachine> machineList) {
        this.machineList = machineList;
    }

    public List<OptaMechanic> getMechanicList() {
        return mechanicList;
    }

    public void setMechanicList(List<OptaMechanic> mechanicList) {
        this.mechanicList = mechanicList;
    }

    public List<OptaVisit> getVisitList() {
        return visitList;
    }

    public void setVisitList(List<OptaVisit> visitList) {
        this.visitList = visitList;
    }

    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }

}
