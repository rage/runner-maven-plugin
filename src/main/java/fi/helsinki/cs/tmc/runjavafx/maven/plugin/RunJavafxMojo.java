package fi.helsinki.cs.tmc.runjavafx.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "run")
public class RunJavafxMojo extends AbstractMojo {

    @Parameter(property = "exec.executable", defaultValue = "java")
    private String executable;

    @Parameter(property = "exec.args", defaultValue = "")
    private String args;

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    public void execute() throws MojoExecutionException {
        final Log logger = getLog();

        try {
            String javaVersion = System.getProperty("java.version");

            MavenFinder mf = new MavenFinder(logger);

            List<String> command = new ArrayList<>();
            if (SystemUtils.IS_OS_WINDOWS) {
                command.add(mf.getMavenHome().resolve(Paths.get("bin", "mvn.cmd")).toString());
            } else {
                command.add(mf.getMavenHome().resolve(Paths.get("bin", "mvn")).toString());
            }
            command.add("\"-Dexec.executable=" + executable + "\"");
            command.add("\"-Dexec.args=" + args + "\"");
            if (javaVersion.startsWith("11")) {
                command.add("javafx:run");
            } else {
                command.add("exec:exec");
            }
            logger.info("Running command: " + command.stream().collect(Collectors.joining(" ")));

            Process process = null;

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(basedir);
            processBuilder.inheritIO();
            process = processBuilder.start();
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                System.exit(exitValue);
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
            throw new MojoExecutionException(ex.getMessage());
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
            throw new MojoExecutionException(ex.getMessage());
        }

    }
}
