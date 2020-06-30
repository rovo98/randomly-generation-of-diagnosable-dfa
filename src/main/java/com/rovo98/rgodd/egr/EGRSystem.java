package com.rovo98.rgodd.egr;

import com.rovo98.rgodd.DFANode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;


/**
 * EGR (Exhaust Gas Recirculation system simulation).
 *<br />
 * This EGR system is referenced from <a href='https://link.springer.com/article/10.1007/BF01441211'>
 *     Diagnosability of Discrete-Event Systems and its applications.</a>,
 *     an on-line diagnosability example in the paper.
 *<br />
 * we modify the system, make it generating running-logs dataset meet our requirements.
 *<br />
 * NOTICE: only one faulty event in this system but there is one more unobservable event.
 *<br />
 * @author rovo98
 */
public class EGRSystem implements Serializable {
    private static final long serialVersionUID = -8081052981065250122L;

    private final static int NUM_OF_STATES = 8;
    private final static int MIN_LOG_LEN = 10;
    private final static int MAX_LOG_LEN = 50;

    private DFANode[] dfaNodes;
    private Set<String> oriLogs;
    private boolean verbose = false;

    private final char UNOBSERVABLE = 'd';
    // only two state partitions in this system.
    List<Integer> firstPartition = new ArrayList<>(Arrays.asList(1, 2, 3, 5, 6, 7));
    List<Integer> secondPartition = new ArrayList<>(Arrays.asList(4, 8));

    Map<Integer, Byte> h; // state output map function

    // this class can not be instanced.
    private EGRSystem() {}
    private static class SingletonHolder {
        private static final EGRSystem INSTANCE = new EGRSystem();
    }

    public static EGRSystem getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // twitch the verbose, default is set to false.
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    // for serialization consideration.
    protected EGRSystem readResolve() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * generating original control event sequences for the number of the given size.
     * with the loss of generality, we constraint the maximum length of log
     * to be generate to 5 ~ 10
     * @param size the number of the running-logs to be generated.
     * @param printStatistic control whether to print out the statistic info.
     * @param saveToFile control whether to print out every generated log.
     */
    public void generateOriLogs(int size, boolean printStatistic, boolean saveToFile) {
        oriLogs = new HashSet<>(size);
        // initialization
        initialize();
        Random r = new Random();

        while (oriLogs.size() < size) {
            // starting from a random dfa node.
            DFANode start = dfaNodes[r.nextInt(dfaNodes.length)];
            // determinate the log length
            int lLen = MIN_LOG_LEN + r.nextInt(MAX_LOG_LEN - MIN_LOG_LEN + 1);
            StringBuilder log = new StringBuilder();
            int lc = 0;
            while (lc < lLen) {
                Character[] symbols = start.getTransitions().keySet()
                        .toArray(new Character[0]);
                char e = symbols[r.nextInt(symbols.length)];
                if (e != UNOBSERVABLE) // filtering the only observable event.
                    log.append(e);
                start = dfaNodes[start.getTransitions().get(e) - 1];
                lc++;
            }
            oriLogs.add(log.toString());
        }
        // filtering logs
        this.filteringLogs();
        int[] statistics = this.countStatistics(printStatistic);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (saveToFile)
            this.save(df.format(new Date()) + "_egr-system-logs.txt", statistics);
    }

    // THE FOLLOWING ARE HELPER FUNCTIONS
    private void initialize() {
        // initialization
        dfaNodes = new DFANode[NUM_OF_STATES];
        for (int i = 1; i <= NUM_OF_STATES; i++) {
            dfaNodes[i - 1] = new DFANode(i);
        }
        // two possible outputs [not descending or descending ]
        byte[] outputs = new byte[] {0, 1};
        h = new HashMap<>();
        for (int i = 1; i <= 6; i++)
            h.put(i, outputs[0]);
        h.put(7, outputs[1]);
        h.put(8, outputs[1]);
        // according to the overall system state diagram,
        // we construct every node individually.
        // node 1
        addTransitionHelper(dfaNodes[0], new char[]{'a', 'b', 'f'}, 1);
        dfaNodes[0].addTransition('e', 5);
        // node 2
        addTransitionHelper(dfaNodes[1], new char[]{'b', 'f'}, 2);
        dfaNodes[1].addTransition('d', 1);
        dfaNodes[1].addTransition('e', 6);
        dfaNodes[1].addTransition('a', 3);
        // node 3
        addTransitionHelper(dfaNodes[2], new char[]{'a', 'f'}, 3);
        dfaNodes[2].addTransition('b', 2);
        dfaNodes[2].addTransition('e', 7);
        dfaNodes[2].addTransition('c', 4);
        // node 4
        addTransitionHelper(dfaNodes[3], new char[]{'a', 'b', 'f'}, 4);
        dfaNodes[3].addTransition('e', 8);
        // node 5
        addTransitionHelper(dfaNodes[4], new char[]{'a', 'b', 'e'}, 5);
        dfaNodes[4].addTransition('f', 1);
        // node 6
        addTransitionHelper(dfaNodes[5], new char[]{'b', 'e'}, 6);
        dfaNodes[5].addTransition('f', 2);
        dfaNodes[5].addTransition('d', 5);
        dfaNodes[5].addTransition('a', 7);
        // node 7
        addTransitionHelper(dfaNodes[6], new char[]{'a', 'e'}, 7);
        dfaNodes[6].addTransition('b', 6);
        dfaNodes[6].addTransition('f', 3);
        dfaNodes[6].addTransition('c', 8);
        // node 8
        addTransitionHelper(dfaNodes[7], new char[]{'a', 'b', 'e'}, 8);
        dfaNodes[7].addTransition('f', 4);
    }

