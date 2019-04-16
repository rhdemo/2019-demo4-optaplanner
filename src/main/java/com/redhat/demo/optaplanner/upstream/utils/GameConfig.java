package com.redhat.demo.optaplanner.upstream.utils;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = GameConfigDeserializer.class)
public class GameConfig {

    private GameState state;

    public GameConfig() {
    }

    public GameConfig(GameState state) {
        this.state = state;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }
}
