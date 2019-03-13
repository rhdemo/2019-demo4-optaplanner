package com.redhat.demo.optaplanner.upstream;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCounterManagerFactory;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.StrongCounter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/* TODO
    - make array machineHealths thread safe
    - get hot-rod connection properties from hotrod-client.properties
 */

@Component
public class InfinispanConnector implements UpstreamConnector {

    private static final long FULL_HEALTH = 1_000_000_000_000_000_000L;
    private static final Random random = new Random();

    private StrongCounter[] counters = new StrongCounter[MACHINES_LENGTH];
    private double[] machineHealths = new double[MACHINES_LENGTH]; // Store counter values to be readily available without blocking
    private Map<StrongCounter, Integer> counterIndices = new HashMap<>(MACHINES_LENGTH);

    public InfinispanConnector() {
        Configuration configuration = HotRodClientConfiguration.get().build();
        RemoteCacheManager remoteCacheManager = new RemoteCacheManager(configuration);
        CounterManager counterManager = RemoteCounterManagerFactory.asCounterManager(remoteCacheManager);
        for (int i = 0; i < MACHINES_LENGTH; i++) {
            StrongCounter currentCounter = counterManager.getStrongCounter(String.format("machine-%d", i+1));
            counters[i] = currentCounter;
            counterIndices.put(currentCounter, i);
        }
        random.setSeed(13);
    }

    @Scheduled(fixedRate = 40)
    private void updateMachineHealths() {
        Arrays.stream(counters)
                .forEach(counter ->
                        counter.getValue().thenAccept(machineHealth -> {
                            machineHealths[counterIndices.get(counter)] = ((double) machineHealth) / FULL_HEALTH;
                        })
                );
    }

    /* Test functionality
    @Scheduled(fixedRate = 2000)
    private void fixRandomMachine() {
        int index = random.nextInt(20);
        System.out.println("InfinispanConnector.fixMachine: " + index);
        resetMachineHealth(index);
    }

    @Scheduled(fixedRate = 2000)
    private void damageRandomMachine() {
        int index = random.nextInt(20);
        double damage = random.nextDouble();
        System.out.println("InfinispanConnector.damageRandomMachine: " + index + "by " + damage + ".");
        damageMachine(index, damage);
    }
    */

    @Override
    public double[] fetchMachineHealths() {
        return machineHealths.clone();
    }

    @Override
    public void resetMachineHealth(int machineIndex) {
        counters[machineIndex]
                .reset()
                .thenRun(() -> machineHealths[machineIndex] = 1.0);
    }

    @Override
    public void damageMachine(int machineIndex, double damage) {
        long damageLong = (long) (damage * FULL_HEALTH);
        counters[machineIndex].addAndGet(-damageLong);
    }
}
