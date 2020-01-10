package com.rovo98.diagnosability;

import com.rovo98.DFAConfig;
import com.rovo98.DFANode;
import com.rovo98.exceptions.TransitionNotFound;
import com.rovo98.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Naive implementation of the Diagnosability of DES Testing approach proposed by jiang in his paper
 * 'A Polynomial Algorithm for Testing Diagnosability of Discrete-Event Systems.
 * <br />
 * <br />
 * Compared to traditional method {@link TraditionalDiagnoser}, this approach tests the diagnosabilty of the given dfa
 * without constructing a diagnoser.
 * <br />
 * <br />
 * More details, see jiang's paper - <a href="https://ieeexplore.ieee.org/document/940942">
 * A Polynomial Algorithm for Testing Diagnosability of Discrete-Event Systems</a>}
 * <br />
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.27
 */
public class NeotypeDiagnoser implements Diagnoser {

    public static final Logger LOGGER = LoggerFactory.getLogger(NeotypeDiagnoser.class);
    // stores all observer nodes for checking diagnosability
    // using observer node's identical key as key.
    private Map<String, NDDFANode> ndDfaNodeMap;
    // stores all composite nodes for checking diagnosability
    // using composite node's identical key as key.
    private Map<String, CompositeNode> compositeNodeMap;

    // this class can not be instanced outside this class.
    private NeotypeDiagnoser() {
        // initialization.
        this.ndDfaNodeMap = new HashMap<>();
        this.compositeNodeMap = new HashMap<>();
    }

    // singleton wrapper.
    private static class SingletonWrapper {
        private static final Diagnoser INSTANCE = new NeotypeDiagnoser();
    }

    /**
     * Returns the singleton instance of the NeotypeDiagnoser.
     *
     * @return the singleton instance of the {@code NeotypeDiagnoser}.
     */
    public static Diagnoser getInstance() {
        return SingletonWrapper.INSTANCE;
    }

    @Override
    public boolean isDiagnosable(DFANode root, DFAConfig dfaConfig) {
        // TODO: basic checking for the given dfa may needed.
        // clear the node maps at every startup.
        this.ndDfaNodeMap.clear();
        this.compositeNodeMap.clear();

        // 1. obtains observer for the given dfa first.
        NDDFANode observerRoot = this.constructNdDfaObserver(root, dfaConfig);
        // 2. computes the composition of the constructed observer with observer itself.
        this.computeComposition(observerRoot);
        // 3. cycle checking to see whether the given dfa is diagnosable or not.
        List<CompositeNode> targets = compositeNodeMap.values().stream()
                .filter(cn -> !cn.firstFailureType.equals(cn.secondFailureType))
                .collect(Collectors.toList());
        // modification of the algorithm for detecting cycle in directed graph.
        Set<String> visitedKeys = new HashSet<>(compositeNodeMap.size());
        Set<String> recStack = new HashSet<>(compositeNodeMap.size());
        // for debug usage only.
        System.out.println("=======================================================\n");
        System.out.println("\t\tDEBUG usage only... (Cycle Testing)");
        for (CompositeNode cn : targets) {
            // reset visited and recursion stack for every node checking.
            visitedKeys.clear();
            recStack.clear();

            System.out.print(cn.firstState + "," + cn.firstFailureType + ":" + cn.secondState + "," +
                    cn.secondFailureType);
            if (existCycle(cn, cn, visitedKeys, recStack))
                System.out.println(": existed.\n");
            else
                System.out.println(": not existed.\n");
        }
        System.out.println("=======================================================\n");
        for (CompositeNode cn : targets) {
            if (existCycle(cn, cn, visitedKeys, recStack)) {
                LOGGER.debug("Current dfa is not diagnosable!");
                return false;
            }
        }
        LOGGER.debug("Current dfa is diagnosable!");
        return true;
    }

    /*
    =========================
        HELPER FUNCTIONS
    =========================
     */
    // return true if there exists a cycle starting from the given node.
    private boolean existCycle(CompositeNode root, CompositeNode node,
                               Set<String> visitedKeys, Set<String> recStack) {
        // mark the current node as visited and part of the recursion stack.
        if (recStack.contains(CommonUtils.getCompositeNodeIdenticalKey(node))) {
            // if the node the back edge navigating to is the root node.
            // there exists a cycle starting from the given root node
            // and back to the root node.
            // print, for debug usage only.
            System.out.println("\n" + CommonUtils.getCompositeNodeIdenticalKey(root) + ":" +
                    CommonUtils.getCompositeNodeIdenticalKey(node));
            if (isSameState(root, node))
                return true;
        }
        if (visitedKeys.contains(CommonUtils.getCompositeNodeIdenticalKey(node)))
            return false;

        visitedKeys.add(CommonUtils.getCompositeNodeIdenticalKey(node));
        recStack.add(CommonUtils.getCompositeNodeIdenticalKey(node));

        List<CompositeNode> children = node.getTransitions().stream()
                .map(t -> compositeNodeMap.get(t.nextKey))
                .collect(Collectors.toList());
        for (CompositeNode n : children)
            if (existCycle(root, n, visitedKeys, recStack))
                return true;

        recStack.remove(CommonUtils.getCompositeNodeIdenticalKey(node));
        return false;
    }

