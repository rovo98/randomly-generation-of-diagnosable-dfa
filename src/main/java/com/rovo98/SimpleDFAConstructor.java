package com.rovo98;

import com.rovo98.diagnosability.Diagnoser;
import com.rovo98.diagnosability.NeotypeDiagnoser;
import com.rovo98.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Naive implementation of interface {@link DFAConstructor} with randomization.
 * <br /><br />
 * More details are documented in README.md file. see first section.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019-12-21
 */
public class SimpleDFAConstructor implements DFAConstructor {

    public static final Logger LOGGER = LoggerFactory.getLogger(SimpleDFAConstructor.class);

    private DFAConfig dfaConfig; // configuration for this DFA constructor.

    // this class can not be instanced outside this class.
    private SimpleDFAConstructor() {}

    // singleton wrapper class.
    private static class SingletonWrapper {
        private static final DFAConstructor INSTANCE = new SimpleDFAConstructor();
    }

    /**
     * Returns the singleton instance of the SimpleDFAConstructor.
     *
     * @return the singleton instance of the {@code SimpleDFAConstructor}.
     */
    public static DFAConstructor getInstance() {
        return SingletonWrapper.INSTANCE;
    }

    /**
     * Initialization is needed before constructing DFA.
     *
     * @param minXSize maximum number of the states
     * @param maxXSize minimum number of the states
     */
    private void initialization(int minXSize, int maxXSize) {
        // TODO: current implementation is less elegant. code refactoring may be needed.
        dfaConfig = new DFAConfig(); // allocates a new config every time.
        Random r = new Random();
        dfaConfig.stateSize = r.nextInt((maxXSize - minXSize) + 1) + minXSize;
        dfaConfig.faultyStateSize = Math.max(dfaConfig.stateSize / 10, 4);

        LOGGER.debug("Generated overall state size and faulty state size: {}, {}",
                dfaConfig.stateSize, dfaConfig.faultyStateSize);

        dfaConfig.states = new int[dfaConfig.stateSize];
        for (int i = 0; i < dfaConfig.stateSize; i++) {
            dfaConfig.states[i] = i;
        }
        // alphabet size: range[10 ~ 20]
        // FIXME: using dynamic design rather than fixed randomization.
        int base = dfaConfig.stateSize > 20 ? 10 : 6;
        int alphabetSize = r.nextInt(11) + base;
        dfaConfig.alphabet = new char[alphabetSize];

        LOGGER.debug("Chosen alphabet size is {}", alphabetSize);

        // randomly fill the alphabet
        int alphabetSpaceLen = dfaConfig.alphabetSpace.length();
        boolean[] tempFlags = new boolean[alphabetSpaceLen];
        int tempIndex;
        for (int i = 0; i < dfaConfig.alphabet.length; i++) {
            tempIndex = r.nextInt(alphabetSpaceLen);
            while (tempFlags[tempIndex]) {
                tempIndex = r.nextInt(alphabetSpaceLen);
            }
            tempFlags[tempIndex] = true;
            dfaConfig.alphabet[i] = dfaConfig.alphabetSpace.charAt(tempIndex);
        }
        // faulty event size (make sure it less than a half of alphabet size)
        int faultEventSize = Math.max(dfaConfig.faultyStateSize / 2, 2);
        faultEventSize = faultEventSize >= 5 ? 4 : faultEventSize;
        if (alphabetSize > 15 && (dfaConfig.faultyStateSize / faultEventSize) > 3) {
            faultEventSize = faultEventSize + r.nextInt(2) + 1;
        }
        dfaConfig.faultyEvents = new int[faultEventSize];

        LOGGER.debug("Chosen faulty event size: {}", faultEventSize);

        boolean[] chosenFaultMarks = new boolean[alphabetSize];
        int choose;
        for (int i = 0; i < faultEventSize; i++) {
            choose = r.nextInt(alphabetSize);
            while (chosenFaultMarks[choose])
                choose = r.nextInt(alphabetSize);
            chosenFaultMarks[choose] = true;
            dfaConfig.faultyEvents[i] = choose;
        }

        LOGGER.debug("Generated alphabet set: {}", Arrays.toString(dfaConfig.alphabet));
        LOGGER.debug("Faulty events (index): {}", Arrays.toString(dfaConfig.faultyEvents));

        Set<Integer> faultyEventIndexSet = new HashSet<>();
        for (int fi : dfaConfig.faultyEvents)
            faultyEventIndexSet.add(fi);

        int ui = 0;
        int oi = 0;
        dfaConfig.observableEvents = new char[alphabetSize - faultEventSize];
        dfaConfig.unobservableEvents = new char[faultEventSize];
        for (int i = 0; i < alphabetSize; i++) {
            if (faultyEventIndexSet.contains(i))
                dfaConfig.unobservableEvents[ui++] = dfaConfig.alphabet[i];
            else
                dfaConfig.observableEvents[oi++] = dfaConfig.alphabet[i];
        }
        LOGGER.debug("selected observable events : {}", Arrays.toString(dfaConfig.observableEvents));
        LOGGER.debug("selected unobservable events : {}", Arrays.toString(dfaConfig.unobservableEvents));
    }

