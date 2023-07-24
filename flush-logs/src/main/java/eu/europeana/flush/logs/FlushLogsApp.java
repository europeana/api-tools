package eu.europeana.flush.logs;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
            URI uri = FlushLogsApp.class.getResource("/logs/").toURI();
            LOG.info("Uri {}", uri);

            if (uri.toString().contains("!")) {
                final Map<String, String> env = new HashMap<>();
                final String[] array = uri.toString().split("!");
                final FileSystem fs = FileSystems.newFileSystem(URI.create(array[0] + array[1]), env);
                final Path path = fs.getPath(array[2]);
                LOG.info("path {}", path);
                Stream<Path> pathStream = Files.list(Paths.get(uri));
                pathStream.forEach(FlushLogsApp::print);
                pathStream.close();
                fs.close();
            } else {
                Stream<Path> pathStream = Files.list(Paths.get(uri));
                pathStream.forEach(FlushLogsApp::print);
                pathStream.close();
            }
        } catch (URISyntaxException | IOException e) {
            LOG.error("Error creating the uri or listing the files from /logs folder {}", e);
            System.exit(1); // exit the program at the end even if exception occurs
        }
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
            LOG.info("Flushed {} number of files for {}", StringUtils.substringAfterLast(zipFile.getName(), "/"), numberOfFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
