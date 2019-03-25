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

package com.redhat.demo.optaplanner;

public interface AppConstants {

    /**
     * The number of machines in the visualization.
     * Cannot be changed at runtime because it's hard coded in the UX image.
     */
    @Deprecated
    int MACHINES_LENGTH = 10; // TODO Use AppConfiguration.getMachinesAndGateLength() instead

    /**
     * The number of mechanics to start with upon setup.
     * Can be changed through puppet master UI at runtime.
     */
    int INIT_MECHANICS_LENGTH = 2;

    // TODO Make machine specific?
    long FIX_TIME_MILLIS = 500L;

    long BREATHING_TIME_MILLIS = 200L;

    long TIME_TICK_MILLIS = 40L;
    long OPTA_MACHINE_HEALTH_REFRESH_RATE = TIME_TICK_MILLIS * 15L;

    // the last "machine" is the entry point to the factory
    int ENTRY_POINT_INDEX = AppConstants.MACHINES_LENGTH - 1;

    long ENTRY_POINT_MECHANIC_DELAY = 0L;

}
