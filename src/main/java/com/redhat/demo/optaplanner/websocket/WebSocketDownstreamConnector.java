package com.redhat.demo.optaplanner.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.redhat.demo.optaplanner.DownstreamConnector;
import com.redhat.demo.optaplanner.Machine;
import com.redhat.demo.optaplanner.Mechanic;
import com.redhat.demo.optaplanner.websocket.domain.JsonLocation;
import com.redhat.demo.optaplanner.websocket.domain.JsonMachine;
import com.redhat.demo.optaplanner.websocket.domain.JsonMechanic;
import com.redhat.demo.optaplanner.websocket.response.AddMechanicResponse;
import com.redhat.demo.optaplanner.websocket.response.DispatchMechanicResponse;
import com.redhat.demo.optaplanner.websocket.response.MachineLocationResponse;
import com.redhat.demo.optaplanner.websocket.response.RemoveMechanicResponse;
import com.redhat.demo.optaplanner.websocket.response.UpdateMachineHealthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketDownstreamConnector implements DownstreamConnector {

    private static final String WEB_SOCKET_ENDPOINT = "/topic/roster";

    @Autowired
    private SimpMessagingTemplate template;

    @Override
    public void mechanicAdded(Mechanic mechanic, long currentMillis) {
        AddMechanicResponse addMechanicResponse = new AddMechanicResponse(new JsonMechanic(mechanic, currentMillis));
        this.template.convertAndSend(WEB_SOCKET_ENDPOINT, addMechanicResponse);
    }

    @Override
    public void mechanicRemoved(Mechanic mechanic) {
        RemoveMechanicResponse removeMechanicResponse = new RemoveMechanicResponse(mechanic.getMechanicIndex());
        this.template.convertAndSend(WEB_SOCKET_ENDPOINT, removeMechanicResponse);
    }

    @Override
    public void dispatchMechanic(Mechanic mechanic, long currentMillis) {
        JsonMechanic jsonMechanic = new JsonMechanic(mechanic, currentMillis);
        DispatchMechanicResponse dispatchMechanicResponse =
                new DispatchMechanicResponse(jsonMechanic);
        this.template.convertAndSend(WEB_SOCKET_ENDPOINT, dispatchMechanicResponse);
    }

    @Override
    public void updateMachinesHealths(Machine[] machines) {
        JsonMachine[] jsonMachines = Arrays.stream(machines)
                .filter(machine -> !machine.isGate())
                .map(this::convertMachineToJson).toArray(JsonMachine[]::new);
        UpdateMachineHealthResponse updateMachineHealthResponse = new UpdateMachineHealthResponse(jsonMachines);
        this.template.convertAndSend(WEB_SOCKET_ENDPOINT, updateMachineHealthResponse);
    }

    @Override
    public void sendMachineLocations(Machine[] machines) {
        JsonLocation [] locations = Arrays.stream(machines)
                .map(machine -> new JsonLocation(machine.getX(), machine.getY()))
                .toArray(JsonLocation[]::new);

        MachineLocationResponse machineLocations = new MachineLocationResponse(locations);
        this.template.convertAndSend(WEB_SOCKET_ENDPOINT, machineLocations);
    }

    private JsonMachine convertMachineToJson(Machine machine) {
        return new JsonMachine(machine.getMachineIndex(), machine.getHealth());
    }
}
