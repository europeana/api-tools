package eu.europeana.flush.logs;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SpringBootApplication(scanBasePackages = "eu.europeana.flush.logs")
public class FlushLogsApp {

    private static final Logger LOG = LogManager.getLogger(FlushLogsApp.class);

    /**
     *
     * Pushes the Logs file on the Kubernetes Node
     * Makes ure to provide the location as an argument if running in IDE or locally
     *
     * If the cronjob is running in kubernetes,
     * make sure the command in the cronjob.yaml.template has the correct location
     * @param args
     */
    //Uri jar:file:/opt/app/flush-logs-api.jar!/BOOT-INF/classes!/logs/
    public static void main(String[] args) {
        LOG.info("Starting FlushLogsApp..................");
        try {
            if(args.length > 0) {
                LOG.info("Running the application with arguments {}", args[0]);
                Stream<Path> pathStream = Files.list(Paths.get(args[0]));
                pathStream.forEach(FlushLogsApp::print);
                pathStream.close();
            }
        } catch (IOException e) {
            LOG.error("Files not present at {}", args[0], e);
            System.exit(1); // exit the program at the end even if exception occurs
        }
        System.exit(1);
    }

    public static void print(Path p) {
        LOG.info( "{}", p);
        // TODO remove later only for testing purpose, later change to .zip extension
        if (StringUtils.contains(p.toString(),"test.zip" )) {
            try (ZipFile zipFile = new ZipFile(p.toString())) {
                int numberOfFiles = 0;
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    numberOfFiles++;
                    ZipEntry entry = entries.nextElement();
                    InputStream stream = zipFile.getInputStream(entry);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    String nextLine;
                    while ((nextLine = reader.readLine()) != null) {
                        System.out.println(nextLine);
                    }
                    stream.close();
                    reader.close();
                }
                LOG.info("Flushed {} number of files for {}", numberOfFiles, StringUtils.substringAfterLast(zipFile.getName(), "/"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
