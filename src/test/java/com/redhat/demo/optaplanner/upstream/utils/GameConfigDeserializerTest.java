package com.redhat.demo.optaplanner.upstream.utils;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class GameConfigDeserializerTest {

    @Test(timeout = 10_000L)
    public void deserializeGameConfigJsonString() {
        String gameConfigString1 = "{\"state\":\"active\"}";
        String gameConfigString2 = "{" +
                    "\"id\": \"uuidv4\"," +
                    "\"state\": \"lobby\", " +
                    "\"shakeDemo\": {" +
                        "\"enabled\": true, " +
                        "\"multiplier\": 2, " +
                        "\"maxPerSecond\": 5000" +
                    "}," +
                    "\"motions\": {" +
                    "} " +
                "}";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GameConfig gameConfig1 = objectMapper.readValue(gameConfigString1, GameConfig.class);
            Assertions.assertThat(gameConfig1.getState().equals("active"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not convert " + gameConfigString1 + "to " + GameConfig.class.getName());
        }

        try {
            GameConfig gameConfig2 = objectMapper.readValue(gameConfigString2, GameConfig.class);
            Assertions.assertThat(gameConfig2.getState().equals("lobby"));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not convert " + gameConfigString2 + "to " + GameConfig.class.getName());
        }
    }
}
