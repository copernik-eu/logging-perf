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
package eu.copernik.logging.perf.jmh;

import eu.copernik.logging.perf.jboss.JBossCountingHandler;
import eu.copernik.logging.perf.log4j.Log4jCountingAppender;
import eu.copernik.logging.perf.logback.LogbackCountingAppender;
import eu.copernik.logging.perf.reload4j.Reload4jCountingAppender;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.configuration.PropertyLogContextConfigurator;
import org.jboss.logmanager.handlers.AsyncHandler;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.BenchmarkParams;

@State(Scope.Thread)
public class AsyncCountingAppenderBenchmark {

    private static final String MESSAGE = "This is a `DEBUG` message.";
    private static final String FQCN = "eu.copernik.logging.perf.jmh.AsyncCountingAppenderBenchmark";
    private static final double MEBI = 1024 * 1024;
    private org.jboss.logging.Logger jbossLogger;
    private org.apache.logging.log4j.Logger log4jLogger;
    private org.apache.log4j.Logger reload4jLogger;
    private org.slf4j.Logger slf4jLogger;

    @Param("4096")
    private int bufferSize;

    @Setup
    public void up(BenchmarkParams params) throws Exception {
        String benchmark = params.getBenchmark();

        // Common
        System.setProperty("log4j2.asyncLoggerRingBufferSize", Integer.toString(bufferSize));

        switch (benchmark) {
            case FQCN + ".jboss":
                try (InputStream inputStream = AsyncCountingAppenderBenchmark.class.getResourceAsStream(
                        "/jboss/AsyncCountingAppenderBenchmark.properties")) {
                    LogContext jbossContext = LogContext.getLogContext();
                    jbossContext.close();
                    new PropertyLogContextConfigurator().configure(jbossContext, inputStream);
                }
                jbossLogger = org.jboss.logging.Logger.getLogger(getClass());
                break;
            case FQCN + ".log4jAsyncAppender":
                System.setProperty(
                        "log4j2.configurationFile", "log4j/AsyncCountingAppenderBenchmark/log4jAsyncAppender.xml");
                log4jLogger = org.apache.logging.log4j.LogManager.getLogger();
                break;
            case FQCN + ".log4jAsyncLogger":
                System.setProperty(
                        "log4j2.configurationFile", "log4j/AsyncCountingAppenderBenchmark/log4jAsyncLogger.xml");
                System.setProperty(
                        "log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
                log4jLogger = org.apache.logging.log4j.LogManager.getLogger();
                break;
            case FQCN + ".logback":
                System.setProperty("logback.configurationFile", "logback/AsyncCountingAppenderBenchmark.xml");
                slf4jLogger = org.slf4j.LoggerFactory.getLogger(getClass());
                break;
            case FQCN + ".reload4j":
                System.setProperty("log4j.configuration", "reload4j/AsyncCountingAppenderBenchmark.xml");
                reload4jLogger = org.apache.log4j.LogManager.getLogger(getClass());
                break;
        }
    }

    @TearDown(Level.Iteration)
    public void printByteCount(BenchmarkParams params) {
        long seconds = params.getMeasurement().getTime().convertTo(TimeUnit.SECONDS);
        switch (params.getBenchmark()) {
            case FQCN + ".jboss":
                AsyncHandler asyncHandler = (AsyncHandler) Logger.getLogger("").getHandlers()[0];
                JBossCountingHandler countingHandler =
                        (JBossCountingHandler) asyncHandler.getHandlers()[0];
                System.err.printf("%4.2f MiB/s,%n", countingHandler.resetByteCount() / MEBI / seconds);
                break;
            case FQCN + ".log4jAsyncAppender":
            case FQCN + ".log4jAsyncLogger":
                Log4jCountingAppender log4jAppender = org.apache.logging.log4j.core.LoggerContext.getContext(false)
                        .getConfiguration()
                        .getAppender("COUNTING");
                System.err.printf("%4.2f MiB/s,%n", log4jAppender.resetByteCount() / MEBI / seconds);
                break;
            case FQCN + ".logback":
                ch.qos.logback.classic.AsyncAppender logbackAsyncAppender =
                        (ch.qos.logback.classic.AsyncAppender) ((ch.qos.logback.classic.Logger)
                                        org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME))
                                .getAppender("ASYNC");
                LogbackCountingAppender logbackCountingAppender =
                        (LogbackCountingAppender) logbackAsyncAppender.getAppender("COUNTING");
                System.err.printf("%4.2f MiB/s,%n", logbackCountingAppender.resetByteCount() / MEBI / seconds);
                break;
            case FQCN + ".reload4j":
                org.apache.log4j.AsyncAppender reload4jAsyncAppender = (org.apache.log4j.AsyncAppender)
                        org.apache.log4j.Logger.getRootLogger().getAppender("ASYNC");
                Reload4jCountingAppender reload4jCountingAppender =
                        (Reload4jCountingAppender) reload4jAsyncAppender.getAppender("COUNTING");
                System.err.printf("%4.2f MiB/s,%n", reload4jCountingAppender.resetByteCount() / MEBI / seconds);
                break;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void jboss() {
        jbossLogger.debug(MESSAGE);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void log4jAsyncAppender() {
        log4jLogger.debug(MESSAGE);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void log4jAsyncLogger() {
        log4jLogger.debug(MESSAGE);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void logback() {
        slf4jLogger.debug(MESSAGE);
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void reload4j() {
        reload4jLogger.debug(MESSAGE);
    }
}
