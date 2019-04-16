package com.redhat.demo.optaplanner.upstream.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class GameConfigDeserializer extends StdDeserializer<GameConfig> {

    public GameConfigDeserializer() {
        this(null);
    }

    public GameConfigDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public GameConfig deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String stateString = node.get("state").asText().toUpperCase();
        return new GameConfig(GameState.valueOf(stateString));
    }
}
