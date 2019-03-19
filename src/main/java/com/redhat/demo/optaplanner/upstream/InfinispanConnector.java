package com.redhat.demo.optaplanner.upstream;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.redhat.demo.optaplanner.SpringProfiles;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCounterManagerFactory;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.StrongCounter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile(SpringProfiles.PRODUCTION)
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
    public void damageMachine(int machineIndex, double damage) {
        long damageLong = (long) (damage * FULL_HEALTH);
        counters[machineIndex].addAndGet(-damageLong);
    }
}
