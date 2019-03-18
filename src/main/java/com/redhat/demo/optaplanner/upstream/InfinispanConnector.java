package com.redhat.demo.optaplanner.upstream;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCounterManagerFactory;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.StrongCounter;
import org.springframework.stereotype.Component;

@Component
public class InfinispanConnector implements UpstreamConnector {

    private static final long FULL_HEALTH = 1_000_000_000_000_000_000L;

    private StrongCounter[] counters = new StrongCounter[MACHINES_LENGTH];
    private Map<StrongCounter, Integer> counterIndices = new HashMap<>(MACHINES_LENGTH);

    public InfinispanConnector() {
        Configuration configuration = HotRodClientConfiguration.get().build();
        RemoteCacheManager remoteCacheManager = new RemoteCacheManager(configuration);
        CounterManager counterManager = RemoteCounterManagerFactory.asCounterManager(remoteCacheManager);
        for (int i = 0; i < MACHINES_LENGTH; i++) {
            StrongCounter currentCounter = counterManager.getStrongCounter(String.format("machine-%d", i + 1));
            counters[i] = currentCounter;
            counterIndices.put(currentCounter, i);
        }
    }

    @Override
    public double[] fetchMachineHealths() {
        return Arrays.stream(counters)
                .mapToLong(strongCounter -> {
                    try {
                        return counters[counterIndices.get(strongCounter)].getValue().get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
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
    public void damageMachine(int machineIndex, double damage) {
        long damageLong = (long) (damage * FULL_HEALTH);
        counters[machineIndex].addAndGet(-damageLong);
    }
}
