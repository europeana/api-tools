package eu.europeana.flush.logs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:flush.logs.properties")
@PropertySource(value = "classpath:flush.logs.user.properties", ignoreResourceNotFound = true)
public class FlushLogsSettings {

    @Value("${server.name}")
    private String server;

    @Value("${files}")
    private String files;

    public String getServer() {
        return server;
    }

    public String getFiles() {
        return files;
    }
}
