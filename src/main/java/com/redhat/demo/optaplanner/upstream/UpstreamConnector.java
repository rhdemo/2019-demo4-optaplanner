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

package com.redhat.demo.optaplanner.upstream;

import com.redhat.demo.optaplanner.Mechanic;
import com.redhat.demo.optaplanner.config.AppConfiguration;

// TODO Needs better name?
public interface UpstreamConnector {

    /**
     * @return never null, of length {@link AppConfiguration#getMachinesOnlyLength()},
     * index is machineIndex, each element between 0.0 (broken) and 1.0 (full health)
     */
    double[] fetchMachineHealths();

    /**
     * @param machineIndex >= 0, <= {@link AppConfiguration#getMachinesOnlyLength()}
     */
    void resetMachineHealth(int machineIndex);

    /**
     * Update mechanic meta-data
     * @param mechanic never null
     * @param currentTimeMillis never null
     */
    void dispatchMechanic(Mechanic mechanic, long currentTimeMillis);

    /**
     * Add mechanic meta-data
     * @param mechanic never null
     * @param currentTimeMillis never null
     */
    void mechanicAdded(Mechanic mechanic, long currentTimeMillis);

    /**
     * Remove mechanic meta-data, such as in DispatchEvents
     * @param mechanic never null
     */
    void mechanicRemoved(Mechanic mechanic);

    void clearMechanicsAndFutureVisits();

    /**
     * For simulation only.
     * @param machineIndex >= 0, <= {@link AppConfiguration#getMachinesOnlyLength()}
     * @param damage between 0.0 (no damage) and 1.0 (kill it)
     */
    void damageMachine(int machineIndex, double damage);

    /**
     * Sends future visits of a mechanic without moving him.
     * @param mechanicIndex >= 0
     * @param futureMachineIndexes each index >= 0, <= {@link AppConfiguration#getMachinesOnlyLength()}
     */
    void sendFutureVisits(int mechanicIndex, int [] futureMachineIndexes);

    void setDispatchStatus(boolean isDispatchActive);

    void setSimulationStatus(boolean isSimulationActive);

}
