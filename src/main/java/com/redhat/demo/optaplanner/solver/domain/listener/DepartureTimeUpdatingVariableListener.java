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

package com.redhat.demo.optaplanner.solver.domain.listener;

import java.util.Objects;

import com.redhat.demo.optaplanner.solver.domain.OptaVisit;
import com.redhat.demo.optaplanner.solver.domain.OptaVisitOrMechanic;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class DepartureTimeUpdatingVariableListener implements VariableListener<OptaVisit> {

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, OptaVisit visit) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, OptaVisit visit) {
        updateDepartureTime(scoreDirector, visit);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, OptaVisit visit) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, OptaVisit visit) {
        updateDepartureTime(scoreDirector, visit);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, OptaVisit visit) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, OptaVisit visit) {
        // Do nothing
    }

    private void updateDepartureTime(ScoreDirector scoreDirector, OptaVisit sourceVisit) {
        OptaVisitOrMechanic previous = sourceVisit.getPrevious();
        Long previousDepartureTimeMillis = (previous == null) ? null
                : previous.getDepartureTimeMillis();
        OptaVisit shadowVisit = sourceVisit;
        Long departureTimeMillis = (previousDepartureTimeMillis == null) ? null
                : previousDepartureTimeMillis + shadowVisit.getTravelTimeMillisFromPrevious() + OptaVisit.SERVICE_TIME_MILLIS;
        while (shadowVisit != null && !Objects.equals(shadowVisit.getDepartureTimeMillis(), departureTimeMillis)) {
            scoreDirector.beforeVariableChanged(shadowVisit, "departureTimeMillis");
            shadowVisit.setDepartureTimeMillis(departureTimeMillis);
            scoreDirector.afterVariableChanged(shadowVisit, "departureTimeMillis");
            shadowVisit = shadowVisit.getNext();
            departureTimeMillis = (departureTimeMillis == null || shadowVisit == null) ? null
                    : departureTimeMillis + shadowVisit.getTravelTimeMillisFromPrevious() + OptaVisit.SERVICE_TIME_MILLIS;
        }
    }

}
