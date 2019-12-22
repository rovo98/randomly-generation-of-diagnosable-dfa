package com.rovo98;

import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private static Set<String> runningLogs = new HashSet<>();

    private static int[] statistics;

    /**
     * Generates running logs of the given {@code dfa}
     *
     * @param logSize the number of the running logs to be generated.
     * @param dfa     A constructed DFA.
     */
    public static void generate(int logSize, DFANode dfa, boolean saveToFile) {
        // FIXME: refactoring may be needed, dfa should be constructed and Config should well prepared.

        LOGGER.info("Generating running logs..., size: {}", logSize);

        statistics = new int[Config.faultyEvents.length + 1];

        int minSteps = 30;
        int maxSteps = 100;

        for (int i = 0; i < logSize; i++) {
            Random r = new Random();
            runningLogs.add(containsVisited(r.nextInt(maxSteps - minSteps + 1) + minSteps, dfa));
//            if (r.nextInt(2) > 0)
//                runningLogs.add(
//                        unvisitedTraversal(r.nextInt(maxSteps - minSteps + 1) + minSteps, dfa));
//            else
//                runningLogs.add(
//                        containsVisited(r.nextInt(maxSteps - minSteps + 1) + minSteps, dfa));
        }
        // do statistic
        for (String l : runningLogs) {
            int index = Integer.parseInt(String.valueOf(l.charAt(l.length() - 1)));
            statistics[index]++;
        }
        LOGGER.info("Running logs generated.All (duplicates removed): {} Normal logs: {}",
                runningLogs.size(), statistics[0]);
        for (int i = 0; i < Config.faultyEvents.length; i++)
            LOGGER.info("====>\t faulty logs, T{}: {}", (i + 1), statistics[i + 1]);

        // Saving the logs to file.
        if (saveToFile) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String filename = Base64.encode((Config.stateSize + ":" + Config.faultyStateSize).getBytes());
            filename = df.format(new Date()).concat("_").concat(filename).concat("_running-logs.txt");
            save(filename);
        }
    }

    private static String unvisitedTraversal(int stopSteps, DFANode root) {

        LOGGER.debug("constructing a log using unvisited approach, expected len->{}", stopSteps);
        boolean[] visited = new boolean[Config.stateSize];
        DFANode pNode = root;
        StringBuilder log = new StringBuilder();
        while (stopSteps > 0) {
            // marked the state as visited.
            visited[pNode.state] = true;
            Random r = new Random();
            Character[] symbols = pNode.transitions.keySet().toArray(new Character[0]);
            if (symbols.length == 0)
                break;
            // navigating to the next unvisited node.
            char symbol = symbols[r.nextInt(symbols.length)];
            while (visited[pNode.navigate(symbol).state])
                symbol = symbols[r.nextInt(symbols.length)];

            pNode = pNode.navigate(symbol);
            log.append(symbol);
            stopSteps--;
        }
        LOGGER.debug("Generated observation: {}", log.toString());
        return attachingLabel(log);
    }

    private static String attachingLabel(StringBuilder log) {
        for (int i = 0; i < Config.unobservableEvents.length; i++) {
            if (log.toString().indexOf(Config.unobservableEvents[i]) >= 0) {
                log = new StringBuilder(log.toString()
                        .replaceAll(Config.unobservableEvents[i] + "", ""));
                log.append("T").append(i + 1);
                break;
            }
        }
        if (log.toString().indexOf('T') < 0)
            log.append("T0");
        LOGGER.debug("Generated log: {}", log.toString());

        return log.toString();
    }

    private static String containsVisited(int stopSteps, DFANode root) {

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

            pNode = pNode.navigate(symbol);
            log.append(symbol);
            stopSteps--;
        }
        LOGGER.debug("Generated observation({}): {}", log.toString().length(), log.toString());
        return attachingLabel(log);
    }

    /**
     * Saving the generated running logs into the given file with the name {@code filename} *
     *
     * @param filename the name of the file to save logs.
     */
    public static void save(String filename) {

        LOGGER.info("Saving the generated logs to file : {}", filename);
        // loads storage location config.
        Properties config = new Properties();
        try {
            config.load(RunningLogsGenerator.class.getClassLoader()
                    .getResourceAsStream("GeneratorConfig.properties"));
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
            for (int i = 0; i < Config.faultyEvents.length; i++) {
                statisticInfo = statisticInfo.concat(", T" + (i + 1) + " logs: " + statistics[i + 1]);
            }
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
        RunningLogsGenerator.generate(
                50,
                new SimpleDFAConstructor().constructRandomDFA(50, 100),
                false);
    }
}
