package eu.copernik.logging.perf.reload4j;

import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class Reload4jCountingAppender extends AppenderSkeleton {

    private final AtomicLong byteCounter = new AtomicLong();

    @Override
    protected void append(LoggingEvent event) {
        byteCounter.addAndGet(layout.format(event).getBytes().length);
    }

    @Override
    public void close() {}

    @Override
    public boolean requiresLayout() {
        return true;
    }

    public long resetByteCount() {
        return byteCounter.getAndSet(0);
    }
}