    @Override
    public DFANode constructRandomDFAWithDiagnosability(int minXNum, int maxXNum) {
        return this.constructRandomDFAWithDiagnosability(minXNum, maxXNum, false);
    }

    @Override
    public DFANode constructRandomDFAWithDiagnosability(int minXNum, int maxXNum, boolean saveConfig) {
        DFANode constructed = this.constructRandomDFA(minXNum, maxXNum);
        Diagnoser dfaDiagnoser = NeotypeDiagnoser.getInstance();
        while (!dfaDiagnoser.isDiagnosable(constructed, dfaConfig)) {
            LOGGER.info("Constructed DFA does have diagnosability! dropped.");
            constructed = this.constructRandomDFA(minXNum, maxXNum);
        }
        if (saveConfig)
            saveDFAConfigs(constructed, dfaConfig);
        return constructed;
    }

    @Override
    public DFANode constructRandomDFAExtraNormalWithDiagnosability(int minXNum, int maxXNum) {
        return this.constructRandomDFAExtraNormalWithDiagnosability(minXNum, maxXNum, false);
    }

    @Override
    public DFANode constructRandomDFAExtraNormalWithDiagnosability(int minXNum, int maxXNum, boolean saveConfig) {
        DFANode constructed = this.constructRandomDFAExtraNormal(minXNum, maxXNum);
        Diagnoser dfaDiagnoser = NeotypeDiagnoser.getInstance();
        while (!dfaDiagnoser.isDiagnosable(constructed, dfaConfig)) {
            LOGGER.info("Constructed DFA does have diagnosability! dropped.");
            constructed = this.constructRandomDFAExtraNormal(minXNum, maxXNum);
        }
        if (saveConfig)
            saveDFAConfigs(constructed, dfaConfig);
        return constructed;
    }

    @Override
    public DFANode constructRandomDFA(int minXNum, int maxXNum) {
        return this.constructRandomDFA(minXNum, maxXNum, false);
    }

    @Override
    public DFANode constructRandomDFA(int minXNum, int maxXNum, boolean saveConfig) {
        return this.constructRandomDFAWithExtraNormalComp(minXNum, maxXNum,
                false, saveConfig);
    }

    @Override
    public DFANode constructRandomDFAExtraNormal(int minXNum, int maxXNum) {
        return this.constructRandomDFAExtraNormal(minXNum, maxXNum, false);
    }

    @Override
    public DFANode constructRandomDFAExtraNormal(int minXNum, int maxXNum, boolean saveConfig) {
        return this.constructRandomDFAWithExtraNormalComp(minXNum, maxXNum,
                true, saveConfig);
    }


