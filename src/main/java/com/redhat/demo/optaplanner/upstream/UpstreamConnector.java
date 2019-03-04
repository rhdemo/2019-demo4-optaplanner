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

import java.util.List;

// TODO Needs better name?
public interface UpstreamConnector {

    // The number is hard coded in the UX image, so there is no point in having it read from configuration
    int MACHINES_LENGTH = 20;

    /**
     * Similar to {@link #fetchRawDamageEventBatch()} but aggregates per machine.
     * @return never null, of length {@value #MACHINES_LENGTH}, each index represents a machineIndex.
     */
    default double[] fetchAggregatedDamagePerMachine() {
        double[] aggregatedDamages = new double[MACHINES_LENGTH];
        for (DamageEvent damageEvent : fetchRawDamageEventBatch()) {
            aggregatedDamages[damageEvent.getMachineIndex()] += damageEvent.getDamage();
        }
        return aggregatedDamages;
    }

    /**
     * Fetches all events up to now that haven't been fetched before.
     * @return never null, might contain the same machine twice
     */
    List<DamageEvent> fetchRawDamageEventBatch();

    /**
     * Use for simulations only!
     * @param damageEventList never null
     */
    void addRawDamageEventBatch(List<DamageEvent> damageEventList);

}
