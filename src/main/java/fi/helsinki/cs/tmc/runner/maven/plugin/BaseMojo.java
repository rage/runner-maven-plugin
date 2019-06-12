package fi.helsinki.cs.tmc.runner.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

abstract class BaseMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    public void execute() throws MojoExecutionException {
        Log logger = getLog();

        List<String> command = buildCommand(logger);
        try {
            executeCommand(command, logger);
        } catch (IOException | InterruptedException ex) {
            logger.error(ex.getMessage());
            throw new MojoExecutionException(ex.getMessage());
        }
    }

    List<String> buildCommand(Log logger) {
        List<String> command = new ArrayList<>();
        MavenFinder mf = new MavenFinder(logger);

        if (SystemUtils.IS_OS_WINDOWS) {
            Path pathToMaven = mf.getMavenHome().resolve(Paths.get("bin", "mvn.cmd"));
            if (!Files.exists(pathToMaven)) {
                pathToMaven = mf.useBundledMaven().resolve(Paths.get("bin", "mvn.cmd"));
            }
            command.add(pathToMaven.toString());
        } else {
            command.add(mf.getMavenHome().resolve(Paths.get("bin", "mvn")).toString());
        }

        session.getUserProperties().forEach((key, value) -> command.add("\"-D" + key.toString() + "=" + value.toString() + "\""));

        command.add("exec:exec");

        return command;
    }

    void executeCommand(List<String> command, Log logger) throws IOException, InterruptedException {
        logger.info("Running command: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command)
                .directory(basedir)
                .inheritIO();

        Process process = processBuilder.start();

        int exitValue = process.waitFor();
        if (exitValue != 0) {
            System.exit(exitValue);
        }
    }
}
