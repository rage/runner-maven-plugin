package fi.helsinki.cs.tmc.runner.maven.plugin;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "run-javafx")
public class RunJavafxMojo extends BaseMojo {

    @Parameter(property = "exec.executable", defaultValue = "java")
    private String executable;

    public void execute() throws MojoExecutionException {
        Log logger = getLog();
        List<String> command = super.buildCommand(logger);

        int lastIndex = command.size() - 1;
        String lastCommand = command.get(lastIndex);
        command.remove(lastIndex);
        command.add("\"-Djavafx.executable=" + executable + "\"");

        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_11)) {
            command.add("javafx:run");
        } else {
            command.add(lastCommand);
        }

        try {
            super.executeCommand(command, logger);
        } catch (IOException | InterruptedException ex) {
            logger.error(ex.getMessage());
            throw new MojoExecutionException(ex.getMessage());
        }


    }
}
