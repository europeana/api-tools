package eu.europeana.flush.logs;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SpringBootApplication(scanBasePackages = "eu.europeana.flush.logs")
public class FlushLogsApp {

    private static final Logger LOG = LogManager.getLogger(FlushLogsApp.class);

    public static void main(String[] args) throws IOException, URISyntaxException {
        LOG.info("Starting FlushLogsApp..................");
        Stream<Path> pathStream = Files.list(Paths.get(FlushLogsApp.class.getResource("/logs/").toURI()));
        pathStream.forEach(FlushLogsApp::print);
        pathStream.close();

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
