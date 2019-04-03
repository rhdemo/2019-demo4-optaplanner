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

package com.redhat.demo.optaplanner.websocket.domain;

import com.redhat.demo.optaplanner.Mechanic;

public class JsonMechanic {

    private int mechanicIndex;
    private int originalMachineIndex;
    private int focusMachineIndex;
    private long focusTravelTimeMillis;
    private long focusFixTimeMillis;

    private int[] futureMachineIndexes;

    public JsonMechanic() {
    }

    public JsonMechanic(Mechanic mechanic, long currentMillis) {
        this(mechanic.getMechanicIndex(),
                mechanic.getOriginalMachineIndex(),
                mechanic.getFocusMachineIndex(),
                mechanic.getFocusTravelTimeMillis() - currentMillis,
                mechanic.getFixDurationMillis(),
                mechanic.getFutureMachineIndexes());
    }

    public JsonMechanic(int mechanicIndex,
                        int originalMachineIndex,
                        int focusMachineIndex,
                        long focusTravelTimeMillis,
                        long focusFixTimeMillis,
                        int[] futureMachineIndexes) {
        this.mechanicIndex = mechanicIndex;
        this.originalMachineIndex = originalMachineIndex;
        this.focusMachineIndex = focusMachineIndex;
        this.focusTravelTimeMillis = focusTravelTimeMillis;
        this.focusFixTimeMillis = focusFixTimeMillis;
        this.futureMachineIndexes = futureMachineIndexes;
    }

    public int getMechanicIndex() {
        return mechanicIndex;
    }
    
    public int getOriginalMachineIndex() {
        return originalMachineIndex;
    }

    public int getFocusMachineIndex() {
        return focusMachineIndex;
    }

    public long getFocusTravelTimeMillis() {
        return focusTravelTimeMillis;
    }

    public int[] getFutureMachineIndexes() {
        return futureMachineIndexes;
    }

    public long getFocusFixTimeMillis() {
        return focusFixTimeMillis;
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
