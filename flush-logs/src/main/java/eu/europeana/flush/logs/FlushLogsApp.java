package eu.europeana.flush.logs;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.stylesheets.LinkStyle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SpringBootApplication(scanBasePackages = "eu.europeana.flush.logs")
public class FlushLogsApp {

    private static final Logger LOG = LogManager.getLogger(FlushLogsApp.class);

    /**
     *
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
        try (ZipFile zipFile = new ZipFile(p.toString())) {
            int numberOfFiles =0;
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
            LOG.info("Flushed {} number of files for {}",numberOfFiles, StringUtils.substringAfterLast(zipFile.getName(), "/"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
