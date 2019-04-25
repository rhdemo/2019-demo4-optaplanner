package com.redhat.demo.optaplanner.websocket.response;

import com.redhat.demo.optaplanner.upstream.utils.OptaPlannerConfig;
import com.redhat.demo.optaplanner.websocket.domain.JsonLocation;
import com.redhat.demo.optaplanner.websocket.domain.JsonMechanic;

public class ConnectResponse extends AbstractResponse {

    private JsonLocation[] locations;
    private JsonMechanic[] mechanics;
    private OptaPlannerConfig optaPlannerConfig;

    public ConnectResponse(JsonLocation[] locations, JsonMechanic[] mechanics, OptaPlannerConfig optaPlannerConfig) {
        super(ResponseType.CONNECT);
        this.locations = locations;
        this.mechanics = mechanics;
        this.optaPlannerConfig = optaPlannerConfig;
    }

    public JsonLocation[] getLocations() {
        return locations;
    }

    public JsonMechanic[] getMechanics() {
        return mechanics;
    }

    public OptaPlannerConfig getOptaPlannerConfig() {
        return optaPlannerConfig;
    }
}
