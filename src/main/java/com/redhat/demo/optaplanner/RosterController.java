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
import java.util.List;

import com.redhat.demo.optaplanner.domain.Machine;
import com.redhat.demo.optaplanner.domain.Mechanic;
import com.redhat.demo.optaplanner.restapi.AbstractResponse;
import com.redhat.demo.optaplanner.restapi.AddMechanicResponse;
import com.redhat.demo.optaplanner.restapi.RemoveMechanicResponse;
import com.redhat.demo.optaplanner.restapi.SetupUIResponse;
import com.redhat.demo.optaplanner.upstream.FakeUpstreamConnector;
import com.redhat.demo.optaplanner.upstream.UpstreamConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

@Controller
public class RosterController {

    private static final Logger log = LoggerFactory.getLogger(RosterController.class);

    // TODO use @Inject
    private UpstreamConnector upstreamConnector = new FakeUpstreamConnector();

    private Machine[] machines;
    private List<Mechanic> mechanicList;

    public RosterController() {
        machines = new Machine[UpstreamConnector.MACHINES_LENGTH];
        for (int i = 0; i < machines.length; i++) {
            machines[i] = new Machine(i, 0.80 + (Math.random() * 0.20));
        }
        int MECHANIC_LIST_SIZE = 2;
        mechanicList = new ArrayList<>(MECHANIC_LIST_SIZE);
        for (int i = 0; i < MECHANIC_LIST_SIZE; i++) {
            mechanicList.add(new Mechanic(i, i, i));
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void tick() {
        log.info("  Tick currently at 1 second ");
        double[] aggregatedDamages = upstreamConnector.fetchAggregatedDamagePerMachine();
        // TODO adjust health
    }

    @MessageMapping("/setupUI")
    @SendTo("/topic/roster")
    public AbstractResponse setupUI() {
        return new SetupUIResponse(machines);
    }

    @MessageMapping("/unpauze")
    public void unpauze() {
        log.info("Unpauze");
        // TODO
    }

    @MessageMapping("/pauze")
    public void pauze() {
        log.info("Pauze");
    }

    @MessageMapping("/addMechanic")
    @SendTo("/topic/roster")
    public AddMechanicResponse addMechanic() {
        int mechanicIndex = mechanicList.size();
        Mechanic mechanic = new Mechanic(mechanicIndex, 0, 0);
        mechanicList.add(mechanic);
        return new AddMechanicResponse(mechanic.getMechanicIndex(), mechanic.getX(), mechanic.getY());
    }

    @MessageMapping("/removeMechanic")
    @SendTo("/topic/roster")
    public AbstractResponse removeMechanic() {
        int mechanicIndex = mechanicList.size() - 1;
        mechanicList.remove(mechanicIndex);
        return new RemoveMechanicResponse(mechanicIndex);
    }

}
