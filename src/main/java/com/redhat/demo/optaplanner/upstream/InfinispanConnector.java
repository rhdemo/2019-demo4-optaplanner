package com.redhat.demo.optaplanner.upstream;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.demo.optaplanner.Mechanic;
import com.redhat.demo.optaplanner.SpringProfiles;
import com.redhat.demo.optaplanner.config.AppConfiguration;
import com.redhat.demo.optaplanner.upstream.utils.GameConfigListener;
import com.redhat.demo.optaplanner.upstream.utils.OptaPlannerConfig;
import com.redhat.demo.optaplanner.websocket.response.FutureVisitsResponse;
import com.redhat.demo.optaplanner.websocket.domain.JsonMechanic;
import com.redhat.demo.optaplanner.websocket.response.AddMechanicResponse;
import com.redhat.demo.optaplanner.websocket.response.DispatchMechanicResponse;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCounterManagerFactory;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.StrongCounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(SpringProfiles.PRODUCTION)
public class InfinispanConnector implements UpstreamConnector {

    private static final long FULL_HEALTH = 1_000_000_000_000_000_000L;
    private static final String DISPATCH_MECHANIC_EVENTS_CACHE_NAME = "DispatchEvents";
    private static final String DEFAULT_CACHE_NAME = "default";
    public static final String OPTA_PLANNER_CONFIG_KEY_NAME = "OptaPlannerConfig";

    @Autowired
    private AppConfiguration appConfiguration;
    @Autowired
    GameConfigListener gameConfigListener;

    private StrongCounter[] counters;
    private Map<StrongCounter, Integer> counterIndices;
    private RemoteCache<String, String> dispatchMechanicEventsCache;
    private RemoteCache<String, String> defaultCache;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void postConstruct() {
        counters = new StrongCounter[appConfiguration.getMachinesOnlyLength()];
        counterIndices = new HashMap<>(counters.length);
        Configuration configuration = HotRodClientConfiguration.get().build();
        RemoteCacheManager remoteCacheManager = new RemoteCacheManager(configuration);
        CounterManager counterManager = RemoteCounterManagerFactory.asCounterManager(remoteCacheManager);
        for (int i = 0; i < counters.length; i++) {
            StrongCounter currentCounter = counterManager.getStrongCounter(String.format("machine-%d", i));
            counters[i] = currentCounter;
            counterIndices.put(currentCounter, i);
        }
        dispatchMechanicEventsCache = remoteCacheManager.getCache(DISPATCH_MECHANIC_EVENTS_CACHE_NAME);
        defaultCache = remoteCacheManager.getCache(DEFAULT_CACHE_NAME);
        defaultCache.addClientListener(gameConfigListener);
        objectMapper = new ObjectMapper();

        OptaPlannerConfig defaultConfig = new OptaPlannerConfig(false, false);
        defaultCache.put(OPTA_PLANNER_CONFIG_KEY_NAME, convertToJsonString(defaultConfig));
    }

    @Override
    public double[] fetchMachineHealths() {
        return Arrays.stream(counters)
                .mapToLong(strongCounter -> {
                    int counterIndex = counterIndices.get(strongCounter);
                    try {
                        return counters[counterIndex].getValue().get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Connector thread was interrupted while getting counter value.", e);
                    } catch (ExecutionException e) {
                        throw new IllegalStateException("Couldn't find StringCounter (" + counterIndex + ").", e.getCause());
                    }
                })
                .mapToDouble(machineHealthLong -> ((double) machineHealthLong) / ((double) FULL_HEALTH))
                .toArray();
    }

    @Override
    public void resetMachineHealth(int machineIndex) {
        counters[machineIndex].reset();
    }

    @Override
    public void dispatchMechanic(Mechanic mechanic, long currentTimeMillis) {
        JsonMechanic jsonMechanic = new JsonMechanic(mechanic, currentTimeMillis);
        DispatchMechanicResponse dispatchMechanicResponse = new DispatchMechanicResponse(jsonMechanic);
        dispatchMechanicEventsCache.put(String.valueOf(jsonMechanic.getMechanicIndex()), convertToJsonString(dispatchMechanicResponse));
    }

    @Override
    public void mechanicAdded(Mechanic mechanic, long currentTimeMillis) {
        JsonMechanic jsonMechanic = new JsonMechanic(mechanic, currentTimeMillis);
        AddMechanicResponse addMechanicResponse = new AddMechanicResponse(jsonMechanic);
        dispatchMechanicEventsCache.put(String.valueOf(mechanic.getMechanicIndex()), convertToJsonString(addMechanicResponse));
    }

    @Override
    public void mechanicRemoved(Mechanic mechanic) {
        dispatchMechanicEventsCache.remove(String.valueOf(mechanic.getMechanicIndex()));
        dispatchMechanicEventsCache.remove(String.format("%d-futureIndexes", mechanic.getMechanicIndex()));
    }

    @Override
    public void damageMachine(int machineIndex, double damage) {
        long damageLong = (long) (damage * FULL_HEALTH);
        counters[machineIndex].addAndGet(-damageLong);
    }

    @Override
    public void sendFutureVisits(int mechanicIndex, int [] futureMachineIndexes) {
        FutureVisitsResponse futureVisitsResponse = new FutureVisitsResponse(mechanicIndex, futureMachineIndexes);
        dispatchMechanicEventsCache.put(String.format("%d-futureIndexes", mechanicIndex), convertToJsonString(futureVisitsResponse));
    }

    @Override
    public synchronized void setDispatchStatus(boolean isDispatchActive) {
        OptaPlannerConfig config = convertFromJsonString(defaultCache.get(OPTA_PLANNER_CONFIG_KEY_NAME), OptaPlannerConfig.class);
        config.setDispatchActive(isDispatchActive);
        defaultCache.put(OPTA_PLANNER_CONFIG_KEY_NAME, convertToJsonString(config));
    }

    @Override
    public synchronized void setSimulationStatus(boolean isSimulationActive) {
        OptaPlannerConfig config = convertFromJsonString(defaultCache.get(OPTA_PLANNER_CONFIG_KEY_NAME), OptaPlannerConfig.class);
        config.setSimulationActive(isSimulationActive);
        defaultCache.put(OPTA_PLANNER_CONFIG_KEY_NAME, convertToJsonString(config));
    }

    private String convertToJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not format " + object.getClass().getName() + " as json.", e);
        }
    }

    private <T> T convertFromJsonString(String json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not convert " + json + "to " + tClass.getName());
        }
    }
}
