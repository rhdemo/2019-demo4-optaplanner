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

public class OptaMechanic extends OptaVisitOrMechanic {

    @PlanningId
    private Integer mechanicIndex;
    private OptaConfiguration optaConfiguration;
    private boolean dummy;
    private double speed;
    private long fixDurationMillis;
    private long thumbUpDurationMillis;

    // The machine component that the mechanic is currently working on, on-route to or has just finished working on
    private OptaMachine focusMachine;
    // When the OptaMechanic will finish with working on focusMachine
    private Long focusDepartureTimeMillis;

    @SuppressWarnings("unused")
    private OptaMechanic() {
    }

    public static OptaMechanic createDummy(OptaConfiguration optaConfiguration) {
        OptaMechanic mechanic = new OptaMechanic(Integer.MIN_VALUE, optaConfiguration, Double.NaN, 0L, 0L, null, null);
        mechanic.dummy = true;
        return mechanic;
    }

    public OptaMechanic(int mechanicIndex, OptaConfiguration optaConfiguration, double speed, long fixDurationMillis, long thumbUpDurationMillis,
            OptaMachine focusMachine, Long focusDepartureTimeMillis) {
        this.mechanicIndex = mechanicIndex;
        this.optaConfiguration = optaConfiguration;
        dummy = false;
        this.speed = speed;
        this.fixDurationMillis = fixDurationMillis;
        this.thumbUpDurationMillis = thumbUpDurationMillis;
        this.focusMachine = focusMachine;
        this.focusDepartureTimeMillis = focusDepartureTimeMillis;
    }

    @Override
    public OptaMachine getMachine() {
        return focusMachine;
    }

    @Override
    public Long getFixOffsetMillis() {
        if (focusDepartureTimeMillis == null) {
            return null;
        }
        long focusDepartureOffsetMillis = focusDepartureTimeMillis - optaConfiguration.getLastDispatchOfAnyMechanicTimeMillis();
        return focusDepartureOffsetMillis - thumbUpDurationMillis;
    }

    @Override
    public String toString() {
        return "OptaMechanic-" + mechanicIndex;
    }

    // ************************************************************************
    // Getter and setters boilerplate
    // ************************************************************************

    public Integer getMechanicIndex() {
        return mechanicIndex;
    }

    public boolean isDummy() {
        return dummy;
    }

    public double getSpeed() {
        return speed;
    }

    public long getFixDurationMillis() {
        return fixDurationMillis;
    }

    public long getThumbUpDurationMillis() {
        return thumbUpDurationMillis;
    }

    public OptaMachine getFocusMachine() {
        return focusMachine;
    }

    public void setFocusMachine(OptaMachine focusMachine) {
        this.focusMachine = focusMachine;
    }

    public long getFocusDepartureTimeMillis() {
        return focusDepartureTimeMillis;
    }

    public void setFocusDepartureTimeMillis(long focusDepartureTimeMillis) {
        this.focusDepartureTimeMillis = focusDepartureTimeMillis;
    }

}
