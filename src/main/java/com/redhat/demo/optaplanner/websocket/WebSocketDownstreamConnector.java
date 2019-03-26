package com.redhat.demo.optaplanner.websocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public void mechanicAdded(Mechanic mechanic) {
        AddMechanicResponse addMechanicResponse = new AddMechanicResponse(mechanic.getMechanicIndex());
        this.template.convertAndSend(WEB_SOCKET_ENDPOINT, addMechanicResponse);
    }

    @Override
    public void mechanicRemoved(Mechanic mechanic) {
        RemoveMechanicResponse removeMechanicResponse = new RemoveMechanicResponse(mechanic.getMechanicIndex());
        this.template.convertAndSend(WEB_SOCKET_ENDPOINT, removeMechanicResponse);
    }

    @Override
    public void dispatchMechanic(Mechanic mechanic, long currentMillis) {
        JsonMechanic jsonMechanic = new JsonMechanic(mechanic.getMechanicIndex(),
                                                     mechanic.getCurrentMachineIndex(),
                                                     mechanic.getFocusMachineIndex(),
                                                     mechanic.getFocusTravelTimeMillis() - currentMillis,
                                                     mechanic.getFixDurationMillis(),
                                                     mechanic.getFutureMachineIndexes());
        DispatchMechanicResponse dispatchMechanicResponse =
                new DispatchMechanicResponse(jsonMechanic);
        this.template.convertAndSend(WEB_SOCKET_ENDPOINT, dispatchMechanicResponse);
    }

    @Override
    public void updateMachinesHealths(Machine[] machines) {
        JsonMachine[] jsonMachines = new JsonMachine[machines.length];
        for (int i = 0; i < jsonMachines.length; i++) {
            jsonMachines[i] = convertMachineToJson(machines[i]);
        }
        UpdateMachineHealthResponse updateMachineHealthResponse = new UpdateMachineHealthResponse(jsonMachines);
        this.template.convertAndSend(WEB_SOCKET_ENDPOINT, updateMachineHealthResponse);
    }

    @Override
    public void machineLocations() {
        List<JsonLocation> locationList;
        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/locations")))) {
            locationList = bufferedReader.lines().map(line -> {
                String [] numbers = line.split(",");
                int x = Integer.parseInt(numbers[0]);
                int y = Integer.parseInt(numbers[1]);
                return new JsonLocation(x, y);
            }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read machine locations.");
        }

        MachineLocationResponse machineLocations = new MachineLocationResponse(locationList.toArray(new JsonLocation[0]));
        this.template.convertAndSend(WEB_SOCKET_ENDPOINT, machineLocations);
    }

    private JsonMachine convertMachineToJson(Machine machine) {
        return new JsonMachine(machine.getMachineIndex(), machine.getHealth());
    }
}
