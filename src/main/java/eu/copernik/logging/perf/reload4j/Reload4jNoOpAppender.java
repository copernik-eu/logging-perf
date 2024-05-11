package eu.copernik.logging.perf.reload4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class Reload4jNoOpAppender extends AppenderSkeleton {
    @Override
    protected void append(LoggingEvent event) {}

    @Override
    public void close() {}

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
