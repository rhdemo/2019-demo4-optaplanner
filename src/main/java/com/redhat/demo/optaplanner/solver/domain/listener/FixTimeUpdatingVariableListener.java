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

import com.redhat.demo.optaplanner.solver.domain.OptaMechanic;
import com.redhat.demo.optaplanner.solver.domain.OptaVisit;
import com.redhat.demo.optaplanner.solver.domain.OptaVisitOrMechanic;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

public class FixTimeUpdatingVariableListener implements VariableListener<OptaVisit> {

    @Override
    public void beforeEntityAdded(ScoreDirector scoreDirector, OptaVisit visit) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector scoreDirector, OptaVisit visit) {
        updateFixTime(scoreDirector, visit);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector scoreDirector, OptaVisit visit) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector scoreDirector, OptaVisit visit) {
        updateFixTime(scoreDirector, visit);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector scoreDirector, OptaVisit visit) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector scoreDirector, OptaVisit visit) {
        // Do nothing
    }

    private void updateFixTime(ScoreDirector scoreDirector, OptaVisit sourceVisit) {
        OptaMechanic mechanic = sourceVisit.getMechanic();
        OptaVisitOrMechanic previous = sourceVisit.getPrevious();
        Long previousFixTimeMillis = (previous == null) ? null : previous.getFixTimeMillis();
        OptaVisit shadowVisit = sourceVisit;
        Long fixTimeMillis = (previousFixTimeMillis == null) ? null
                : previousFixTimeMillis + mechanic.getThumbUpDurationMillis()
                + shadowVisit.getTravelTimeMillisFromPrevious() + mechanic.getFixDurationMillis();
        while (shadowVisit != null && !Objects.equals(shadowVisit.getFixTimeMillis(), fixTimeMillis)) {
            scoreDirector.beforeVariableChanged(shadowVisit, "fixTimeMillis");
            shadowVisit.setFixTimeMillis(fixTimeMillis);
            scoreDirector.afterVariableChanged(shadowVisit, "fixTimeMillis");
            shadowVisit = shadowVisit.getNext();
            fixTimeMillis = (fixTimeMillis == null || shadowVisit == null) ? null
                    : fixTimeMillis + mechanic.getThumbUpDurationMillis()
                    + shadowVisit.getTravelTimeMillisFromPrevious() + mechanic.getFixDurationMillis();
        }
    }

}
