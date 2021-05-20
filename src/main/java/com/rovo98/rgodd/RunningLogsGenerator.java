package com.rovo98.rgodd;

import com.rovo98.rgodd.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

/**
 * Utils for generating running logs using the given DFA.
 * <br />
 * Generating logs, and then save the logs to the specified location (or a fixed location).
 * <br />
 * Storage location can be specified in AppConfigs.properties file in the classpath.
 * <br />
 * NOTICE: before calling the generate() method, the following options can be tuned.
 * <ul>
     * <li>minSteps: default 10</li>
     * <li>maxSteps: default 100</li>
     * <li>verbose : default false.</li>
 * </ul>
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.21
 */
public class RunningLogsGenerator {

    private final Logger LOGGER = LoggerFactory.getLogger(RunningLogsGenerator.class);

    private Map<String, String> runningLogs;

    private int[] statistics;

    //Whether to show every generated logs as debug infos in console.
    private final boolean showGeneratedLogs;

    /** Determine the minimum length of the log to be generated */
    private final int minSteps;

    /** Determine the maximum length of the log to be generated */
    private final int maxSteps;

    public RunningLogsGenerator() {
        this(10, 100, false);
    }

    public RunningLogsGenerator(boolean showGeneratedLogs) {
        this(10, 100, showGeneratedLogs);
    }

    public RunningLogsGenerator(int minSteps, int maxSteps) {
        this(minSteps, maxSteps, false);
    }

    public  RunningLogsGenerator(int minSteps, int maxSteps, boolean showGeneratedLogs) {
        this.minSteps = minSteps;
        this.maxSteps = maxSteps;
        this.showGeneratedLogs = showGeneratedLogs;
    }

