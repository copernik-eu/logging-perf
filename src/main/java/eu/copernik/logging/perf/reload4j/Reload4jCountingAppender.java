/*
 * Copyright © 2024 Piotr P. Karwasz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.copernik.logging.perf.reload4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class Reload4jCountingAppender extends AppenderSkeleton {

    private final Map<String, AtomicLong> countersByThread = new ConcurrentHashMap<>();

    @Override
    protected void append(LoggingEvent event) {
        getCounter(event.getThreadName()).addAndGet(layout.format(event).getBytes().length);
    }

    @Override
    public void close() {}

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public AtomicLong getCounter(String threadName) {
        return countersByThread.computeIfAbsent(threadName, ignored -> new AtomicLong());
    }
}
