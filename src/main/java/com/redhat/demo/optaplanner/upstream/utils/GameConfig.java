package com.redhat.demo.optaplanner.upstream.utils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = GameConfigDeserializer.class)
public class GameConfig {

    private String state;

    public GameConfig() {
    }

    public GameConfig(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
