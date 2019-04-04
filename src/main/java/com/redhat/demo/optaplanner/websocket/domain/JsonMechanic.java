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
    private long focusTravelDurationMillis;
    private long focusFixDurationMillis;

    private int[] futureMachineIndexes;

    public JsonMechanic() {
    }

    public JsonMechanic(Mechanic mechanic, long currentMillis) {
        this.mechanicIndex = mechanic.getMechanicIndex();
        this.originalMachineIndex = mechanic.getOriginalMachineIndex();
        this.focusMachineIndex = mechanic.getFocusMachineIndex();
        this.focusTravelDurationMillis = mechanic.getFocusTravelTimeMillis() - currentMillis;
        this.focusFixDurationMillis = mechanic.getFixDurationMillis();
        this.futureMachineIndexes = mechanic.getFutureMachineIndexes();
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

    public long getFocusTravelDurationMillis() {
        return focusTravelDurationMillis;
    }

    public int[] getFutureMachineIndexes() {
        return futureMachineIndexes;
    }

    public long getFocusFixDurationMillis() {
        return focusFixDurationMillis;
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