    // default implementation of the random dfa construction.
    private DFANode constructRandomDFAWithExtraNormalComp(int minXNum, int maxXNum,
                                                          boolean withAnotherNormalComp, boolean saveConfig) {
        // TODO: range checking may be needed or refactoring
        if (minXNum <= 10)
            throw new IllegalArgumentException("Given minXNum should be larger than 10.");
        if (minXNum >= maxXNum)
            throw new IllegalArgumentException("maxXNum must greater than minXNum!");
        LOGGER.info("Do preparation before constructing the DFA...");
        this.initialization(minXNum, maxXNum);
        LOGGER.info("Preparation done.");

        // divides states into normal set and faulty set.
        LOGGER.info("Constructing directed graph components...");
        LOGGER.debug("====>\t Dividing states set....");
        int normalStateRange = dfaConfig.stateSize - dfaConfig.faultyStateSize;

        LOGGER.debug("Divided normal states set : [{}:{})", 0, normalStateRange);

        DFANode normalComponent = constructGraphComponent(dfaConfig.states, 0, normalStateRange, 0);
        LOGGER.debug("====>\t normal component constructed.");
        DFANode[] faultyComponents = new DFANode[dfaConfig.faultyEvents.length];

        int steps = dfaConfig.faultyStateSize / dfaConfig.faultyEvents.length;
        int faultyStart = normalStateRange;
        int faultyEnd = faultyStart + steps;
        for (int i = 0; i < dfaConfig.faultyEvents.length; i++) {
            LOGGER.debug("Divided fault set {}, range: [{}:{})", i, faultyStart, faultyEnd);
            faultyComponents[i] = constructGraphComponent(dfaConfig.states, faultyStart, faultyEnd, 0);
            LOGGER.debug("====>\t faulty component_" + i + " constructed.");
            faultyStart = faultyEnd;
            faultyEnd = faultyEnd + steps;
            if (dfaConfig.stateSize - faultyEnd < steps)
                faultyEnd = dfaConfig.stateSize;
        }
        // connecting normalComponent with other faulty components.
        LOGGER.debug("Composing all the components...");
        int faultySize = dfaConfig.faultyEvents.length;
        for (DFANode component : faultyComponents)
            // FIXME: current implementation: faulty type assignment is reversed for the given faulty events.
            // natural order may be more adaptive.
            connectingNormalCompWithFaultyComp(normalComponent, component, --faultySize);

        if (withAnotherNormalComp) {
            int[] acStates = new int[dfaConfig.stateSize / 2];
            // initialization.
            for (int i = 0; i < acStates.length; i++)
                acStates[i] = i;

            int mapKeyStart = dfaConfig.stateSize;
            LOGGER.debug("Add another normal graph component for every faulty component, extra size: {}",
                    acStates.length);
            LOGGER.debug("Extra range to be added: [{},{})", mapKeyStart, mapKeyStart + acStates.length);
            DFANode anotherNormalComponent = constructGraphComponent(acStates, 0, acStates.length, mapKeyStart);
            LOGGER.debug("====>\t Extra normal graph component constructed.");
            for (DFANode component : faultyComponents)
                connectingFaultyCompWithNormalComp(component, anotherNormalComponent);
        }

        LOGGER.info("DFA constructed.");

        if (saveConfig)
            saveDFAConfigs(normalComponent, dfaConfig);

        return normalComponent;
    }

    /*
    =======================
        HELPER FUNCTIONS
    =======================
     */

    // saving constructed dfa configurations.
    private void saveDFAConfigs(DFANode dfaRoot, DFAConfig dfaConfig) {
        String filename = CommonUtils.generateDefaultDFAName(dfaConfig);
        filename = filename.concat("_config");
        CommonUtils.saveDFAConfigs(filename, dfaRoot, dfaConfig);
    }

