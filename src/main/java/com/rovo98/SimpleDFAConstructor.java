package com.rovo98;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Naive implementation of interface {@code DFAConstructor} with randomization.
 *
 * More details are documented in README.md file. see first section.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019-12-21
 */
public class SimpleDFAConstructor implements DFAConstructor {

    public static final Logger LOGGER = LoggerFactory.getLogger(SimpleDFAConstructor.class);

    private Config config; // configuration for this DFA constructor.

    // default constructor.
    public SimpleDFAConstructor() {
        this.config = new Config();
    }

    /**
     * Initialization is needed before constructing DFA.
     *
     * @param minXSize maximum number of the states
     * @param maxXSize minimum number of the states
     */
    private void initialization(int minXSize, int maxXSize) {
        Random r = new Random();
        config.stateSize = r.nextInt((maxXSize - minXSize) + 1) + minXSize;
        config.faultyStateSize = config.stateSize / 10;

        LOGGER.debug("Generated overall state size and faulty state size: {}, {}",
                config.stateSize, config.faultyStateSize);

        config.states = new int[config.stateSize];
        for (int i = 0; i < config.stateSize; i++) {
            config.states[i] = i;
        }
        // alphabet size: range[10 ~ 20]
        int alphabetSize = r.nextInt(11) + 10;
        config.alphabet = new char[alphabetSize];

        LOGGER.debug("Chosen alphabet size is {}", alphabetSize);

        // randomly fill the alphabet
        int alphabetSpaceLen = config.alphabetSpace.length();
        boolean[] tempFlags = new boolean[alphabetSpaceLen];
        int tempIndex;
        for (int i = 0; i < config.alphabet.length; i++) {
            tempIndex = r.nextInt(alphabetSpaceLen);
            while (tempFlags[tempIndex]) {
                tempIndex = r.nextInt(alphabetSpaceLen);
            }
            tempFlags[tempIndex] = true;
            config.alphabet[i] = config.alphabetSpace.charAt(tempIndex);
        }
        // faulty event size (make sure less than a half of alphabet size)
        // 4 ~ 6
        int faultSize = config.stateSize / 10;
        faultSize = faultSize > 5 ? 4 : faultSize;
        if (alphabetSize > 15 && (config.faultyStateSize / faultSize) > 3) {
            faultSize = faultSize + r.nextInt(2) + 1;
        }
        config.faultyEvents = new int[faultSize];

        LOGGER.debug("Chosen faulty event size: {}", faultSize);

        boolean[] chosenFaultMarks = new boolean[alphabetSize];
        int choose;
        for (int i = 0; i < faultSize; i++) {
            choose = r.nextInt(alphabetSize);
            while (chosenFaultMarks[choose])
                choose = r.nextInt(alphabetSize);
            chosenFaultMarks[choose] = true;
            config.faultyEvents[i] = choose;
        }

        LOGGER.debug("Generated alphabet set: {}", Arrays.toString(config.alphabet));
        LOGGER.debug("Faulty events (index): {}", Arrays.toString(config.faultyEvents));

        Set<Integer> faultyEventIndexSet = new HashSet<>();
        for (int fi : config.faultyEvents)
            faultyEventIndexSet.add(fi);

        int ui = 0;
        int oi = 0;
        config.observableEvents = new char[alphabetSize - faultSize];
        config.unobservableEvents = new char[faultSize];
        for (int i = 0; i < alphabetSize; i++) {
            if (faultyEventIndexSet.contains(i))
                config.unobservableEvents[ui++] = config.alphabet[i];
            else
                config.observableEvents[oi++] = config.alphabet[i];
        }
        LOGGER.debug("selected observable events : {}", Arrays.toString(config.observableEvents));
        LOGGER.debug("selected unobservable events : {}", Arrays.toString(config.unobservableEvents));
    }

    @Override
    public DFANode constructRandomDFA(int minXNum, int maxXNum) {
        // TODO: range checking may be needed or refactoring
        if (minXNum < 50 || maxXNum > 200) {
            throw new IllegalArgumentException("Given minXNum should be >= 50, maxXNum should be <= 500");
        }
        LOGGER.info("Do preparation before constructing the DFA...");
        initialization(minXNum, maxXNum);
        LOGGER.info("Preparation done.");

        // divides states into normal set and faulty set.
        LOGGER.info("Constructing directed graph components...");
        LOGGER.debug("====>\t Dividing states set....");
        int normalStateRange = config.stateSize - config.faultyStateSize;

        LOGGER.debug("Divided normal states set : [{}:{})", 0, normalStateRange);

        DFANode normalComponent = constructGraphComponent(0, normalStateRange);
        LOGGER.debug("====>\t normal component constructed.");
        DFANode[] faultyComponents = new DFANode[config.faultyEvents.length];

        int steps = config.faultyStateSize / config.faultyEvents.length;
        int faultyStart = normalStateRange;
        int faultyEnd = faultyStart + steps;
        for (int i = 0; i < config.faultyEvents.length; i++) {
            LOGGER.debug("Divided fault set {}, range: [{}:{})", i, faultyStart, faultyEnd);
            faultyComponents[i] = constructGraphComponent(faultyStart, faultyEnd);
            LOGGER.debug("====>\t faulty component_" + i + " constructed.");
            faultyStart = faultyEnd;
            faultyEnd = faultyEnd + steps;
            if (config.stateSize - faultyEnd < steps)
                faultyEnd = config.stateSize;
        }
        // connecting normalComponent with other faulty components.
        LOGGER.debug("Composing all the components...");
        int faultySize = config.faultyEvents.length;
        for (DFANode component : faultyComponents)
            connectingDFAComponent(normalComponent, component, --faultySize);

        LOGGER.info("DFA constructed.");
        return normalComponent;
    }

