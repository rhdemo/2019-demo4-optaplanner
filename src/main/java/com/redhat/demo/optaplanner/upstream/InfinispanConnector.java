package com.redhat.demo.optaplanner.upstream;

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

    @Autowired
    private AppConfiguration appConfiguration;

    private StrongCounter[] counters;
    private Map<StrongCounter, Integer> counterIndices;
    private RemoteCache<String, String> dispatchMechanicEventsCache;
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
        objectMapper = new ObjectMapper();
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
        try {
            String jsonDispatchMechanicResponse = objectMapper.writeValueAsString(dispatchMechanicResponse);
            dispatchMechanicEventsCache.put(String.valueOf(jsonMechanic.getMechanicIndex()), jsonDispatchMechanicResponse);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not format mechanic (" + mechanic.getMechanicIndex() + ") as json.", e);
        }
    }

    @Override
    public void mechanicAdded(Mechanic mechanic, long currentTimeMillis) {
        JsonMechanic jsonMechanic = new JsonMechanic(mechanic, currentTimeMillis);
        AddMechanicResponse addMechanicResponse = new AddMechanicResponse(jsonMechanic);
        try {
            String jsonAddMechanicResponse = objectMapper.writeValueAsString(addMechanicResponse);
            dispatchMechanicEventsCache.put(String.valueOf(mechanic.getMechanicIndex()), jsonAddMechanicResponse);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not format mechanic (" + mechanic.getMechanicIndex() + ") as json.", e);
        }
    }

    @Override
    public void mechanicRemoved(Mechanic mechanic) {
        dispatchMechanicEventsCache.remove(String.valueOf(mechanic.getMechanicIndex()));
    }

    @Override
    public void damageMachine(int machineIndex, double damage) {
        long damageLong = (long) (damage * FULL_HEALTH);
        counters[machineIndex].addAndGet(-damageLong);
    }

    @Override
    public void sendFutureVisits(int mechanicIndex, int [] futureMachineIndexes) {
        FutureVisitsResponse futureVisitsResponse = new FutureVisitsResponse(mechanicIndex, futureMachineIndexes);
        try {
            String jsonFutureVisitsResponse = objectMapper.writeValueAsString(futureVisitsResponse);
            dispatchMechanicEventsCache.put(String.format("%d-futureIndexes", mechanicIndex), jsonFutureVisitsResponse);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not format futureVisitsResponse of mechanic (" + mechanicIndex + ") as json.", e);
        }
    }
}
