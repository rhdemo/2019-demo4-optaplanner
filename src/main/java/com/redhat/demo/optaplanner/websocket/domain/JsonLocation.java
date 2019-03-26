package com.redhat.demo.optaplanner.websocket.domain;

public class JsonLocation {
    private int x;
    private int y;

    public JsonLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
