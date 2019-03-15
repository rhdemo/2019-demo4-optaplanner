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

import com.redhat.demo.optaplanner.AppConstants;

// TODO Needs better name?
public interface UpstreamConnector {

    // TODO remove this and directly use AppConstants.MACHINES_LENGTH
    int MACHINES_LENGTH = AppConstants.MACHINES_LENGTH;

    /**
     * @return never null, of length {@value MACHINES_LENGTH},
     * index is machineIndex, each element between 0.0 (broken) and 1.0 (full health)
     */
    double[] fetchMachineHealths();

    /**
     * @param machineIndex >= 0, <= {@value MACHINES_LENGTH}
     */
    void resetMachineHealth(int machineIndex);

    /**
     * For simulation only.
     * @param machineIndex >= 0, <= {@value MACHINES_LENGTH}
     * @param damage between 0.0 (no damage) and 1.0 (kill it)
     */
    void damageMachine(int machineIndex, double damage);

}
