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

import java.util.Arrays;
import javax.annotation.PostConstruct;

import com.redhat.demo.optaplanner.Mechanic;
import com.redhat.demo.optaplanner.SpringProfiles;
import com.redhat.demo.optaplanner.config.AppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(SpringProfiles.DEVELOPMENT)
public class FakeUpstreamConnector implements UpstreamConnector {

    @Autowired
    private AppConfiguration appConfiguration;

    private double[] machineHealths;

    @PostConstruct
    public void postConstruct() {
        machineHealths = new double[appConfiguration.getMachinesOnlyLength()];
        Arrays.fill(machineHealths, 0.9);
    }

    @Override
    public synchronized double[] fetchMachineHealths() {
        return Arrays.copyOf(machineHealths, machineHealths.length);
    }

    @Override
    public synchronized void resetMachineHealth(int machineIndex) {
        machineHealths[machineIndex] = 1.0;
    }

    @Override
    public void dispatchMechanic(Mechanic mechanic, long currentTimeMillis) {
    }

    @Override
    public void mechanicAdded(Mechanic mechanic, long currentTimeMillis) {
    }

    @Override
    public void mechanicRemoved(Mechanic mechanic) {
    }

    @Override
    public void clearMechanicsAndFutureVisits() {
    }

    @Override
    public synchronized void damageMachine(int machineIndex, double damage) {
        machineHealths[machineIndex] = Math.max(0.0, machineHealths[machineIndex] - damage);
    }

    @Override
    public void sendFutureVisits(int mechanicIndex, int [] futureMachineIndexes) {
    }

    @Override
    public void setDispatchStatus(boolean isDispatchActive) {

    }

    @Override
    public void setSimulationStatus(boolean isSimulationActive) {

    }
}
