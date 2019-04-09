package com.redhat.demo.optaplanner.websocket.response;

import com.redhat.demo.optaplanner.websocket.response.AbstractResponse;
import com.redhat.demo.optaplanner.websocket.response.ResponseType;

public class FutureVisitsResponse extends AbstractResponse {

    private int mechanicIndex;
    private int[] futureMachineIndexes;

    public FutureVisitsResponse(int mechanicIndex, int[] futureMachineIndexes) {
        super(ResponseType.UPDATE_FUTURE_VISITS);
        this.mechanicIndex = mechanicIndex;
        this.futureMachineIndexes = futureMachineIndexes;
    }

    public int getMechanicIndex() {
        return mechanicIndex;
    }

    public int[] getFutureMachineIndexes() {
        return futureMachineIndexes;
    }
}
