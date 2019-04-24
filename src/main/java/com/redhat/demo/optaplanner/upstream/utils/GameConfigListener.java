package com.redhat.demo.optaplanner.upstream.utils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demo.optaplanner.GameService;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryModified;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCustomEvent;
import org.infinispan.commons.util.KeyValueWithPrevious;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ClientListener(converterFactoryName = "key-value-with-previous-converter-factory")
public class GameConfigListener {

    public static final Logger logger = LoggerFactory.getLogger(GameConfigListener.class);

    public final String KEY_VALUE_WITH_PREVIOUS_REGEX =
            "KeyValueWithPrevious\\{key=(?<key>[\\w]+), " +
                    "value=(?<value>\\{[([^\\{\\}]*)(\\{[^\\{\\}]*\\})]*\\}), " +
                    "prev=.*\\}";

    private GameService gameService;
    private Pattern keyValueWithPreviousPattern;
    private ObjectMapper objectMapper;

    @Autowired
    public GameConfigListener(GameService gameService) {
        this.gameService = gameService;
        keyValueWithPreviousPattern = Pattern.compile(KEY_VALUE_WITH_PREVIOUS_REGEX);
        objectMapper = new ObjectMapper();
    }

    @ClientCacheEntryCreated
    @ClientCacheEntryModified
    public void handleGameConfigChange(ClientCacheEntryCustomEvent<KeyValueWithPrevious<String, String>> event) {
        // String.valueOf(KeyValueWithPrevious) is a workaround ClassCastException (String cannot be cast to KeyValueWithPrevious
        //     Because of the use of StringMarshaller, the event comes in as a String and Infinispan cannot deconstruct key and value
        logger.info(String.valueOf(event.getEventData()));
        String key = extractKeyValueWithPreviousGroup(String.valueOf(event.getEventData()), "key");
        String value = extractKeyValueWithPreviousGroup(String.valueOf(event.getEventData()), "value");
        if (key.equals("game")) {
            try {
                GameConfig gameConfig = objectMapper.readValue(value, GameConfig.class);
                GameState state = gameConfig.getState();
                logger.info("Game state is " + state);
                if (state == GameState.LOBBY) {
                    logger.info("resetting...");
//                    gameService.reset(); // TODO FIXME (Musa asked to comment this out for the load test)
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not convert " + value + "to " + GameConfig.class.getName());
            }
        }
    }

    private String extractKeyValueWithPreviousGroup(String keyValueWithPreviousString, String groupName) {
        Matcher matcher = keyValueWithPreviousPattern.matcher(keyValueWithPreviousString);
        return matcher.matches() ? matcher.group(groupName) : "";
    }
}
