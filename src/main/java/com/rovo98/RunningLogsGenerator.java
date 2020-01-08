package com.rovo98;

import com.rovo98.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

/**
 * Utils for generating running logs using the given DFA.
 *
 * - Generating logs, and then save the logs to specified locations (or a fix location).
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.21
 */
public class RunningLogsGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunningLogsGenerator.class);

    private static Set<String> runningLogs;

    private static int[] statistics;

    private static boolean showGeneratedLogs = false;

    private static int minSteps = 30;
    private static int maxSteps = 100;

    //Whether to show every generated logs as debug infos in console.

    /**
     * settings control whether to print out the debug msg for every generated logs.
     * False by default.
     *
     * @param verbose {@code true} for yes, and {@code false} for no
     */
    public static void setVerbose(boolean verbose) {
        showGeneratedLogs = verbose;
    }

    /**
     * Setting minimum length of the generated log.
     * By default, 30 is token.
     *
     * @param ms An Integer representing minimum length of the log to be generated.
     */
    public static void setMinSteps(int ms) {
        minSteps = ms;
    }

    /**
     * Setting maximum length of the generated log.
     * By default, 100 is token
     *
     * @param ms An Integer representing maximum length of the log to be generated.
     */
    public static void setMaxSteps(int ms) {
        maxSteps = ms;
    }

    /**
     * Generates running logs of the given {@code dfa}
     *
     * @param logSize   the number of the running logs to be generated.
     * @param dfaRoot   the root node of the constructed dfa.
     * @param dfaConfig configuration of the constructed dfa.
     */
    public static void generate(int logSize, DFANode dfaRoot, DFAConfig dfaConfig) {
        generate(logSize, dfaRoot, dfaConfig, false);
    }

    /**
     * Generates running logs of the given {@code dfa}
     *
     * @param logSize    the number of the running logs to be generated.
     * @param dfaRoot    the root node of the constructed dfa.
     * @param dfaConfig  configuration of the constructed dfa.
     * @param saveToFile whether to save the generated logs to file.
     */
    public static void generate(int logSize, DFANode dfaRoot, DFAConfig dfaConfig, boolean saveToFile) {
        // TODO: dfa validation may be needed, dfa should be constructed and Config should well prepared.
        // FIXME: code refactoring is needed here, the constructed dfa should be diagnosable before
        // using it to generate logs.

        // basic checking
        if (minSteps >= maxSteps)
            throw new IllegalArgumentException("Given minSteps and maxSteps is invalided!");
        LOGGER.info("Generating running logs..., size: {}, steps range: [{}, {}]", logSize, minSteps, maxSteps);

        runningLogs = new HashSet<>(logSize);
        statistics = new int[dfaConfig.faultyEvents.length + 1];

        for (int i = 0; i < logSize; i++) {
            Random r = new Random();
            runningLogs.add(containsVisitedTraversal(
                    r.nextInt(maxSteps - minSteps + 1) + minSteps, dfaRoot, dfaConfig));
        }

        // statistic infos
        for (String l : runningLogs) {
            int index = Integer.parseInt(String.valueOf(l.charAt(l.length() - 1)));
            statistics[index]++;
        }
        LOGGER.info("Running logs generated.All (duplicates removed): {} Normal logs: {}",
                runningLogs.size(), statistics[0]);
        for (int i = 0; i < dfaConfig.faultyEvents.length; i++)
            LOGGER.info("====>\t faulty logs, T{}: {}", (i + 1), statistics[i + 1]);

        // Saving the logs to file.
        if (saveToFile) {
            String filename = CommonUtils.generateDefaultDFAName(dfaConfig).concat("_running-logs.txt");
            save(filename, dfaConfig);
        }
    }

    private static String attachingLabel(StringBuilder log, DFAConfig dfaConfig) {
        for (int i = 0; i < dfaConfig.unobservableEvents.length; i++) {
            if (log.toString().indexOf(dfaConfig.unobservableEvents[i]) >= 0) {
                log = new StringBuilder(log.toString()
                        .replaceAll(dfaConfig.unobservableEvents[i] + "", ""));
                log.append("T").append(i + 1);
                break;
            }
        }
        if (log.toString().indexOf('T') < 0)
            log.append("T0");

        if (showGeneratedLogs)
            LOGGER.debug("Generated log: {}", log.toString());

        return log.toString();
    }

    private static String containsVisitedTraversal(int stopSteps, DFANode root, DFAConfig dfaConfig) {
        if (showGeneratedLogs)
            LOGGER.debug("constructing a log using visited approach, expected len->{}", stopSteps);
        DFANode pNode = root;
        StringBuilder log = new StringBuilder();
        while (stopSteps > 0) {
            Random r = new Random();
            Character[] symbols = pNode.transitions.keySet().toArray(new Character[0]);
            // if no transitions for current node. stop traversing.
            if (symbols.length == 0)
                break;
            // navigating to the next unvisited node.
            char symbol = symbols[r.nextInt(symbols.length)];

            pNode = pNode.navigate(symbol, dfaConfig);
            log.append(symbol);
            stopSteps--;
        }
        if (showGeneratedLogs)
            LOGGER.debug("Generated observation({}): {}", log.toString().length(), log.toString());
        return attachingLabel(log, dfaConfig);
    }

    /**
     * Saving the generated running logs into the given file with the name {@code filename} *
     *
     * @param filename the name of the file to save logs.
     */
    private static void save(String filename, DFAConfig dfaConfig) {

        LOGGER.info("Saving the generated logs to file : {}", filename);
        // loads storage location config.
        Properties config = new Properties();
        try {
            config.load(RunningLogsGenerator.class.getClassLoader()
                    .getResourceAsStream("AppConfigs.properties"));
            String location = config.getProperty("logs.storageLocation");


            Path folder = Paths.get(location);
            String path = location + File.separator + filename;
            // create the folder if not existed.
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }
            BufferedWriter bfw = new BufferedWriter(new FileWriter(path));

            // writes statics info as the first line.
            String statisticInfo = "";
            statisticInfo = statisticInfo.concat("Logs size: " + runningLogs.size())
                    .concat(", Normal logs: " + statistics[0]);
            for (int i = 0; i < dfaConfig.faultyEvents.length; i++) {
                statisticInfo = statisticInfo.concat(", T" + (i + 1) + " logs: " + statistics[i + 1]);
            }
            // add observable event set info
            String obsInfo = "[";
            for (char c : dfaConfig.observableEvents)
                obsInfo = obsInfo.concat(c + ",");
            obsInfo = obsInfo.substring(0, obsInfo.length() - 1).concat("]");
            statisticInfo = statisticInfo.concat(" observable events:").concat(obsInfo);
            bfw.write(statisticInfo);
            bfw.newLine();

            for (String log : runningLogs) {
                bfw.write(log);
                bfw.newLine();
            }
            bfw.close();
            LOGGER.debug("==>\t File location: {}", path);
            LOGGER.info("==>\t Done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Driver the program to test generating logs.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        RunningLogsGenerator.setVerbose(false);
        RunningLogsGenerator.setMinSteps(50);
        RunningLogsGenerator.setMaxSteps(100);
        DFAConstructor constructor = new SimpleDFAConstructor();
        RunningLogsGenerator.generate(
                1_00,
                constructor.constructRandomDFA(50, 100),
                constructor.getDFAConfig());
    }
}