    /*
    ===================
    HELPER FUNCTIONS
    ===================
     */

    /**
     * Constructing a direct graph component over {@code states} with range of
     * {@code start} to {@code end}.
     *
     * @param start starting index of the range
     * @param end   ending index of the range.
     * @return Constructed direct graph.
     */
    private DFANode constructGraphComponent(int start, int end) {
        // TODO: index range validation may needed.
        int size = end - start;
        int steps = 0;
        boolean[] visited = new boolean[config.stateSize];
        DFANode root = new DFANode(config.states[start]);
        config.statesMap.put(config.states[start], root);

        visited[start] = true;
        steps++;

        // tracking unvisited states.
        int[] unvisitedStates = Arrays.copyOfRange(config.states, start + 1, end);
        List<Integer> unvisitedList = new ArrayList<>();
        for (int state : unvisitedStates)
            unvisitedList.add(state);

        Random r = new Random();
        DFANode pNode = root;
        while (steps < size) {     // stop condition: when all states are visited and do one more iteration.
            // randomly choose another state from unvisited states set.
            int nextXIndex = r.nextInt(unvisitedList.size());
            int nextState = unvisitedList.get(nextXIndex);
            // add new state to Config.statesMap.
            if (!config.statesMap.containsKey(nextState)) {
                DFANode nextNode = new DFANode(nextState);
                config.statesMap.put(nextState, nextNode);
            }
            addRandomTransition(pNode, nextState);
            // for every node, attaching it with 2 ~ 4 nodes.
            // including itself. adding more nodes.
            int connections = r.nextInt(3) + 1;

            for (int i = 0; i < connections; i++) {
                // randomly choose one state (can be visited, btw, itself is adapted)
                int ns = r.nextInt(size) + start;
                if (!config.statesMap.containsKey(config.states[ns])) {
                    DFANode newNode = new DFANode(config.states[ns]);
                    config.statesMap.put(config.states[ns], newNode);
                }
                addRandomTransition(pNode, config.states[ns]);
            }

            // navigating to one unvisited node.
            Character[] unvisitedTransitionSymbols = getUnvisitedTransitionSymbols(pNode, visited);

            int navigatingSymbolIndex = r.nextInt(unvisitedTransitionSymbols.length);
            pNode = pNode.navigate(unvisitedTransitionSymbols[navigatingSymbolIndex], config);

            // mark new income state as visited.
            if (!visited[pNode.state]) {
                visited[pNode.state] = true;
                unvisitedList.remove((Integer) pNode.state);
                steps++;
            }
            // for last visited node. add 1~2 more transitions.
            if (steps == size) {
                int c = r.nextInt(2) + 1;
                for (int i = 0; i < c; i++) {
                    int ns = r.nextInt(size) + start;
                    addRandomTransition(pNode, config.states[ns]);
                }
            }
        }
        return root;
    }

    // Returns symbols set (non leading to current node or visited nodes)
    private static Character[] getUnvisitedTransitionSymbols(DFANode curr, boolean[] visited) {
        return curr.transitions.keySet().stream()
                .filter(s ->
                        curr.transitions.get(s) != curr.state && !visited[curr.transitions.get(s)]
                ).toArray(Character[]::new);
    }

    // Randomly add transition for the given dfa node.
    private void addRandomTransition(DFANode curr, int nextState) {
        // randomly choose a symbol from observable events set \ transition events.
        // Choose a symbol (event) not in transition table of current node already.
        Random r = new Random();
        int observableEventSize = config.observableEvents.length;

        Character[] obs = new Character[observableEventSize];
        for (int i = 0; i < observableEventSize; i++)
            obs[i] = config.observableEvents[i];

        Character[] unattachedSymbols = Arrays.stream(obs)
                .filter(s -> {
                    for (char c : curr.transitions.keySet())
                        if (c == s) return false;
                    return true;
                }).toArray(Character[]::new);

        // if all symbols are attached to current node, skipping it.
        if (unattachedSymbols.length == 0)
            return;

        int nextSymbolIndex = r.nextInt(unattachedSymbols.length);
        char nextSymbol = unattachedSymbols[nextSymbolIndex];
        curr.addTransition(nextSymbol, nextState);
    }

    /**
     * Composing second DFA component into first one.
     *
     * @param compA one DFA component.
     * @param compB anther DFA component.
     */
    private void connectingDFAComponent(DFANode compA, DFANode compB, int faultyMode) {
        int faultyState = compB.state;
        DFANode pNode = compA;

        // traverses several steps, and then adding the faulty transition
        // to the node which we stop at.
        Random r = new Random();
        int rt = r.nextInt(41) + 10; // 10 ~ 50
        while (rt > 0) {
            // filtering the unobservable events
            Character[] symbols = pNode.transitions.keySet().stream()
                    .filter(s -> {
                        for (char c : config.unobservableEvents)
                            if (c == s) return false;
                        return true;
                    }).toArray(Character[]::new);
            char symbol = symbols[r.nextInt(symbols.length)];
            pNode = pNode.navigate(symbol, config);
            rt--;
        }
        pNode.addTransition(config.unobservableEvents[faultyMode], faultyState);
    }

    // getter.
    public Config getConfig() {
        return config;
    }

    /**
     * Driver the program to test the method above.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        SimpleDFAConstructor constructor = new SimpleDFAConstructor();
        constructor.constructRandomDFA(50, 100);

        System.out.println(constructor.config.statesMap.get(0).state);
    }
}
