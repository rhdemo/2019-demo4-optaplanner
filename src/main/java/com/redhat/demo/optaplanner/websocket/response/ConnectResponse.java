package com.redhat.demo.optaplanner.websocket.response;

import com.redhat.demo.optaplanner.websocket.domain.JsonLocation;
import com.redhat.demo.optaplanner.websocket.domain.JsonMechanic;

public class ConnectResponse extends AbstractResponse {

    private JsonLocation[] locations;
    private JsonMechanic[] mechanics;

    public ConnectResponse(JsonLocation[] locations, JsonMechanic[] mechanics) {
        super(ResponseType.CONNECT);
        this.locations = locations;
        this.mechanics = mechanics;
    }

    public JsonLocation[] getLocations() {
        return locations;
    }

    public JsonMechanic[] getMechanics() {
        return mechanics;
    }
}
