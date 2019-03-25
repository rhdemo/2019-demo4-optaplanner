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

package com.redhat.demo.optaplanner.tools;

import java.util.Random;

import com.redhat.demo.optaplanner.AppConstants;

/**
 * Helper class until we have have real road travel times from UX image.
 */
public class TravelTimeGenerator {

    private static final int MAX_X = 800; // Maximum pixels to cross horizontally
    private static final int MAX_Y = 600; // Maximum pixels seconds to cross vertically

    public static void main(String[] args) {
        Random random = new Random(37);
        int[][] machineIndexToXYPairs = new int[AppConstants.MACHINES_LENGTH + 1][2];
        for (int i = 0; i < machineIndexToXYPairs.length; i++) {
            machineIndexToXYPairs[i][0] = random.nextInt(MAX_X);
            machineIndexToXYPairs[i][1] = random.nextInt(MAX_Y);
        }
        double[][] travelDistanceMatrix = new double[machineIndexToXYPairs.length][machineIndexToXYPairs.length];
        for (int i = 0; i < machineIndexToXYPairs.length; i++) {
            int fromX =  machineIndexToXYPairs[i][0];
            int fromY =  machineIndexToXYPairs[i][1];
            for (int j = 0; j < machineIndexToXYPairs.length; j++) {
                int toX =  machineIndexToXYPairs[j][0];
                int toY =  machineIndexToXYPairs[j][1];

                // Euclidean distance (Pythagorean theorem)
                double xDifference = toX - fromX;
                double yDifference = toY - fromY;
                travelDistanceMatrix[i][j]  = Math.sqrt((xDifference * xDifference) + (yDifference * yDifference));
            }
        }
        System.out.print("machine name, x, y");
        for (int i = 0; i < machineIndexToXYPairs.length; i++) {
            if (i != AppConstants.MACHINES_LENGTH) {
                System.out.print(", machine-");
                System.out.print(i + 1);
            } else {
                System.out.print(", gate");
            }
        }
        System.out.println();

        for (int i = 0; i < travelDistanceMatrix.length; i++) {
            if (i != AppConstants.MACHINES_LENGTH) {
                System.out.print("machine-");
                System.out.print(i + 1);
            } else {
                System.out.print("gate");
            }
            System.out.print(", ");
            System.out.print(machineIndexToXYPairs[i][0]);
            System.out.print(", ");
            System.out.print(machineIndexToXYPairs[i][1]);
            for (int j = 0; j < travelDistanceMatrix[i].length; j++) {
                System.out.print(", ");
                System.out.print(travelDistanceMatrix[i][j]);
            }
            System.out.println();
        }
    }

}
