package eu.europeana.flush.logs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;


import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@SpringBootApplication(scanBasePackages = "eu.europeana.flush.logs")
public class FlushLogsApp implements CommandLineRunner {

    private static final Logger LOG = LogManager.getLogger(FlushLogsApp.class);

    @Autowired
    FlushLogsSettings flushLogsSettings;

    @Override
    public void run(String... args) throws Exception {
        List<String> zipFiles = new ArrayList<>(Arrays.asList(flushLogsSettings.getFiles().split("\\s*,\\s*")));
        LOG.info("zip files to be read {}", zipFiles);

        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();

        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();

        // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();

        LOG.info("heapSize {}", heapSize);
        LOG.info("heapMaxSize {}", heapMaxSize);
        LOG.info("heapFreeSize {}", heapFreeSize);

        zipFiles.stream().forEach(file -> {
            String url = flushLogsSettings.getServer() + file + ".zip";
            LOG.info("Url {}", url);
            String content = readZipFileFromRemote(url);
            System.out.println(content);
        });
    }

    public static void main(String args[]) {
        new SpringApplicationBuilder().sources(FlushLogsApp.class).web(WebApplicationType.NONE).run(args);
    }

    private static String readZipFileFromRemote(String remoteFileUrl) {
        StringBuilder sb = new StringBuilder();
        URL url = getUrl(remoteFileUrl);
        if(url != null) {
            try (InputStream in = new BufferedInputStream(url.openStream(), 1024)) {
                ZipInputStream stream = new ZipInputStream(in);
                byte[] buffer = new byte[1024];
                ZipEntry entry;
                while ((entry = stream.getNextEntry()) != null) {
                    int read;
                    while ((read = stream.read(buffer, 0, 1024)) >= 0) {
                        sb.append(new String(buffer, 0, read));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    private static URL getUrl(String remoteFileUrl) {
        try {
            return new URL(remoteFileUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
