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

package com.redhat.demo.optaplanner.simulation;

import java.util.Random;

/**
 * How the damage is distributed across machines
 */
public enum DamageDistributionType {
    /**
     * With 0.20 total damage per second across 10 machines,
     * each machine receives 0.02 damage per second on average.
     */
    UNIFORM,
    /**
     * With 0.20 total damage per second across 10 machines,
     * some machines will have more damage per second on average than others.
     * <p>
     * Using {@link Random#nextGaussian()} in order to create a bell curve.
     */
    GAUSS,
    /**
     * With 0.24 total damage per second across 10 machines,
     * each machine receives 0.02 damage per second on average, except for machine C and J who receive 0.04.
     */
    DOUBLE_DISTRIBUTION_ON_MACHINE_C_AND_J
}
