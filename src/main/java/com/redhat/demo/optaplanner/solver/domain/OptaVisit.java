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

import com.redhat.demo.optaplanner.solver.domain.listener.FixTimeUpdatingVariableListener;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

@PlanningEntity
public class OptaVisit extends OptaVisitOrMechanic {

    @PlanningId
    private Integer machineIndex;
    private OptaMachine machine;

    @PlanningVariable(valueRangeProviderRefs = {"mechanicRange", "dummyMechanicRange", "visitRange"},
            graphType = PlanningVariableGraphType.CHAINED)
    private OptaVisitOrMechanic previous;

    @AnchorShadowVariable(sourceVariableName = "previous")
    private OptaMechanic mechanic;
    @CustomShadowVariable(variableListenerClass = FixTimeUpdatingVariableListener.class,
            sources = {@PlanningVariableReference(variableName = "previous"),
                    @PlanningVariableReference(variableName = "mechanic")})
    private Long fixTimeMillis = null; // Always after OptaMechanic.focusDepartureTimeMillis

    @SuppressWarnings("unused")
    private OptaVisit() {
    }

    public OptaVisit(int machineIndex, OptaMachine machine) {
        this.machineIndex = machineIndex;
        this.machine = machine;
    }

    public boolean isMachineFocused() {
        return machine.isFocused();
    }

    public boolean isMechanicDummy() {
        return mechanic != null && mechanic.isDummy();
    }

    public Long getTravelTimeMillisFromPrevious() {
        if (previous == null) {
            return null;
        }
        return previous.getMachine().getTravelTimeMillisTo(machine, mechanic);
    }

    public long getAttritionMicros() {
        double attrition = 1.0 - machine.getHealth();
        return (long) (attrition * 1_000_000.0);
    }

    @Override
    public String toString() {
        return "OptaVisit-" + machineIndex;
    }

    // ************************************************************************
    // Getter and setters boilerplate
    // ************************************************************************

    public Integer getMachineIndex() {
        return machineIndex;
    }

    @Override
    public OptaMachine getMachine() {
        return machine;
    }

    public OptaVisitOrMechanic getPrevious() {
        return previous;
    }

    public void setPrevious(OptaVisitOrMechanic previous) {
        this.previous = previous;
    }

    public OptaMechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(OptaMechanic mechanic) {
        this.mechanic = mechanic;
    }

    @Override
    public Long getFixTimeMillis() {
        return fixTimeMillis;
    }

    public void setFixTimeMillis(Long fixTimeMillis) {
        this.fixTimeMillis = fixTimeMillis;
    }

}