    // returns true if the given two composite node have same states and failure types.
    private boolean isSameState(CompositeNode f, CompositeNode s) {
        return f.firstState == s.firstState && f.secondState == s.secondState &&
                f.firstFailureType.equals(s.firstFailureType) &&
                f.secondFailureType.equals(s.secondFailureType);
    }

    private NDDFANode constructNdDfaObserver(DFANode root, DFAConfig dfaConfig) {
        // initialization
        int numOfFaultyTypes = dfaConfig.getFaultyEvents().length;
        String[] failureTypes = new String[numOfFaultyTypes + 1];
        failureTypes[0] = "N";
        for (int i = 1; i < failureTypes.length; i++)
            failureTypes[i] = "F" + i;

        //states whether the observer node is visited or not.
        int visitedStateSize = dfaConfig.getStateSize() * 2; // allocating a bit more space.
        boolean[] visited = new boolean[visitedStateSize];
        int visitedCount = dfaConfig.getStateSize();

        NDDFANode observerRoot = new NDDFANode(root.getState(), failureTypes[0]);
        // add new node to observerNodeMap
        ndDfaNodeMap.put(CommonUtils.getObserverNodeIdenticalKey(observerRoot), observerRoot);
        // iterates all the states in the given dfa.
        Deque<NDDFANode> queue = new ArrayDeque<>(); // bfs approach: stores all unvisited observer nodes.
        queue.offer(observerRoot);
        while (visitedCount > 0 && !queue.isEmpty()) {
            NDDFANode poNode = queue.poll();
            DFANode pNode = dfaConfig.getStatesMap().get(poNode.state);

            visited[poNode.state] = true; // mark current node as visited.
            Character[] currSymbols = pNode.getTransitions().keySet().toArray(new Character[0]);
            for (char s : currSymbols) {
                int faulty = isFaultyEvent(s, dfaConfig);
                DFANode nextDFANode = pNode.navigate(s, dfaConfig);
                // deal with faulty event label.
                if (faulty > 0) {
                    Character[] nextSymbols = nextDFANode.getTransitions().keySet().toArray(new Character[0]);
                    for (char ns : nextSymbols) {
                        DFANode fnNode = nextDFANode.navigate(ns, dfaConfig);
                        poNode.addTransition(ns,
                                addNewNdDfaNodeToMap(fnNode.getState(), failureTypes[faulty]));
                    }
                } else {
                    // add a new transition to current observer node.
                    poNode.addTransition(s,
                            addNewNdDfaNodeToMap(nextDFANode.getState(), poNode.failureType));
                }
            }
            queue.addAll(getUnvisitedNextNdNodes(poNode, visited, queue));
            visitedCount--;
        }
        return observerRoot;
    }

    // add new nd-dfa node to map, and returns the identical key for the added node.
    private String addNewNdDfaNodeToMap(int state, String failureType) {
        String ikey = CommonUtils.getObserverNodeIdenticalKey(state, failureType);
        // add new observer node is it not in observerNodeMap.
        if (!ndDfaNodeMap.containsKey(ikey)) {
            NDDFANode newNode = new NDDFANode(state, failureType);
            ndDfaNodeMap.put(ikey, newNode);
        }
        return ikey;
    }

    // returns the faulty type index if the given symbol is faulty
    // otherwise negative number -1 is returned.
    private int isFaultyEvent(char symbol, DFAConfig dfaConfig) {
        int faultySize = dfaConfig.getFaultyEvents().length;
        for (int i = 0; i < faultySize; i++) {
            if (dfaConfig.getUnobservableEvents()[i] == symbol)
                return faultySize - i;   // since the original faulty types is reversed.
        }
        return -1;
    }

    // returns unvisited next nd-dfa nodes for the given node.
    private List<NDDFANode> getUnvisitedNextNdNodes(NDDFANode curr, boolean[] visited,
                                                    Deque<NDDFANode> targetQueue) {
        return curr.transitions.stream()
                .map(t -> ndDfaNodeMap.get(t.getNextKey()))
                .filter(n -> !visited[n.state] && !targetQueue.contains(n))
                .distinct()
                .collect(Collectors.toList());
    }

