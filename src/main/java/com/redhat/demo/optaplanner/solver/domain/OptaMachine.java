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

import org.optaplanner.core.api.domain.lookup.PlanningId;

public class OptaMachine {

    @PlanningId
    private int machineIndex;
    private long[] toMachineIndexTravelTimeMillis;

    private double health;

    public OptaMachine(int machineIndex, long[] toMachineIndexTravelTimeMillis) {
        this.machineIndex = machineIndex;
        this.toMachineIndexTravelTimeMillis = toMachineIndexTravelTimeMillis;
    }

    public long getTravelTimeMillisTo(OptaMachine machine) {
        return this.toMachineIndexTravelTimeMillis[machine.getMachineIndex()];
    }

    @Override
    public String toString() {
        return "OptaMachine-" + machineIndex;
    }

    // ************************************************************************
    // Getter and setters boilerplate
    // ************************************************************************

    public int getMachineIndex() {
        return machineIndex;
    }

    public long[] getToMachineIndexTravelTimeMillis() {
        return toMachineIndexTravelTimeMillis;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

}
