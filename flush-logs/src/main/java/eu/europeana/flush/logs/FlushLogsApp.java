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
//        public static void main(String[] args) {
//            LOG.info("Starting FlushLogsApp..................");
//            try {
//                Stream<Path> pathStream = Files.list(Paths.get(FlushLogsApp.class.getResource("test.zip").toURI()));
//                pathStream.forEach(FlushLogsApp::print);
//                pathStream.close();
//            } catch (Exception e) {
//                LOG.error("Files not present in resource", e);
//                System.exit(1); // exit the program at the end even if exception occurs
//            }
//            System.exit(1);
//        }
    public static void main(final String[] args) throws IOException
    {
        //Creating instance to avoid static member methods
        FlushLogsApp instance = new FlushLogsApp();

        InputStream is = instance.getFileAsIOStream("application.2.log");
        instance.printFileContent(is);

//        is = instance.getFileAsIOStream("data/demo.txt");
//        instance.printFileContent(is);
    }

    private InputStream getFileAsIOStream(final String fileName)
    {
        InputStream ioStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null) {
            throw new IllegalArgumentException(fileName + " is not found");
        }
        return ioStream;
    }

    private void printFileContent(InputStream is) throws IOException
    {
        try (InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr);)
        {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            is.close();
        }
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
