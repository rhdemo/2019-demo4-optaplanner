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
    int MACHINES_LENGTH = 10; // TODO Read tokens length of resource /travelTimeMillisMatrix.csv instead

    /**
     * The travel time from any machineIndex to any other machineIndex.
     * Travel time might be asymmetric: from A to B might differ from B to A.
     * The first index is the fromMachineIndex, the second index is the toMachineIndex.
     */
    long[][] TRAVEL_TIME_MILLIS_MATRIX = { // TODO Read resource /travelTimeMillisMatrix.csv instead
            {0, 1287, 1562, 75, 2091, 2623, 1242, 1002, 1475, 1503},
            {1287, 0, 2200, 1283, 1663, 2184, 1376, 1421, 782, 324},
            {1562, 2200, 0, 1637, 1561, 1866, 891, 779, 2782, 2180},
            {75, 1283, 1637, 0, 2148, 2683, 1309, 1074, 1431, 1512},
            {2091, 1663, 1561, 2148, 0, 550, 926, 1231, 2445, 1419},
            {2623, 2184, 1866, 2683, 550, 0, 1415, 1712, 2967, 1917},
            {1242, 1376, 891, 1309, 926, 1415, 0, 305, 2055, 1307},
            {1002, 1421, 779, 1074, 1231, 1712, 305, 0, 2019, 1421},
            {1475, 782, 2782, 1431, 2445, 2967, 2055, 2019, 0, 1067},
            {1503, 324, 2180, 1512, 1419, 1917, 1307, 1421, 1067, 0}
    };

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