    /**
     * Constructing a direct graph component over {@code states} with range of
     * {@code start} to {@code end}.
     *
     * @param states      the given states to constructs dfa graph component.
     * @param start       starting index of the range
     * @param end         ending index of the range.
     * @param mapKeyStart the start index bias of the key in dfaConfig.statesMap.
     * @return Constructed direct graph dfa component.
     */
    private DFANode constructGraphComponent(int[] states, int start, int end, int mapKeyStart) {
        // TODO: index range validation may needed.
        int size = end - start;
        int steps = 0;
        boolean[] visited = new boolean[states.length];
        int rootState = states[start] + mapKeyStart;
        DFANode root = new DFANode(rootState);
        dfaConfig.statesMap.put(rootState, root);

        visited[start] = true;
        steps++;
        // tracking unvisited states.
        int[] unvisitedStates = Arrays.copyOfRange(states, start + 1, end);
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
            int tempNextState = nextState + mapKeyStart;
            if (!dfaConfig.statesMap.containsKey(tempNextState)) {
                DFANode nextNode = new DFANode(tempNextState);
                dfaConfig.statesMap.put(tempNextState, nextNode);
            }
            addRandomTransition(pNode, tempNextState);
            // for every node, attaching it with 2 ~ 4 nodes.
            // including itself. adding more nodes.
            int connections = r.nextInt(3) + 1;
            for (int i = 0; i < connections; i++) {
                // randomly choose one state (can be visited, btw, itself is adapted)
                int ns = r.nextInt(size) + start;
                int tnState = states[ns] + mapKeyStart;
                if (!dfaConfig.statesMap.containsKey(tnState)) {
                    DFANode newNode = new DFANode(tnState);
                    dfaConfig.statesMap.put(tnState, newNode);
                }
                addRandomTransition(pNode, tnState);
            }
            // navigating to one unvisited node.
            Character[] unvisitedTransitionSymbols = getUnvisitedTransitionSymbols(pNode, visited, mapKeyStart);
            int navigatingSymbolIndex = r.nextInt(unvisitedTransitionSymbols.length);
            pNode = pNode.navigate(unvisitedTransitionSymbols[navigatingSymbolIndex], dfaConfig);
            // mark new income state as visited.
            if (!visited[pNode.state - mapKeyStart]) {
                visited[pNode.state - mapKeyStart] = true;
                unvisitedList.remove((Integer) (pNode.state - mapKeyStart));
                steps++;
            }
            // for last visited node. add 1~2 more transitions.
            if (steps == size) {
                int c = r.nextInt(2) + 1;
                for (int i = 0; i < c; i++) {
                    int ns = r.nextInt(size) + start;
                    addRandomTransition(pNode, states[ns] + mapKeyStart);
                }
            }
        }
        return root;
    }

    // Returns symbols set (non leading to current node or visited nodes)
    private static Character[] getUnvisitedTransitionSymbols(DFANode curr, boolean[] visited,
                                                             int mapKeyStart) {
        return curr.transitions.keySet().stream()
                .filter(s ->
                        curr.transitions.get(s) != curr.state && !visited[curr.transitions.get(s) - mapKeyStart]
                ).toArray(Character[]::new);
    }

    // Randomly add transition for the given dfa node.
    private void addRandomTransition(DFANode curr, int nextState) {
        // randomly choose a symbol from observable events set \ transition events.
        // Choose a symbol (event) not in transition table of current node already.
        Random r = new Random();
        int observableEventSize = dfaConfig.observableEvents.length;

        Character[] obs = new Character[observableEventSize];
        for (int i = 0; i < observableEventSize; i++)
            obs[i] = dfaConfig.observableEvents[i];

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
    private void connectingNormalCompWithFaultyComp(DFANode compA, DFANode compB, int faultyMode) {
        int faultyState = compB.state;
        DFANode pNode = compA;

        // traverses several steps, and then adding the faulty transition
        // to the node which we stop at.
        Random r = new Random();
        // FIXME: randomization bound should be a dynamic design here.
        int bound = dfaConfig.stateSize > 20 ? 31 : 21;
        int rt = r.nextInt(bound) + 10; // 10 ~ 30 or 10 ~ 40
        while (rt > 0) {
            // filtering the unobservable events
            Character[] symbols = pNode.transitions.keySet().stream()
                    .filter(s -> {
                        for (char c : dfaConfig.unobservableEvents)
                            if (c == s) return false;
                        return true;
                    }).toArray(Character[]::new);
            char symbol = symbols[r.nextInt(symbols.length)];
            pNode = pNode.navigate(symbol, dfaConfig);
            rt--;
        }
        pNode.addTransition(dfaConfig.unobservableEvents[faultyMode], faultyState);
    }

    // connects the given faulty component with the given normal component ( by adding a observable transition).
    private void connectingFaultyCompWithNormalComp(DFANode faulty, DFANode normal) {
        DFANode pNode = faulty;
        DFANode npNode = normal;

        Random r = new Random();
        // FIXME: A more elegant design should be taken to navigates nodes in faulty component.
        int rt = r.nextInt(5);
        // randomly navigating several times in faulty component.
        while (rt > 0) {
            Character[] symbols = pNode.transitions.keySet().toArray(new Character[0]);
            char symbol = symbols[r.nextInt(symbols.length)];
            pNode = pNode.navigate(symbol, dfaConfig);
            rt--;
        }
        // also randomly navigating several times in normal component.
        int nrt = r.nextInt(10);
        while (nrt > 0) {
            Character[] symbols = npNode.transitions.keySet().toArray(new Character[0]);
            char symbol = symbols[r.nextInt(symbols.length)];
            npNode = npNode.navigate(symbol, dfaConfig);
            nrt--;
        }
        addRandomTransition(pNode, npNode.state);
    }

    // getter.
    @Override
    public DFAConfig getDFAConfig() {
        return dfaConfig;
    }

    /**
     * Driver the program to test the method above.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        DFAConstructor constructor = SimpleDFAConstructor.getInstance();
        DFANode constructedRoot = constructor.constructRandomDFAExtraNormalWithDiagnosability(11, 20);
    }
}
