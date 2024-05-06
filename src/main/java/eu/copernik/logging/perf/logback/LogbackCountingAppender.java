package eu.copernik.logging.perf.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import java.util.concurrent.atomic.AtomicLong;

public class LogbackCountingAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private Encoder<ILoggingEvent> encoder;
    private final AtomicLong byteCounter = new AtomicLong();

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }
        if (event instanceof DeferredProcessingAware) {
            ((DeferredProcessingAware) event).prepareForDeferredProcessing();
        }
        byteCounter.addAndGet(encoder.encode(event).length);
    }

    public long resetByteCount() {
        return byteCounter.getAndSet(0);
    }
}
