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
package eu.copernik.logging.perf.log4j;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.plugins.Configurable;
import org.apache.logging.log4j.plugins.Factory;
import org.apache.logging.log4j.plugins.Plugin;
import org.apache.logging.log4j.plugins.PluginAttribute;
import org.apache.logging.log4j.plugins.PluginElement;
import org.apache.logging.log4j.plugins.validation.constraints.Required;

@Plugin("Counting")
@Configurable
public final class Log4jCountingAppender extends AbstractAppender implements ByteBufferDestination {

    @Factory
    public static Log4jCountingAppender create(
            @PluginAttribute("name") @Required String name, @PluginElement("layout") @Required Layout layout) {
        return new Log4jCountingAppender(name, layout);
    }

    private final ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.ENCODER_BYTE_BUFFER_SIZE);
    private final Map<String, AtomicLong> countersByThread = new ConcurrentHashMap<>();
    private volatile AtomicLong counter;

    private Log4jCountingAppender(String name, Layout layout) {
        super(name, null, layout, true, null);
    }

    @Override
    public void append(LogEvent event) {
        counter = getCounter(event.getThreadName());
        if (Constants.ENABLE_DIRECT_ENCODERS) {
            getLayout().encode(event, this);
            drain(byteBuffer);
        } else {
            counter.addAndGet(getLayout().toByteArray(event).length);
        }
        counter = null;
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    @Override
    public ByteBuffer drain(ByteBuffer buf) {
        buf.flip();
        counter.addAndGet(buf.remaining());
        buf.clear();
        return buf;
    }

    @Override
    public void writeBytes(ByteBuffer data) {
        counter.addAndGet(data.remaining());
    }

    @Override
    public void writeBytes(byte[] data, int offset, int length) {
        counter.addAndGet(length);
    }

    public AtomicLong getCounter(String threadName) {
        return countersByThread.computeIfAbsent(threadName, ignored -> new AtomicLong());
    }
}
