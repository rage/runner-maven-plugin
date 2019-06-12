package fi.helsinki.cs.tmc.runner.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "run-with-fixed-umlauts")
public class FixUmlautsMojo extends BaseMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true)
    private File basedir;

    public void execute() throws MojoExecutionException {
        Log logger = getLog();
        try {
            List<String> command = super.buildCommand(logger);

            logger.info("Running command: " + String.join(" ", command));

            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .directory(basedir)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT);

            if (SystemUtils.IS_OS_WINDOWS) {
                processBuilder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            }

            Process process = processBuilder.start();

            if (!SystemUtils.IS_OS_WINDOWS) {
                Thread inputRedirector = startInputRedirectorThread(process.getOutputStream());
                inputRedirector.start();
            }

            int exitValue = process.waitFor();
            if (exitValue != 0) {
                System.exit(exitValue);
            }
        } catch (IOException | InterruptedException ex) {
            logger.error(ex.getMessage());
            throw new MojoExecutionException(ex.getMessage());
        }
    }

    private Thread startInputRedirectorThread(OutputStream processOutputStream) {
        InputStream in = System.in;

        return new Thread(() -> {
            try {
                int intByte = in.read();

                while (intByte != -1) {
                    switch (intByte) {
                        case 228: // ä
                            processOutputStream.write((byte) 195);
                            processOutputStream.write((byte) 164);
                            processOutputStream.flush();
                            break;
                        case 246: // ö
                            processOutputStream.write((byte) 195);
                            processOutputStream.write((byte) 182);
                            processOutputStream.flush();
                            break;
                        case 229: // å
                            processOutputStream.write((byte) 195);
                            processOutputStream.write((byte) 165);
                            processOutputStream.flush();
                            break;
                        case 196: // Ä
                            processOutputStream.write((byte) 195);
                            processOutputStream.write((byte) 132);
                            processOutputStream.flush();
                            break;
                        case 214: // Ö
                            processOutputStream.write((byte) 195);
                            processOutputStream.write((byte) 150);
                            processOutputStream.flush();
                            break;
                        case 197: // Å
                            processOutputStream.write((byte) 195);
                            processOutputStream.write((byte) 133);
                            processOutputStream.flush();
                            break;
                        default:
                            byte intByteAsByte = (byte) intByte;
                            processOutputStream.write(intByteAsByte);
                            processOutputStream.flush();
                            break;
                    }
                    intByte = in.read();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
