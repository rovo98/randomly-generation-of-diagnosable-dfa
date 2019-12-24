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

    @Override
    public DFANode constructRandomDFA(int minXNum, int maxXNum) {
        // TODO: range checking may be needed or refactoring
        if (minXNum < 50 || maxXNum > 500) {
            throw new IllegalArgumentException("Given minXNum should be >= 50, maxXNum should be <= 500");
        }
        LOGGER.info("Do preparation before constructing the DFA...");
        Config.initialization(minXNum, maxXNum);
        LOGGER.info("Preparation done.");

        // divides states into normal set and faulty set.
        LOGGER.info("Constructing directed graph components...");
        LOGGER.debug("====>\t Dividing states set....");
        int normalStateRange = Config.stateSize - Config.faultyStateSize;

        LOGGER.debug("Divided normal states set : [{}:{})", 0, normalStateRange);

        DFANode normalComponent = constructGraphComponent(0, normalStateRange);
        LOGGER.debug("====>\t normal component constructed.");
        DFANode[] faultyComponents = new DFANode[Config.faultyEvents.length];

        int steps = Config.faultyStateSize / Config.faultyEvents.length;
        int faultyStart = normalStateRange;
        int faultyEnd = faultyStart + steps;
        for (int i = 0; i < Config.faultyEvents.length; i++) {
            LOGGER.debug("Divided fault set {}, range: [{}:{})", i, faultyStart, faultyEnd);
            faultyComponents[i] = constructGraphComponent(faultyStart, faultyEnd);
            LOGGER.debug("====>\t faulty component_" + i + " constructed.");
            faultyStart = faultyEnd;
            faultyEnd = faultyEnd + steps;
            if (Config.stateSize - faultyEnd < steps)
                faultyEnd = Config.stateSize;
        }
        // connecting normalComponent with other faulty components.
        LOGGER.debug("Composing all the components...");
        int faultySize = Config.faultyEvents.length;
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
        boolean[] visited = new boolean[Config.stateSize];
        DFANode root = new DFANode(Config.states[start]);
        Config.statesMap.put(Config.states[start], root);

        visited[start] = true;
        steps++;

        // tracking unvisited states.
        int[] unvisitedStates = Arrays.copyOfRange(Config.states, start + 1, end);
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
            if (!Config.statesMap.containsKey(nextState)) {
                DFANode nextNode = new DFANode(nextState);
                Config.statesMap.put(nextState, nextNode);
            }
            addRandomTransition(pNode, nextState);
            // for every node, attaching it with 2 ~ 4 nodes.
            // including itself. adding more nodes.
            int connections = r.nextInt(3) + 1;

            for (int i = 0; i < connections; i++) {
                // randomly choose one state (can be visited, btw, itself is adapted)
                int ns = r.nextInt(size) + start;
                if (!Config.statesMap.containsKey(Config.states[ns])) {
                    DFANode newNode = new DFANode(Config.states[ns]);
                    Config.statesMap.put(Config.states[ns], newNode);
                }
                addRandomTransition(pNode, Config.states[ns]);
            }

            // navigating to one unvisited node.
            Character[] unvisitedTransitionSymbols = getUnvisitedTransitionSymbols(pNode, visited);

            int navigatingSymbolIndex = r.nextInt(unvisitedTransitionSymbols.length);
            pNode = pNode.navigate(unvisitedTransitionSymbols[navigatingSymbolIndex]);

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
                    addRandomTransition(pNode, Config.states[ns]);
                }
            }
        }
        return root;
    }

    // Returns symbols set (non leading to current node or visited nodes.
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
        int observableEventSize = Config.observableEvents.length;

        Character[] obs = new Character[observableEventSize];
        for (int i = 0; i < observableEventSize; i++)
            obs[i] = Config.observableEvents[i];

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
        int rt = r.nextInt(21) + 10; // 10 ~ 30
        while (rt > 0) {
            // filtering the unobservable events
            Character[] symbols = pNode.transitions.keySet().stream()
                    .filter(s -> {
                        for (char c : Config.unobservableEvents)
                            if (c == s) return false;
                        return true;
                    }).toArray(Character[]::new);
            char symbol = symbols[r.nextInt(symbols.length)];
            pNode = pNode.navigate(symbol);
            rt--;
        }
        pNode.addTransition(Config.unobservableEvents[faultyMode], faultyState);
    }

    /**
     * Driver the program to test the method above.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        new SimpleDFAConstructor().constructRandomDFA(50, 100);

        System.out.println(Config.statesMap.get(0).state);
    }
}
