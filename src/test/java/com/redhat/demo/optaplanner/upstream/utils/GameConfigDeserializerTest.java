package com.redhat.demo.optaplanner.upstream.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GameConfigDeserializerTest {

    private String gameConfigString;
    private GameState expectedGameState;

    public GameConfigDeserializerTest(String gameConfigString, GameState expectedGameState) {
        this.gameConfigString = gameConfigString;
        this.expectedGameState = expectedGameState;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"{\"state\":\"active\"}", GameState.ACTIVE},
                {"{" +
                    "\"id\": \"uuidv4\"," +
                    "\"state\": \"lobby\", " +
                    "\"shakeDemo\": {" +
                        "\"enabled\": true, " +
                        "\"multiplier\": 2, " +
                        "\"maxPerSecond\": 5000" +
                    "}," +
                    "\"motions\": {" +
                    "} " +
                "}", GameState.LOBBY}});
    }

    @Test(timeout = 10_000L)
    public void deserializeGameConfigJsonString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GameConfig gameConfig = objectMapper.readValue(gameConfigString, GameConfig.class);
            Assertions.assertThat(gameConfig.getState()).isEqualTo(expectedGameState);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not convert " + gameConfigString + "to " + GameConfig.class.getName());
        }
    }
}
