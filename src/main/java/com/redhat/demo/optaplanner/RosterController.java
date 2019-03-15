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

import javax.annotation.PostConstruct;

import com.redhat.demo.optaplanner.domain.JsonMachine;
import com.redhat.demo.optaplanner.domain.JsonMechanic;
import com.redhat.demo.optaplanner.restapi.AbstractResponse;
import com.redhat.demo.optaplanner.restapi.AddMechanicResponse;
import com.redhat.demo.optaplanner.restapi.SetupUIResponse;
import com.redhat.demo.optaplanner.solver.TravelSolverManager;
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

    private static final String WEB_SOCKET_ENDPOINT = "/topic/roster";
    private static final Logger log = LoggerFactory.getLogger(RosterController.class);

    @Autowired
    private UpstreamConnector upstreamConnector;
    @Autowired
    private TravelSolverManager solverManager;

    @Autowired
    private SimpMessagingTemplate template;

    private boolean pauzed = false;
    private long timeMillis = 0L; // This class is the keeper of time
    private JsonMachine[] machines;
    private JsonMechanic[] mechanics;

    public RosterController() {
    }

    @PostConstruct
    public void init() {
        machines = new JsonMachine[UpstreamConnector.MACHINES_LENGTH];
        double[] machineHealths = upstreamConnector.fetchMachineHealths();
        for (int i = 0; i < machines.length; i++) {
            long[] toMachineIndexTravelTimeMillis = AppConstants.TRAVEL_TIME_MILLIS_MATRIX[i];
            machines[i] = new JsonMachine(i, toMachineIndexTravelTimeMillis, machineHealths[i]);
        }
        mechanics = new JsonMechanic[AppConstants.INIT_MECHANICS_LENGTH];
        for (int i = 0; i < mechanics.length; i++) {
            mechanics[i] = new JsonMechanic(i, i, timeMillis);
        }
        solverManager.startSolver(machines, mechanics);
    }

    @Scheduled(fixedDelay = AppConstants.TIME_TICK_MILLIS)
    public void tick() {
        if (pauzed) {
            return;
        }
        timeMillis += AppConstants.TIME_TICK_MILLIS;

        // Update futureMachineIndexes first (it might affect mechanic dispatch events)
        solverManager.updateFutureMachineIndexes(mechanics);

        // Update machine healths
        double[] machineHealths = upstreamConnector.fetchMachineHealths();
        for (int i = 0; i < machines.length; i++) {
            machines[i].setHealth(machineHealths[i]);
        }
        if (timeMillis % AppConstants.OPTA_MACHINE_HEALTH_REFRESH_RATE == 0L) {
            solverManager.updateMachineHealths(machines);
        }
        // TODO send MachineHealthResponse to websocket (iff open)

        // Check mechanic fixed or departure events
        for (int i = 0; i < mechanics.length; i++) {
            JsonMechanic mechanic = mechanics[i];
            if (timeMillis >= mechanic.getFocusDepartureTimeMillis() - AppConstants.FIX_TIME_MILLIS) {
                // TODO If it didn't already happen for this fix case...
                // upstreamConnector.resetMachineHealth(mechanic.getFocusMachineIndex());
            }
            if (timeMillis >= mechanic.getFocusDepartureTimeMillis()) {
                int[] futureMachineIndexes = mechanic.getFutureMachineIndexes();
                int futureMachineIndex = futureMachineIndexes.length <= 0 ? mechanic.getFocusMachineIndex()
                        : futureMachineIndexes[0];
                mechanic.setFocusMachineIndex(futureMachineIndex);
                mechanic.setFocusDepartureTimeMillis(timeMillis + AppConstants.FIX_TIME_MILLIS + AppConstants.BREATHING_TIME_MILLIS);
                // TODO send Dispatch event to websocket (iff open)
            }
        }

        // TODO The effectively adding or removing of mechanics we want to do in this thread too
        // TODO To avoid a race condition on JsonMachine[] machines and JsonMechanic[] mechanics



//        if (timeMillis % 5000 == 0) {
//            log.info("  Ticked 1 second.");
//            long[] machineHealths = upstreamConnector.fetchMachineHealths();
//            updateMachineHealth(machineHealths);
//
//            // TODO If a mechanic has fixed a machine, reset health and dispatch it to next location
//            mechanicList.stream()
//                    .filter(mechanic -> mechanic.getFocusMachineIndex() != null)
//                    .collect(Collectors.toList())
//                    .forEach(mechanic -> {
//                        // fix the machine
//                        machines[mechanic.getFocusMachineIndex()].setHealth(1.0);
//                        // make the mechanic available again
//                        mechanic.setFocusMachineIndex(null);
//                    });

//            // simplification: pick the component with the lowest health.
//            // TODO: connect OptaPlanner
//            List<JsonMechanic> availableMechanics = getAvailableMechanics();
//            List<JsonMachine> lowestHealthMachines = getMachinesWithLowestHealth(availableMechanics.size());
//
//            int available = Math.min(availableMechanics.size(), lowestHealthMachines.size());
//            for (int i = 0; i < available; i++) {
//                JsonMechanic mechanic = availableMechanics.get(i);
//                JsonMachine machine = lowestHealthMachines.get(i);
//                mechanic.setFocusMachineIndex(machine.getMachineIndex());
//                DispatchMechanicResponse dispatchMechanicResponse =
//                        new DispatchMechanicResponse(mechanic.getMechanicIndex(), machine.getMachineIndex());
//                this.template.convertAndSend(WEB_SOCKET_ENDPOINT, dispatchMechanicResponse);
//            }
//        }
    }