    private void computeComposition(NDDFANode root) {
        CompositeNode compositeRoot = new CompositeNode(root.state, root.failureType,
                root.state, root.failureType);
        compositeNodeMap.put(CommonUtils.getCompositeNodeIdenticalKey(compositeRoot), compositeRoot);

        Set<String> keysOfVisitedCompositedNodes = new HashSet<>();

        Deque<CompositeNode> queue = new ArrayDeque<>();
        queue.offer(compositeRoot);

        while (!queue.isEmpty()) {
            CompositeNode pcNode = queue.poll();
            // mark current node as visited.
            keysOfVisitedCompositedNodes.add(CommonUtils.getCompositeNodeIdenticalKey(pcNode));
            NDDFANode firstNdNode = ndDfaNodeMap.get(
                    CommonUtils.getObserverNodeIdenticalKey(pcNode.firstState, pcNode.firstFailureType));
            NDDFANode secondNdNode = ndDfaNodeMap.get(
                    CommonUtils.getObserverNodeIdenticalKey(pcNode.secondState, pcNode.secondFailureType));

            // if two nd-dfa node of the composition node have the same states and failure types.
            if (isSameState(firstNdNode, secondNdNode)) {
                Character[] events = firstNdNode.getTransitions().stream()
                        .map(t -> t.symbol)
                        .distinct()
                        .toArray(Character[]::new);
                for (Character s : events) {
                    NDDFANode[] nextNdNodes = firstNdNode.getTransitions().stream()
                            .filter(t -> t.symbol == s)
                            .map(t -> ndDfaNodeMap.get(t.nextKey))
                            .toArray(NDDFANode[]::new);

                    for (NDDFANode outerNdNode : nextNdNodes) {
                        for (NDDFANode innerNdNode : nextNdNodes) {
                            pcNode.addTransition(s,
                                    addNewCompositeNodeToMap(outerNdNode.state, outerNdNode.failureType,
                                            innerNdNode.state, innerNdNode.failureType));
                        }
                    }
                }
            } else {
                // obtains common event labels of two nd-dfa node.
                List<Character> fEvents = firstNdNode.getTransitions().stream()
                        .map(t -> t.symbol)
                        .distinct()
                        .collect(Collectors.toList());
                List<Character> sEvents = secondNdNode.getTransitions().stream()
                        .map(t -> t.symbol)
                        .distinct()
                        .collect(Collectors.toList());
                fEvents.retainAll(sEvents);
                for (Character s : fEvents) {
                    NDDFANode fNextNdNode = firstNdNode.getTransitions().stream()
                            .filter(t -> t.symbol == s)
                            .map(t -> ndDfaNodeMap.get(t.nextKey))
                            .findFirst()
                            .orElseThrow(() -> new TransitionNotFound("target transition not found! symbol: " + s));
                    NDDFANode sNextNdNode = secondNdNode.getTransitions().stream()
                            .filter(t -> t.symbol == s)
                            .map(t -> ndDfaNodeMap.get(t.nextKey))
                            .findFirst()
                            .orElseThrow(() -> new TransitionNotFound("target transition not found! symbol: " + s));
                    // add transition to current Composition node.
                    pcNode.addTransition(s,
                            addNewCompositeNodeToMap(fNextNdNode.state, fNextNdNode.failureType,
                                    sNextNdNode.state, sNextNdNode.failureType));
                }
            }
            // add next unvisited nodes of current composite node to queue.(FIFO)
            queue.addAll(getUnvisitedNextCompNodes(pcNode, keysOfVisitedCompositedNodes));
        }
    }

    // add new composite node to map, and then returns the identical key for the added node.
    private String addNewCompositeNodeToMap(int fState, String fFailureType, int sState, String sFailureType) {
        String ikey = CommonUtils.getCompositeNodeIdenticalKey(fState, fFailureType, sState, sFailureType);
        if (!compositeNodeMap.containsKey(ikey)) {
            CompositeNode newCompositeNode = new CompositeNode(fState, fFailureType, sState, sFailureType);
            compositeNodeMap.put(ikey, newCompositeNode);
        }
        return ikey;
    }

    // return true if the given two nd-dfa node have same states and failure types.
    private boolean isSameState(NDDFANode f, NDDFANode s) {
        return f.state == s.state && f.failureType.equals(s.failureType);
    }

    // returns the next unvisited composited nodes for the given curr node.
    private List<CompositeNode> getUnvisitedNextCompNodes(CompositeNode curr,
                                                          Set<String> keysOfVisitedCompositedNodes) {
        return curr.getTransitions().stream()
                .filter(t -> !keysOfVisitedCompositedNodes.contains(t.nextKey))
                .map(t -> compositeNodeMap.get(t.nextKey))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Driver the program to test the methods above.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-01-06 10:48:57_czEzOmZzNDphczc6ZmVzMg==_config");
        Optional<Object[]> loaded = CommonUtils
                .loadDFAConfigs("2020-01-06 10:47:30_czE2OmZzNDphczg6ZmVzMg==_config");
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-01-06 10:48:17_czE4OmZzNDphczE0OmZlczI=_config");
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-01-09 22:54:38_czE4OmZzNDphczE2OmZlczI=_config");
        if (loaded.isPresent()) {
            Object[] res = loaded.get();

            DFANode testRootNode = (DFANode) res[0];
            DFAConfig testDfaConfig = (DFAConfig) res[1];
            Diagnoser dfaDiagnoser = NeotypeDiagnoser.getInstance();
            System.out.println("is diagnosable? -> " + dfaDiagnoser.isDiagnosable(testRootNode, testDfaConfig));
        }
    }
}
