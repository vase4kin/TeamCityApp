/*
 * Copyright (C) 2016 Azimo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azimolabs.conditionwatcher;

/**
 * Polls an {@link Instruction} until it succeeds or reaches its timeout.
 *
 * <p>Vendored from the Apache-licensed AzimoLabs ConditionWatcher project because its published
 * artifact is no longer available from the repositories used by this project.</p>
 */
public final class ConditionWatcher {

    private static final int DEFAULT_TIMEOUT_LIMIT = 60_000;
    private static final int DEFAULT_INTERVAL = 250;

    private static int timeoutLimit = DEFAULT_TIMEOUT_LIMIT;
    private static int watchInterval = DEFAULT_INTERVAL;

    private ConditionWatcher() {
    }

    public static void waitForCondition(Instruction instruction) throws Exception {
        waitForCondition(instruction, timeoutLimit, watchInterval);
    }

    public static void waitForCondition(Instruction instruction, int timeoutLimit) throws Exception {
        waitForCondition(instruction, timeoutLimit, watchInterval);
    }

    public static void waitForCondition(
            Instruction instruction,
            int timeoutLimit,
            int watchInterval
    ) throws Exception {
        long deadline = System.currentTimeMillis() + timeoutLimit;

        while (!instruction.checkCondition()) {
            if (System.currentTimeMillis() >= deadline) {
                throw new Exception(
                        instruction.getDescription()
                                + " - took more than "
                                + timeoutLimit / 1000
                                + " seconds. Test stopped."
                );
            }
            Thread.sleep(watchInterval);
        }
    }

    public static void setWatchInterval(int watchInterval) {
        ConditionWatcher.watchInterval = watchInterval;
    }

    public static void setTimeoutLimit(int timeoutLimit) {
        ConditionWatcher.timeoutLimit = timeoutLimit;
    }
}
