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

public class JsonMechanic {

    private int mechanicIndex;

    private int focusMachineIndex;
    private long focusDepartureTimeMillis;

    private int[] futureMachineIndexes;

    public JsonMechanic() {
    }

    public JsonMechanic(int mechanicIndex, int focusMachineIndex, long focusDepartureTimeMillis) {
        this.mechanicIndex = mechanicIndex;
        this.focusMachineIndex = focusMachineIndex;
        this.focusDepartureTimeMillis = focusDepartureTimeMillis;
        futureMachineIndexes = new int[0];
    }

    public int getMechanicIndex() {
        return mechanicIndex;
    }

    public void setMechanicIndex(int mechanicIndex) {
        this.mechanicIndex = mechanicIndex;
    }

    public int getFocusMachineIndex() {
        return focusMachineIndex;
    }

    public void setFocusMachineIndex(int focusMachineIndex) {
        this.focusMachineIndex = focusMachineIndex;
    }

    public long getFocusDepartureTimeMillis() {
        return focusDepartureTimeMillis;
    }

    public void setFocusDepartureTimeMillis(long focusDepartureTimeMillis) {
        this.focusDepartureTimeMillis = focusDepartureTimeMillis;
    }

    public int[] getFutureMachineIndexes() {
        return futureMachineIndexes;
    }

    public void setFutureMachineIndexes(int[] futureMachineIndexes) {
        this.futureMachineIndexes = futureMachineIndexes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonMechanic)) {
            return false;
        }
        JsonMechanic mechanic = (JsonMechanic) o;
        return mechanicIndex == mechanic.mechanicIndex;
    }

    @Override
    public int hashCode() {
        return mechanicIndex;
    }

}
