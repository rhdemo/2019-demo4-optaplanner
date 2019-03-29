/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.redhat.demo.optaplanner.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.springframework.stereotype.Service;

@Service
public class AppConfiguration {

    public static final long TIME_TICK_MILLIS = 40L;
    public static final long OPTA_MACHINE_HEALTH_REFRESH_RATE = TIME_TICK_MILLIS * 15L;

    private int machinesAndGateLength;
    private int initialMechanicsSize = 1;
    private double mechanicSpeed = 0.1;
    private long fixDurationMillis = 2000L;
    private long thumbUpDurationMillis = 200L;

    private int[] machineIndexToGridX;
    private int[] machineIndexToGridY;
    private double[][] travelDistanceMatrix;

    private double manualDamageValue = 0.30;

    @PostConstruct
    public void readTravelDistanceMatrix() {
        List<String> lines;
        try {
            lines = new ArrayList<>(Arrays.asList(
                    Resources.toString(getClass().getResource("/machineTravelDistanceMatrix.csv"), Charsets.UTF_8)
                            .split("\n")));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find machineTravelDistanceMatrix.csv.", e);
        }
        String firstLine = lines.remove(0);
        if (!firstLine.startsWith("machine name, x, y,")) {
            throw new IllegalStateException(
                    "The machineTravelDistanceMatrix.csv is corrupted on line (" + firstLine + ").");
        }
        machinesAndGateLength = firstLine.split(",").length - 3;
        machineIndexToGridX = new int[machinesAndGateLength];
        machineIndexToGridY = new int[machinesAndGateLength];
        travelDistanceMatrix = new double[machinesAndGateLength][machinesAndGateLength];
        if (lines.size() != machinesAndGateLength) {
            throw new IllegalStateException(
                    "The machineTravelDistanceMatrix.csv is corrupted.");
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] tokens = line.split(",\\s*");
            if (tokens.length != machinesAndGateLength + 3) {
                throw new IllegalStateException(
                        "The machineTravelDistanceMatrix.csv is corrupted on line (" + line + ").");
            }
            boolean gate = isGate(i);
            if (tokens[0].equals("gate")) {
                if (!gate) {
                    throw new IllegalStateException(
                            "The machineTravelDistanceMatrix.csv is corrupted on line (" + line + ").");
                }
            } else if (tokens[0].startsWith("machine-")) {
                if (gate) {
                    throw new IllegalStateException(
                            "The machineTravelDistanceMatrix.csv is corrupted on line (" + line + ").");
                }
            } else {
                throw new IllegalStateException(
                        "The machineTravelDistanceMatrix.csv is corrupted on line (" + line + ").");
            }
            machineIndexToGridX[i] = Integer.parseInt(tokens[1]);
            machineIndexToGridY[i] = Integer.parseInt(tokens[2]);
            for (int j = 0; j < machinesAndGateLength; j++) {
                travelDistanceMatrix[i][j] = Double.parseDouble(tokens[j + 3]);
            }
        }
    }

    public int getMachineGridX(int machineIndex) {
        return machineIndexToGridX[machineIndex];
    }

    public int getMachineGridY(int machineIndex) {
        return machineIndexToGridY[machineIndex];
    }

    public int getMachinesAndGateLength() {
        return machinesAndGateLength;
    }

    public int getMachinesOnlyLength() {
        return machinesAndGateLength - 1;
    }

    public int getGateMachineIndex() {
        return getMachinesOnlyLength();
    }

    public boolean isGate(int machineIndex) {
        return machineIndex >= getMachinesOnlyLength();
    }

    public double[] getMachineIndexToTravelDistances(int machineIndex) {
        return travelDistanceMatrix[machineIndex];
    }

    public int getInitialMechanicsSize() {
        return initialMechanicsSize;
    }

    public double getMechanicSpeed() {
        return mechanicSpeed;
    }

    public long getFixDurationMillis() {
        return fixDurationMillis;
    }

    public long getThumbUpDurationMillis() {
        return thumbUpDurationMillis;
    }

    public double getManualDamageValue() {
        return manualDamageValue;
    }
}
