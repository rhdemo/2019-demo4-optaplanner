package com.redhat.demo.optaplanner;

import java.util.List;

public interface GameService {
     void pauseGame();

     void resumeGame();

     boolean isGameRunning();

     void addMechanic();

     void removeMechanic();

     List<Mechanic> getMechanics();

     Machine[] getMachines();
}
