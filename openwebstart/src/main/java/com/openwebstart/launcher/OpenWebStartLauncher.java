package com.openwebstart.launcher;

import com.install4j.api.launcher.StartupNotification;
import com.install4j.runtime.installer.helper.InstallerUtil;
import com.openwebstart.install4j.Install4JUtils;
import net.adoptopenjdk.icedteaweb.JavaSystemProperties;
import net.adoptopenjdk.icedteaweb.commandline.CommandLineOptions;
import net.adoptopenjdk.icedteaweb.logging.Logger;
import net.adoptopenjdk.icedteaweb.logging.LoggerFactory;
import net.sourceforge.jnlp.util.logging.FileLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.openwebstart.util.PathQuoteUtil.quoteIfRequired;


/**
 * This launcher resolves OS specific command line argument handling and starts Iced-Tea Web with the correct
 * argument arrangement.
 */
public class OpenWebStartLauncher {

    static {
        // this is placed here above the any thing else to ensure no logger has been created prior to this line
        FileLog.setLogFileNamePostfix("ows-stage1");
    }

    private static final Logger LOG = LoggerFactory.getLogger(OpenWebStartLauncher.class);

    public static void main(String... args) {
        final List<String> verboseArgs = getVerboseArgs(args);
        if (!InstallerUtil.isMacOS()) {
            LOG.info("OWS main args {}.", verboseArgs);
            PhaseTwoWebStartLauncher.main(verboseArgs.toArray(new String[0]));
        } else {
            Install4JUtils.applicationVersion().ifPresent(v -> LOG.info("Starting OpenWebStart MacLauncher {}", v));

            StartupNotification.registerStartupListener(parameters -> {
                try {
                    LOG.info("MacOS detected, Launcher needs to add JNLP file name {} to the list of arguments.", parameters);
                    verboseArgs.add(parameters); // add file name at the end
                    LOG.info("OWS main args {}.", verboseArgs);

                    final List<String> commands = new ArrayList<>();
                    commands.add(quoteIfRequired(JavaSystemProperties.getJavaHome() + "/bin/java"));
                    commands.add("-Dapple.awt.UIElement=true");
                    commands.add("-cp");
                    commands.add(JavaSystemProperties.getJavaClassPath());
                    commands.add(PhaseTwoWebStartLauncher.class.getName());
                    commands.addAll(verboseArgs);

                    LOG.info("Starting: " + commands);
                    new ProcessBuilder().command(commands).inheritIO().start();
                } catch (Exception e) {
                    LOG.error("Error in starting JNLP application", e);
                    throw new RuntimeException("Error in starting JNLP application", e);
                }
            });
        }
    }

    private static ArrayList<String> getVerboseArgs(String[] args) {
        // TODO: this makes sure any app launched with OWS is running in verbose/logging mode. Remove this once OWS is stable
        final ArrayList<String> result = new ArrayList<>(Arrays.asList(args));
        if (!result.contains(CommandLineOptions.VERBOSE.getOption())) {
            result.add(0, CommandLineOptions.VERBOSE.getOption());
        }
        return result;
    }
}
