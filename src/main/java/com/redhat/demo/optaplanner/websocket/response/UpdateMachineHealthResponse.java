package com.redhat.demo.optaplanner.websocket.response;

import com.redhat.demo.optaplanner.websocket.domain.JsonMachine;

public class UpdateMachineHealthResponse extends AbstractResponse {
    private JsonMachine[] machines;

    public UpdateMachineHealthResponse(JsonMachine[] machines) {
        super(ResponseType.UPDATE_MACHINE_HEALTHS);
        this.machines = machines;
    }

    public JsonMachine[] getMachines() {
        return machines;
    }
}
