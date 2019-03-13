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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.redhat.demo.optaplanner.domain.Machine;
import com.redhat.demo.optaplanner.domain.Mechanic;
import com.redhat.demo.optaplanner.restapi.AbstractResponse;
import com.redhat.demo.optaplanner.restapi.AddMechanicResponse;
import com.redhat.demo.optaplanner.restapi.DispatchMechanicResponse;
import com.redhat.demo.optaplanner.restapi.RemoveMechanicResponse;
import com.redhat.demo.optaplanner.restapi.SetupUIResponse;
import com.redhat.demo.optaplanner.upstream.UpstreamConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class RosterController {

    private static final String WEBSOCKET_ENDPOINT = "/topic/roster";

    private static final Logger log = LoggerFactory.getLogger(RosterController.class);
    public static final int TIME_TICK_MILLIS = 40;

    @Autowired
    private UpstreamConnector upstreamConnector;

    @Autowired
    private SimpMessagingTemplate template;

    private Machine[] machines;
    private List<Mechanic> mechanicList;

    private boolean pauzed = false;
    private long timeMillis = 0L; // This class is the keeper of time

    public RosterController() {
        machines = new Machine[UpstreamConnector.MACHINES_LENGTH];
        for (int i = 0; i < machines.length; i++) {
            machines[i] = new Machine(i, 0.80 + (Math.random() * 0.20));
        }
        int MECHANIC_LIST_SIZE = 2;
        mechanicList = new ArrayList<>(MECHANIC_LIST_SIZE);
        for (int i = 0; i < MECHANIC_LIST_SIZE; i++) {
            mechanicList.add(new Mechanic(i, null));
        }
    }

    @Scheduled(fixedDelay = TIME_TICK_MILLIS)
    public void tick() {
        if (pauzed) {
            return;
        }
        timeMillis += TIME_TICK_MILLIS;
        if (timeMillis % 5000 == 0) {
            log.info("  Ticked 1 second.");
//            double[] aggregatedDamages = upstreamConnector.fetchAggregatedDamagePerMachine();
//            updateMachineHealth(aggregatedDamages);

            // TODO If a mechanic has fixed a machine, reset health and dispatch it to next location
            mechanicList.stream()
                    .filter(mechanic -> mechanic.getMachineIndex() != null)
                    .collect(Collectors.toList())
                    .forEach(mechanic -> {
                        // fix the machine
                        machines[mechanic.getMachineIndex()].setHealth(1.0);
                        // make the mechanic available again
                        mechanic.setMachineIndex(null);
                    });

            // simplification: pick the component with the lowest health.
            // TODO: connect OptaPlanner
            List<Mechanic> availableMechanics = getAvailableMechanics();
            List<Machine> lowestHealthMachines = getMachinesWithLowestHealth(availableMechanics.size());

            int available = Math.min(availableMechanics.size(), lowestHealthMachines.size());
            for (int i = 0; i < available; i++) {
                Mechanic mechanic = availableMechanics.get(i);
                Machine machine = lowestHealthMachines.get(i);
                mechanic.setMachineIndex(machine.getMachineIndex());
                DispatchMechanicResponse dispatchMechanicResponse =
                        new DispatchMechanicResponse(mechanic.getMechanicIndex(), machine.getMachineIndex());
                this.template.convertAndSend(WEBSOCKET_ENDPOINT, dispatchMechanicResponse);
            }
        }
    }

    private List<Mechanic> getAvailableMechanics() {
        return mechanicList.stream().filter(mechanic -> mechanic.getMachineIndex() == null).collect(Collectors.toList());
    }

    private void updateMachineHealth(double[] aggregatedDamages) {
        if (machines.length != aggregatedDamages.length) {
            throw new IllegalArgumentException(
                    "The number of machines and aggregated damages must be the same. Machines ("
                            + machines.length + "), aggregated damages (" + aggregatedDamages.length + ")");
        }

        for (int i = 0; i < machines.length; i++) {
            double value = machines[i].getHealth() - aggregatedDamages[i];
            if (value < 0.0) {
                value = 0.0;
            }
            machines[i].setHealth(value);
        }
    }

    private List<Machine> getMachinesWithLowestHealth(int maxCount) {
        TreeSet<Machine> sortedMachines = Arrays.stream(machines)
                .filter(machine -> machine.getHealth() < 1.0)
                .collect(Collectors.toCollection(TreeSet::new));
        maxCount = Math.min(sortedMachines.size(), maxCount);
        List<Machine> machinesWithLowestHealth = new ArrayList<>(maxCount);

        for (int i = 0; i < maxCount; i++) {
            machinesWithLowestHealth.add(sortedMachines.pollFirst());
        }

        return machinesWithLowestHealth;
    }

    @MessageMapping("/setupUI")
    @SendTo(WEBSOCKET_ENDPOINT)
    public AbstractResponse setupUI() {
        return new SetupUIResponse(machines, mechanicList.toArray(new Mechanic[0]));
    }

    @MessageMapping("/pauze")
    public void pauze() {
        log.info("Pauze");
        pauzed = true;
    }

    @MessageMapping("/unpauze")
    public void unpauze() {
        log.info("Unpauze");
        pauzed = false;
    }

    @MessageMapping("/addMechanic")
    @SendTo(WEBSOCKET_ENDPOINT)
    public AddMechanicResponse addMechanic() {
        int mechanicIndex = mechanicList.size();
        Mechanic mechanic = new Mechanic(mechanicIndex, null);
        mechanicList.add(mechanic);
        return new AddMechanicResponse(mechanic.getMechanicIndex());
    }

    @MessageMapping("/removeMechanic")
    @SendTo(WEBSOCKET_ENDPOINT)
    public AbstractResponse removeMechanic() {
        int mechanicIndex = mechanicList.size() - 1;
        if (mechanicList.size() > 0) {
            mechanicList.remove(mechanicIndex);
        }
        return new RemoveMechanicResponse(mechanicIndex);
    }
}
