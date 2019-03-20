package com.redhat.demo.optaplanner.websocket;

import com.redhat.demo.optaplanner.DownstreamConnector;
import com.redhat.demo.optaplanner.GameController;
import com.redhat.demo.optaplanner.Machine;
import com.redhat.demo.optaplanner.Mechanic;
import com.redhat.demo.optaplanner.websocket.domain.JsonMachine;
import com.redhat.demo.optaplanner.websocket.domain.JsonMechanic;
import com.redhat.demo.optaplanner.websocket.response.AddMechanicResponse;
import com.redhat.demo.optaplanner.websocket.response.DispatchMechanicResponse;
import com.redhat.demo.optaplanner.websocket.response.UpdateMachineHealthResponse;
import com.redhat.demo.optaplanner.websocket.response.RemoveMechanicResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class OptaPlannerWebSocketController implements DownstreamConnector {

    private static final String WEB_SOCKET_ENDPOINT = "/topic/roster";

    @Autowired
    private GameController gameController;

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/pauze")
    public void pauze() {
        gameController.pauseGame();
    }

    @MessageMapping("/unpauze")
    public void unpauze() {
        gameController.resumeGame();
    }

    @MessageMapping("/addMechanic")
    public void addMechanic() {
        gameController.addMechanic();
    }

    @MessageMapping("/removeMechanic")
    public void removeMechanic() {
        gameController.removeMechanic();
    }

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
    public void dispatchMechanic(Mechanic mechanic) {
        JsonMechanic jsonMechanic = new JsonMechanic(mechanic.getMechanicIndex(),
                                                     mechanic.getFocusMachineIndex(),
                                                     mechanic.getFocusDepartureTimeMillis());
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

    private JsonMachine convertMachineToJson(Machine machine) {
        return new JsonMachine(machine.getMachineIndex(), machine.getHealth());
    }

    private JsonMechanic convertMechanicToJson(Mechanic mechanic) {
        return new JsonMechanic(mechanic.getMechanicIndex(),
                                mechanic.getFocusMachineIndex(),
                                mechanic.getFocusDepartureTimeMillis(),
                                mechanic.getFutureMachineIndexes());
    }
}
