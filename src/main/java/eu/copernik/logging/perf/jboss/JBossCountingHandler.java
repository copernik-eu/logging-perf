package eu.copernik.logging.perf.jboss;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Formatter;
import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

public class JBossCountingHandler extends ExtHandler {

    private final AtomicLong byteCounter = new AtomicLong();

    @Override
    protected void doPublish(ExtLogRecord record) {
        final Formatter formatter = getFormatter();
        final String formatted = formatter.format(record);
        byteCounter.addAndGet(formatted.getBytes().length);
    }

    public long resetByteCount() {
        return byteCounter.getAndSet(0);
    }
}