    private int[] countStatistics(boolean printOut) {
        int[] statistic = new int[3];
        for (String log : oriLogs) {
            statistic[Integer.parseInt(log.split("T")[1])]++;
        }
        if (printOut) {
            System.out.println("Generated logs statistics:");
            for (int i = 0; i < statistic.length; i++) {
                System.out.println("> T" + i + ": " + statistic[i]);
            }
        }
        return statistic;
    }

    private void filteringLogs() {
        Set<String> processedLogs = new HashSet<>();
        for (String log : oriLogs) {
            if (!isOnlineDiagnosable(log))
                log += "T2";
            processedLogs.add(log);
        }
        // empty oriLogs set.
        oriLogs.clear();
        for (String log : processedLogs) {
            oriLogs.add(attachLabel(log));
        }
    }

    // references to the algorithm proposed by Feng Lin.
    // in Diagnosability of Discrete-Event systems and
    // its applications.
    private boolean isOnlineDiagnosable(String log) {
        // filtering the unobservable events firstly.
        log = log.replaceAll(UNOBSERVABLE + "", "");
        char[] events = log.toCharArray();

        DFANode[] finalNodes = Stream.of(dfaNodes)
                .filter(node -> node.getTransitions().containsKey(events[0]))
                .toArray(DFANode[]::new);
        for (char e : events) {
            finalNodes = Stream.of(finalNodes)
                    .filter(n -> n.getTransitions().containsKey(e))
                    .map(n -> dfaNodes[n.getTransitions().get(e) - 1])
                    .distinct()
                    .toArray(DFANode[]::new);
        }
        Objects.requireNonNull(finalNodes);
        // check on-line diagnosability
        for (int i = 0; i < finalNodes.length; i++) {
            for (int j = i+1; j < finalNodes.length; j++) {
                int x1 = finalNodes[i].getState();
                int x2 = finalNodes[j].getState();
                if (h.get(x1).equals(h.get(x2)))
                    if (firstPartition.contains(x1) && secondPartition.contains(x2) ||
                    firstPartition.contains(x2) && secondPartition.contains(x1))
                        return false;
            }
        }
        return true;
    }

    private String attachLabel(String log) {
        // FIXME: temporary implementation.
        char faultyEvent = 'c';

        if (log.contains(faultyEvent + "")) {
            log = log.replaceAll(faultyEvent + "", "");
            if (!log.contains("T"))
                log = log + "T1";
        } else {
            if (!log.contains("T"))
                log = log + "T0";
        }
        if (verbose)
            System.out.println("> generated log: " + log);
        return log;
    }

    private void addTransitionHelper(DFANode node, char[] events, int ns) {
        for (char e : events)
            node.addTransition(e, ns);
    }

    // save generated logs to file.
    // FIXME: extract duplicated code (utility function may be adopted)
    @SuppressWarnings("DuplicatedCode")
    private  void save(String filename, int[] statistics) {

        System.out.println("Saving the generated logs to file : "+  filename);
        // loads storage location config.
        Properties config = new Properties();
        try {
            config.load(EGRSystem.class.getClassLoader()
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
            statisticInfo = statisticInfo.concat("Logs size: " + oriLogs.size())
                    .concat(", Normal logs: " + statistics[0]);
            for (int i = 1; i < statistics.length; i++) {
                statisticInfo = statisticInfo.concat(", T" + i + " logs: " + statistics[i]);
            }
            // add observable event set info
            String obsInfo = "[a,b,e,f]";
            statisticInfo = statisticInfo.concat(",minLen:" + MIN_LOG_LEN).concat(",maxLen:" + MAX_LOG_LEN);
            statisticInfo = statisticInfo.concat(" observable events:").concat(obsInfo);
            bfw.write(statisticInfo);
            bfw.newLine();

            for (String log : oriLogs) {
                bfw.write(log);
                bfw.newLine();
            }
            bfw.close();
            System.out.println("==>\t File location: " + path);
            System.out.println("==>\t Done.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Driver the program to run this EGR system to generate logs.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        EGRSystem egrSystem = EGRSystem.getInstance();
//        egrSystem.setVerbose(true);

        egrSystem.generateOriLogs(40_000, true, true);
    }
}
