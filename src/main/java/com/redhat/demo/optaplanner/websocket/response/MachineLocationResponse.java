package com.redhat.demo.optaplanner.websocket.response;

import com.redhat.demo.optaplanner.websocket.domain.JsonLocation;

public class MachineLocationResponse extends AbstractResponse {

    private JsonLocation[] locations;

    public MachineLocationResponse(JsonLocation[] locations) {
        super(ResponseType.MACHINE_LOCATIONS);
        this.locations = locations;
    }

    public JsonLocation[] getLocations() {
        return locations;
    }
}
