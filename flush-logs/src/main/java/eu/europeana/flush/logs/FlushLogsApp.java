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

        zipFiles.stream().forEach(file -> {
            String url = flushLogsSettings.getServer() + file + ".zip";
            LOG.info("Url {}", url);
            readZipFileFromRemote(url);
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
                ZipEntry entry;
                while ((entry = stream.getNextEntry()) != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    String nextLine;
                    while ((nextLine = reader.readLine()) != null) {
                        System.out.println(nextLine);
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
