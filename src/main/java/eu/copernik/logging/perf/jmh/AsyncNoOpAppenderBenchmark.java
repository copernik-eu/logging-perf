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

import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.jboss.logmanager.LogContext;
import org.jboss.logmanager.configuration.PropertyLogContextConfigurator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.BenchmarkParams;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(time = 100, timeUnit = TimeUnit.MILLISECONDS, iterations = 200)
@State(Scope.Benchmark)
public class AsyncNoOpAppenderBenchmark {

    private static final String MESSAGE = "This is a `DEBUG` message.";

    private static final String FQCN = "eu.copernik.logging.perf.jmh.AsyncNoOpAppenderBenchmark";
    private static final String BENCHMARK_JBOSS = FQCN + ".jboss";
    private static final String BENCHMARK_LOG4J_ASYNC_APPENDER = FQCN + ".log4jAsyncAppender";
    private static final String BENCHMARK_LOG4J_DISRUPTOR = FQCN + ".log4jDisruptor";
    private static final String BENCHMARK_LOGBACK_ASYNC_APPENDER = FQCN + ".logbackAsyncAppender";
    private static final String BENCHMARK_LOGBACK_DISRUPTOR = FQCN + ".logbackDisruptor";
    private static final String BENCHMARK_RELOAD4J = FQCN + ".reload4j";

    private org.jboss.logging.Logger jbossLogger;
    private org.apache.logging.log4j.Logger log4jLogger;
    private org.apache.log4j.Logger reload4jLogger;
    private org.slf4j.Logger slf4jLogger;

    @Param("262144")
    private int bufferSize;

    @Setup
    public void up(final BenchmarkParams params) throws Exception {
        final String benchmark = params.getBenchmark();

        // Common
        final String bufferSize = Integer.toString(this.bufferSize);
        System.setProperty("benchmark.bufferSize", bufferSize);

        switch (benchmark) {
            case BENCHMARK_JBOSS:
                try (final InputStream inputStream = AsyncNoOpAppenderBenchmark.class.getResourceAsStream(
                        "/jboss/AsyncNoOpAppenderBenchmark.properties")) {
                    final LogContext jbossContext = LogContext.getLogContext();
                    jbossContext.close();
                    new PropertyLogContextConfigurator().configure(jbossContext, inputStream);
                }
                jbossLogger = org.jboss.logging.Logger.getLogger(FQCN);
                break;
            case BENCHMARK_LOG4J_ASYNC_APPENDER:
                System.setProperty(
                        "log4j2.configurationFile", "log4j/AsyncNoOpAppenderBenchmark/log4jAsyncAppender.xml");
                log4jLogger = org.apache.logging.log4j.LogManager.getLogger(FQCN);
                break;
            case BENCHMARK_LOG4J_DISRUPTOR:
                System.setProperty("log4j.configurationFile", "log4j/AsyncNoOpAppenderBenchmark/log4jDisruptor.xml");
                System.setProperty(
                        "log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
                System.setProperty("log4j2.asyncLoggerRingBufferSize", bufferSize);
                System.setProperty("log4j2.asyncLoggerWaitStrategy", "YIELD");
                log4jLogger = org.apache.logging.log4j.LogManager.getLogger(FQCN);
                break;
            case BENCHMARK_LOGBACK_ASYNC_APPENDER:
                System.setProperty(
                        "logback.configurationFile", "logback/AsyncNoOpAppenderBenchmark/logbackAsyncAppender.xml");
                slf4jLogger = org.slf4j.LoggerFactory.getLogger(FQCN);
                break;
            case BENCHMARK_LOGBACK_DISRUPTOR:
                System.setProperty(
                        "logback.configurationFile", "logback/AsyncNoOpAppenderBenchmark/logbackDisruptor.xml");
                slf4jLogger = org.slf4j.LoggerFactory.getLogger(FQCN);
                break;
            case BENCHMARK_RELOAD4J:
                System.setProperty("log4j.configuration", "reload4j/AsyncNoOpAppenderBenchmark.xml");
                reload4jLogger = org.apache.log4j.LogManager.getLogger(FQCN);
                break;
        }
    }

    @TearDown
    public void sleep() throws InterruptedException {
        // Give the async thread 500 ns per log event to clear the queue.
        Thread.sleep((bufferSize * 500L) / 1_000_000L);
    }

    @Benchmark
    public void jboss() {
        jbossLogger.debug(MESSAGE);
    }

    @Benchmark
    public void log4jAsyncAppender() {
        log4jLogger.debug(MESSAGE);
    }

    @Benchmark
    public void log4jDisruptor() {
        log4jLogger.debug(MESSAGE);
    }

    @Benchmark
    public void logbackAsyncAppender() {
        slf4jLogger.debug(MESSAGE);
    }

    @Benchmark
    public void logbackDisruptor() {
        slf4jLogger.debug(MESSAGE);
    }

    @Benchmark
    public void reload4j() {
        reload4jLogger.debug(MESSAGE);
    }
}