    /**
     * Generates running logs of the given {@code dfa}
     *
     * @param logSize   the number of the running logs to be generated.
     * @param dfaRoot   the root node of the constructed dfa.
     * @param dfaConfig configuration of the constructed dfa.
     */
    public void generate(int logSize, DFANode dfaRoot, DFAConfig dfaConfig) {
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
    public void generate(int logSize, DFANode dfaRoot, DFAConfig dfaConfig, boolean saveToFile) {
        // TODO: dfa validation may be needed, dfa should be constructed and Config should well prepared.
        // FIXME: code refactoring may be needed here, the constructed dfa should be diagnosable before

        // basic checking
        if (minSteps >= maxSteps)
            throw new IllegalArgumentException("Given minSteps and maxSteps is invalided!");
        LOGGER.info("Generating running logs..., size: {}, steps range: [{}, {}]", logSize, minSteps, maxSteps);

        runningLogs = new HashMap<>(logSize);
        if (dfaConfig.multiFaulty)
            statistics = new int[(int) Math.pow(2, dfaConfig.faultyEvents.length)];
        else
            statistics = new int[dfaConfig.faultyEvents.length + 1];

        // generating and adding running logs to the map.
        while (runningLogs.size() < logSize) {
            Random r = new Random();
            removeConflictedAdd(
                    runningLogs,
                    containsVisitedTraversal(r.nextInt(maxSteps - minSteps + 1) + minSteps,
                            dfaRoot, dfaConfig));
        }

        // statistic infos
        for (String k : runningLogs.keySet()) {
            int index = Integer.parseInt(runningLogs.get(k));
            statistics[index]++;
        }
        LOGGER.info("Running logs generated.All (duplicates removed): {} Normal logs: {}",
                runningLogs.size(), statistics[0]);
        for (int i = 1; i < statistics.length; i++)
            LOGGER.info("====>\t faulty logs, T{}: {}", i, statistics[i]);

        // Saving the logs to file.
        if (saveToFile) {
            String filename = CommonUtils.generateDefaultDFAName(dfaConfig).concat("_running-logs.txt");
            save(filename, dfaConfig);
        }
    }

    // returns true if the logs already contains a log with the same observation
    // to new come log.
    private void removeConflictedAdd(Map<String, String> logs, String newComeLog) {
        // if logs set already contains the newComeLog.
        String[] splitNewComeLog = newComeLog.split("T");
        String newComeObservation = splitNewComeLog[0];
        String newComeLabel = splitNewComeLog[1];
        // ignoring added logs.
        if (logs.containsKey(newComeObservation)) {
            if (logs.get(newComeObservation).equals(newComeLabel)) return;
            // remove the conflicted logs.
            logs.remove(newComeObservation);
        } else {
            logs.put(newComeObservation, newComeLabel);
        }

    }

    // Attaching the log type. Only single faulty mode is considered.
    private String attachingLabel(StringBuilder log, DFAConfig dfaConfig) {
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

    // Attaching the log types. multi-faulty mode is considered.
    private String attachingLabelMultiFaulty(StringBuilder log, DFAConfig dfaConfig) {
        int faultyTypes = dfaConfig.faultyEvents.length;
        byte[] faultyFlags = new byte[faultyTypes];
        for (int i = 0; i < dfaConfig.unobservableEvents.length; i++) {
            if (log.toString().indexOf(dfaConfig.unobservableEvents[i]) >= 0) {
                log = new StringBuilder(log.toString()
                        .replaceAll(dfaConfig.unobservableEvents[i] + "", ""));
                faultyFlags[i] = 1;
            }
        }
        int type = 0;
        for (int i = 0; i < faultyTypes; i++) {
            type += faultyFlags[i] * Math.pow(2, (faultyTypes) - i - 1);
        }
        log.append("T").append(type);
        if (showGeneratedLogs)
            LOGGER.debug("Generated log: {}", log.toString());
        return log.toString();
    }

    // returns a running log of the random stopSteps length.
    private String containsVisitedTraversal(int stopSteps, DFANode root, DFAConfig dfaConfig) {
        if (showGeneratedLogs)
            LOGGER.debug("constructing a log using visited approach, expected len->{}", stopSteps);
        DFANode pNode = root;
        StringBuilder log = new StringBuilder();
        while (stopSteps > 0) {
            Random r = new Random();
            Character[] symbols = pNode.transitions.keySet().toArray(new Character[0]);
            // if no transitions for current node. stop traversing.
            // Since dfa may exists terminal states.
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
        if (dfaConfig.multiFaulty)
            return attachingLabelMultiFaulty(log, dfaConfig);
        return attachingLabel(log, dfaConfig);
    }

    /**
     * Saving the generated running logs into the given file with the name {@code filename} *
     *
     * @param filename the name of the file to save logs.
     */
    @SuppressWarnings("DuplicatedCode")
    private void save(String filename, DFAConfig dfaConfig) {

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
            for (int i = 1; i < statistics.length; i++) {
                statisticInfo = statisticInfo.concat(", T" + i + " logs: " + statistics[i]);
            }
            // add observable event set info
            String obsInfo = "[";
            for (char c : dfaConfig.observableEvents)
                obsInfo = obsInfo.concat(c + ",");
            obsInfo = obsInfo.substring(0, obsInfo.length() - 1).concat("]");
            statisticInfo = statisticInfo.concat(",minLen:" + minSteps).concat(",maxLen:" + maxSteps);
            statisticInfo = statisticInfo.concat(" observable events:").concat(obsInfo);
            bfw.write(statisticInfo);
            bfw.newLine();

            for (String observation : runningLogs.keySet()) {
                String log = observation.concat("T").concat(runningLogs.get(observation));
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
        RunningLogsGenerator runningLogsGenerator = new RunningLogsGenerator(15, 20, true);
        DFAConstructor constructor = SimpleDFAConstructor.getInstance();
        runningLogsGenerator.generate(
                10,
                constructor.constructRandomDFAWithDiagnosability(11, 20, false),
                constructor.getDFAConfig());
    }
}