//    private List<JsonMechanic> getAvailableMechanics() {
//        return mechanicList.stream().filter(mechanic -> mechanic.getFocusMachineIndex() == null).collect(Collectors.toList());
//    }
//
//    private List<JsonMachine> getMachinesWithLowestHealth(int maxCount) {
//        TreeSet<JsonMachine> sortedMachines = Arrays.stream(machines)
//                .filter(machine -> machine.getHealth() < 1.0)
//                .collect(Collectors.toCollection(TreeSet::new));
//        maxCount = Math.min(sortedMachines.size(), maxCount);
//        List<JsonMachine> machinesWithLowestHealth = new ArrayList<>(maxCount);
//
//        for (int i = 0; i < maxCount; i++) {
//            machinesWithLowestHealth.add(sortedMachines.pollFirst());
//        }
//
//        return machinesWithLowestHealth;
//    }

    @MessageMapping("/setupUI")
    @SendTo(WEB_SOCKET_ENDPOINT)
    public AbstractResponse setupUI() {
        return new SetupUIResponse(machines, mechanics);
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
    @SendTo(WEB_SOCKET_ENDPOINT)
    public AddMechanicResponse addMechanic() {
        return null; // TODO Doing the code below in the rest thread instead of the tick() thread causes race conditions
//        int mechanicIndex = mechanicList.size();
//        JsonMechanic mechanic = new JsonMechanic(mechanicIndex, null);
//        mechanicList.add(mechanic);
//        solverManager.addMechanic(mechanicIndex, foo, foo);
//        return new AddMechanicResponse(mechanic.getMechanicIndex());
    }

    @MessageMapping("/removeMechanic")
    @SendTo(WEB_SOCKET_ENDPOINT)
    public AbstractResponse removeMechanic() {
        return null; // TODO Doing the code below in the rest thread instead of the tick() thread causes race conditions
//        if (mechanicList.size() <= 1) {
//            throw new IllegalStateException(
//                    "Remove mechanic failed because there must always be at least one mechanic.");
//        }
//        int mechanicIndex = mechanicList.size() - 1;
//        mechanicList.remove(mechanicIndex);
//        solverManager.removeMechanic(mechanicIndex);
//        return new RemoveMechanicResponse(mechanicIndex);
    }

}
