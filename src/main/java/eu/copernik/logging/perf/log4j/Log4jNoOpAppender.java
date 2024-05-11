package eu.copernik.logging.perf.log4j;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "NoOp", category = Node.CATEGORY)
public class Log4jNoOpAppender extends AbstractAppender {

    @PluginFactory
    public static Log4jNoOpAppender create(@PluginAttribute("name") String name) {
        return new Log4jNoOpAppender(name);
    }

    private Log4jNoOpAppender(String name) {
        super(name, null, null, true, null);
    }

    @Override
    public void append(LogEvent event) {}
}
