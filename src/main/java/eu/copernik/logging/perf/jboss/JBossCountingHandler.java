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
package eu.copernik.logging.perf.jboss;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Formatter;
import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

public class JBossCountingHandler extends ExtHandler {

    private final Map<String, AtomicLong> countersByThread = new ConcurrentHashMap<>();

    @Override
    protected void doPublish(ExtLogRecord record) {
        final Formatter formatter = getFormatter();
        final String formatted = formatter.format(record);
        getCounter(record.getThreadName()).addAndGet(formatted.getBytes().length);
    }

    public AtomicLong getCounter(String threadName) {
        return countersByThread.computeIfAbsent(threadName, ignored -> new AtomicLong());
    }
}
