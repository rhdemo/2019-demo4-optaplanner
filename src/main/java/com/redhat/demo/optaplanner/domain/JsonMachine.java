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

package com.redhat.demo.optaplanner.domain;

public class JsonMachine {

    private int machineIndex;
    private long[] toMachineIndexTravelTimeMillis;
    private double health;

    public JsonMachine() {
    }

    public JsonMachine(int machineIndex, long[] toMachineIndexTravelTimeMillis, double health) {
        this.machineIndex = machineIndex;
        this.toMachineIndexTravelTimeMillis = toMachineIndexTravelTimeMillis;
        this.health = health;
    }

    public int getMachineIndex() {
        return machineIndex;
    }

    public void setMachineIndex(int machineIndex) {
        this.machineIndex = machineIndex;
    }

    public long[] getToMachineIndexTravelTimeMillis() {
        return toMachineIndexTravelTimeMillis;
    }

    public void setToMachineIndexTravelTimeMillis(long[] toMachineIndexTravelTimeMillis) {
        this.toMachineIndexTravelTimeMillis = toMachineIndexTravelTimeMillis;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonMachine)) {
            return false;
        }
        JsonMachine machine = (JsonMachine) o;
        return machineIndex == machine.machineIndex;
    }

    @Override
    public int hashCode() {
        return machineIndex;
    }

}
