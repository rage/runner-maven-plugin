package fi.helsinki.cs.tmc.runjavafx.maven.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.logging.Log;

import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

public class MavenFinder {
    
    private final Log log;
    
    public MavenFinder(Log log) {
        this.log = log;
    }

    public Path getMavenHome() {
        String mavenHome = System.getenv("M3_HOME");
        if (mavenHome == null) {
            mavenHome = System.getenv("M2_HOME");
        }
        if (mavenHome == null) {
            mavenHome = System.getenv("MAVEN_HOME");
        }
        if (mavenHome == null) {
            mavenHome = System.getProperty("maven.home");
        }
        if (mavenHome == null) {
            return useBundledMaven();
        }
        return Paths.get(mavenHome);
    }

    private Path useBundledMaven() {
        Path mavenHome = getConfigDirectory();
        Path extractedMavenLocation = mavenHome.resolve("apache-maven-3.5.4");
        if (Files.exists(extractedMavenLocation)) {
            log.info("Maven already extracted");

            // Add the name of the extracted folder to the path
            return extractedMavenLocation;
        }
        log.info("Maven bundle not previously extracted, extracting...");
        try {
            InputStream data = getClass().getResourceAsStream("/apache-maven-3.5.4.zip");
            if (data == null) {
                throw new RuntimeException("Could not find bundled maven.");
            }
            Path tmpFile = Files.createTempFile("tmc-maven", "zip");
            Files.copy(data, tmpFile, StandardCopyOption.REPLACE_EXISTING);
            Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP);
            archiver.extract(tmpFile.toFile(), mavenHome.toFile());
            try {
                Files.deleteIfExists(tmpFile);
            } catch (IOException e) {
                log.warn("Deleting tmp apache-maven.zip failed", e);
            }

            // Add the name of the extracted folder to the path
            return mavenHome.resolve("apache-maven-3.5.4");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Path getConfigDirectory() {
        Path configPath;

        if (SystemUtils.IS_OS_WINDOWS) {
            String appdata = System.getenv("APPDATA");
            if (appdata == null) {
                configPath = Paths.get(System.getProperty("user.home"));
            } else {
                configPath = Paths.get(appdata);
            }
        } else {
            // Assume we're using Unix (Linux, Mac OS X or *BSD)
            String configEnv = System.getenv("XDG_CONFIG_HOME");

            if (configEnv != null && configEnv.length() > 0) {
                configPath = Paths.get(configEnv);
            } else {
                configPath = Paths.get(System.getProperty("user.home")).resolve(".config");
            }
        }
        return configPath.resolve("tmc");
    }
}
