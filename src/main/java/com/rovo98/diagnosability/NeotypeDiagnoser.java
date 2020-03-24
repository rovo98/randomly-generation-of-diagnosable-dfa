package com.rovo98.diagnosability;

import com.rovo98.DFAConfig;
import com.rovo98.DFANode;
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
 * <br />
 * Two types of implementations is given in this class, the single faulty mode one and its generalization
 * <br />
 * NOTICE:
 * <li>1. single faulty mode implementation can only handle dfa with single faulty mode.</li>
 * <li>2. multi-faulty mode implementation (a generalization of single faulty mode)
 * can handle both dfa with single faulty mode and dfa with multi-faulty mode.</li>
 *
 * <br /><strong>By default</strong>, when the dfa with single faulty mode is given, using
 * the single faulty mode implementation is preferred. Otherwise the multi-faulty mode implementation is used.
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

    // for multi-faulty mode.
    private Map<String, MultiFaultyNDDFANode> multiFaultyNDDFANodeMap;
    private Map<String, MultiFaultyCompositeNode> multiFaultyCompositeNodeMap;

    // this class can not be instanced outside this class.
    private NeotypeDiagnoser() {
        // initialization.
        this.ndDfaNodeMap = new HashMap<>();
        this.compositeNodeMap = new HashMap<>();
        this.multiFaultyNDDFANodeMap = new HashMap<>();
        this.multiFaultyCompositeNodeMap = new HashMap<>();
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
        if (dfaConfig.isMultiFaulty()) {
            LOGGER.debug("Multi-faulty mode (generalization) implementation is used.");
            return isDiagnosableMultiFaulty(root, dfaConfig);
        }
        LOGGER.debug("Single-faulty mode implementation is used.");
        return isDiagnosableSingleFaulty(root, dfaConfig);
    }

    // approach to test diagnosability for the constructed dfa with single faulty mode.
    // returns true if the given constructed dfa is diagnosable; otherwise false.
    private boolean isDiagnosableSingleFaulty(DFANode dfaRoot, DFAConfig dfaConfig) {
        // clear the node maps before testing diagnosability of the constructed dfa.
        this.ndDfaNodeMap.clear();
        this.compositeNodeMap.clear();
        // 1. obtains observer for the given dfa first.
        NDDFANode observerRoot = this.constructNdDfaObserver(dfaRoot, dfaConfig);
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
        System.out.println("\t\tDEBUG usage only... (Cycle Testing), target nodes size: " + targets.size());
//        for (CompositeNode cn : targets) {
//            // reset visited and recursion stack for every node checking.
//            visitedKeys.clear();
//            recStack.clear();
//
//            System.out.print(cn.firstState + "," + cn.firstFailureType + ":" + cn.secondState + "," +
//                    cn.secondFailureType);
//            if (existCycle(cn, cn, visitedKeys, recStack))
//                System.out.println(": existed.\n");
//            else
//                System.out.println(": not existed.\n");
//        }
        System.out.println("=======================================================\n");
        for (CompositeNode cn : targets) {
            // reset visited and recursion stack for every node checking.
            visitedKeys.clear();
            recStack.clear();
            if (existCycle(cn, cn, visitedKeys, recStack)) {
                LOGGER.debug("Current dfa is not diagnosable!");
                return false;
            }
        }
        LOGGER.debug("Current dfa is diagnosable!");

        return true;
    }

    // Approach to test diagnosability for the constructed dfa with multi-faulty mode.
    // REMARKS: This method is a generalization of the single faulty version.
    // So it can also handle the DFAs with single faulty mode.
    // returns true if the given constructed dfa is diagnosable; otherwise false.
    private boolean isDiagnosableMultiFaulty(DFANode dfaRoot, DFAConfig dfaConfig) {
        // clear used node maps before testing the diagnosability of the given dfa.
        this.multiFaultyNDDFANodeMap.clear();
        this.multiFaultyCompositeNodeMap.clear();
        // 1. obtains a nondeterministic finite machine for the given dfa first.
        MultiFaultyNDDFANode mnfRoot = this.constructNdDfaObserverMultiFaulty(dfaRoot, dfaConfig);
        // 2. computes the product composition of the machine got above.
        this.computeCompositionMultiFaulty(mnfRoot);
        // 3. checking whether there is exists a cycle starting from a composited node whose label
        // are not the same.
        List<MultiFaultyCompositeNode> targets = multiFaultyCompositeNodeMap.values().stream()
                .filter(node -> !isSameFailureTypes(node.firstFailureTypes, node.secondFailureTypes))
                .collect(Collectors.toList());

        // modification of the algorithm for detecting cycle in directed graph.
        Set<String> visitedKeys = new HashSet<>(multiFaultyCompositeNodeMap.size());
        Set<String> recStack = new HashSet<>(multiFaultyCompositeNodeMap.size());
        // for debug usage only.
        System.out.println("=======================================================\n");
        System.out.println("\t\tDEBUG usage only... (Cycle Testing)");
        System.out.println("target nodes size: " + targets.size());
//        for (MultiFaultyCompositeNode cn : targets) {
//            // reset visited and recursion stack for every node checking.
//            visitedKeys.clear();
//            recStack.clear();
//
//            System.out.print(cn.firstState + "," + cn.firstFailureTypes + ":" + cn.secondState + "," +
//                    cn.secondFailureTypes);
//            if (existCycle(cn, cn, visitedKeys, recStack))
//                System.out.println(": existed.\n");
//            else
//                System.out.println(": not existed.\n");
//        }
        System.out.println("=======================================================\n");
        for (MultiFaultyCompositeNode cn : targets) {
            // reset visited and recursion stack for every node checking.
            visitedKeys.clear();
            recStack.clear();
            if (existCycle(cn, cn, visitedKeys, recStack)) {
                System.out.println("first detected node which exists cycle:" +
                        cn.firstState + "," + cn.firstFailureTypes + ":" +
                        cn.secondState + "," + cn.secondFailureTypes);
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

        List<CompositeNode> children = node.transitions.stream()
                .map(t -> compositeNodeMap.get(t.nextKey))
                .collect(Collectors.toList());
        for (CompositeNode n : children)
            if (existCycle(root, n, visitedKeys, recStack))
                return true;

        recStack.remove(CommonUtils.getCompositeNodeIdenticalKey(node));
        return false;
    }

    // return true if there exists a cycle starting from the given node.
    // A modification of the implementation for single faulty mode.
    private boolean existCycle(MultiFaultyCompositeNode root, MultiFaultyCompositeNode node,
                               Set<String> visitedKeys, Set<String> recStack) {
        if (recStack.contains(CommonUtils.getCompositeNodeIdenticalKeyMultiFaulty(node))) {
            if (isSameState(root, node))
                return true;
        }
        if (visitedKeys.contains(CommonUtils.getCompositeNodeIdenticalKeyMultiFaulty(node)))
            return false;

        visitedKeys.add(CommonUtils.getCompositeNodeIdenticalKeyMultiFaulty(node));
        recStack.add(CommonUtils.getCompositeNodeIdenticalKeyMultiFaulty(node));

        List<MultiFaultyCompositeNode> children = node.transitions.stream()
                .map(t -> multiFaultyCompositeNodeMap.get(t.nextKey))
                .collect(Collectors.toList());
        for (MultiFaultyCompositeNode n : children)
            if (existCycle(root, n, visitedKeys, recStack))
                return true;

        recStack.remove(CommonUtils.getCompositeNodeIdenticalKeyMultiFaulty(node));
        return false;
    }

    // returns true if the given two composite node have same states and failure types.
    private boolean isSameState(CompositeNode f, CompositeNode s) {
        return f.firstState == s.firstState && f.secondState == s.secondState &&
                f.firstFailureType.equals(s.firstFailureType) &&
                f.secondFailureType.equals(s.secondFailureType);
    }

    // returns true if the given two composited node have same states and failure types.
    private boolean isSameState(MultiFaultyCompositeNode f, MultiFaultyCompositeNode s) {
        return f.firstState == s.firstState && f.secondState == s.secondState &&
                isSameFailureTypes(f.firstFailureTypes, s.firstFailureTypes) &&
                isSameFailureTypes(f.secondFailureTypes, s.secondFailureTypes);
    }

    // obtains a nondeterministic finite machine for the given dfa.
    // REMARKS: this method only used for single faulty mode.
    private NDDFANode constructNdDfaObserver(DFANode root, DFAConfig dfaConfig) {
        // initialization
        String[] failureTypes = getFailureTypes(dfaConfig);

        //states whether the observer node is visited or not.
        Set<String> keysOfVisitedNdNodes = new HashSet<>();

        NDDFANode observerRoot = new NDDFANode(root.getState(), failureTypes[0]);
        // add new node to observerNodeMap
        ndDfaNodeMap.put(CommonUtils.getObserverNodeIdenticalKey(observerRoot), observerRoot);
        // iterates all the states in the given dfa.
        Deque<NDDFANode> queue = new ArrayDeque<>(); // bfs approach: stores all unvisited observer nodes.
        queue.offer(observerRoot);
        while (!queue.isEmpty()) {
            NDDFANode poNode = queue.poll();
            DFANode pNode = dfaConfig.getStatesMap().get(poNode.state);
            // mark current node as visited.
            keysOfVisitedNdNodes.add(CommonUtils.getObserverNodeIdenticalKey(poNode));
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
            queue.addAll(getUnvisitedNextNdNodes(poNode, keysOfVisitedNdNodes, queue));
        }
        return observerRoot;
    }

    // returns all the failure types array.
    private String[] getFailureTypes(DFAConfig dfaConfig) {
        int numOfFaultyTypes = dfaConfig.getFaultyEvents().length;
        String[] failureTypes = new String[numOfFaultyTypes + 1];
        failureTypes[0] = "N";
        for (int i = 1; i < failureTypes.length; i++)
            failureTypes[i] = "F" + i;
        return failureTypes;
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
                return i + 1;   // since a normal type is at index 0 in failureTypes array.
        }
        return -1;
    }

    // returns unvisited next nd-dfa nodes for the given node.
    private List<NDDFANode> getUnvisitedNextNdNodes(NDDFANode curr,
                                                    Set<String> keysOfVisitedNdNodes,
                                                    Deque<NDDFANode> targetQueue) {
        return curr.transitions.stream()
                .map(t -> ndDfaNodeMap.get(t.nextKey))
                .filter(n -> !keysOfVisitedNdNodes.contains(CommonUtils.getObserverNodeIdenticalKey(n)) &&
                        !targetQueue.contains(n))
                .distinct()
                .collect(Collectors.toList());
    }

    // compute the product composition for the given two same nd-observer.
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
            List<Character> fEvents = getEventSymbols(firstNdNode);
            if (isSameState(firstNdNode, secondNdNode)) {
                for (char s : fEvents) {
                    NDDFANode[] nextNdNodes = getNextNDDFNodes(firstNdNode, s);
                    for (NDDFANode outerNdNode : nextNdNodes)
                        for (NDDFANode innerNdNode : nextNdNodes)
                            pcNode.addTransition(s,
                                    addNewCompositeNodeToMap(outerNdNode.state, outerNdNode.failureType,
                                            innerNdNode.state, innerNdNode.failureType));
                }
            } else {
                // obtains common event labels of two nd-dfa node.
                List<Character> sEvents = getEventSymbols(secondNdNode);
                fEvents.retainAll(sEvents);
                for (char s : fEvents) {
                    NDDFANode[] fNextNdNodes = getNextNDDFNodes(firstNdNode, s);
                    NDDFANode[] sNextNdNodes = getNextNDDFNodes(secondNdNode, s);
                    // add transition to current Composition node.
                    for (NDDFANode outer : fNextNdNodes)
                        for (NDDFANode inner : sNextNdNodes)
                            pcNode.addTransition(s,
                                    addNewCompositeNodeToMap(outer.state, outer.failureType,
                                            inner.state, inner.failureType));
                }
            }
            // add next unvisited nodes of current composite node to queue.(FIFO)
            queue.addAll(getUnvisitedNextCompNodes(pcNode, keysOfVisitedCompositedNodes));
        }
    }

    // returns a list of events of the given node.
    private List<Character> getEventSymbols(NDDFANode node) {
        return node.transitions.stream()
                .map(t -> t.symbol)
                .distinct()
                .collect(Collectors.toList());
    }

    // returns an array of the next nd-observer nodes of the given nd-observer node curr.
    private NDDFANode[] getNextNDDFNodes(NDDFANode curr, char symbol) {
        return curr.transitions.stream()
                .filter(t -> t.symbol == symbol)
                .map(t -> ndDfaNodeMap.get(t.nextKey))
                .toArray(NDDFANode[]::new);
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
        return curr.transitions.stream()
                .filter(t -> !keysOfVisitedCompositedNodes.contains(t.nextKey))
                .map(t -> compositeNodeMap.get(t.nextKey))
                .distinct()
                .collect(Collectors.toList());
    }

    // A modification of the implementation of the single faulty mode one.
    private MultiFaultyNDDFANode constructNdDfaObserverMultiFaulty(DFANode dfaRoot, DFAConfig dfaConfig) {
        // initialization
        String[] failureTypes = getFailureTypes(dfaConfig);

        // states whether the observer node is visited or not.
        Set<String> keysOfVisitedNdNodes = new HashSet<>();

        // constructing the root node.
        MultiFaultyNDDFANode mfNdRoot = new MultiFaultyNDDFANode(dfaRoot.getState());
        mfNdRoot.addFailureType(failureTypes[0]);
        multiFaultyNDDFANodeMap.put(CommonUtils.getObserverNodeIdenticalKeyMultiFaulty(mfNdRoot), mfNdRoot);

        // bfs traversals the given constructed dfa.
        Deque<MultiFaultyNDDFANode> queue = new ArrayDeque<>();
        queue.offer(mfNdRoot);

        while (!queue.isEmpty()) {
            MultiFaultyNDDFANode mnpDNode = queue.poll();
            DFANode pNode = dfaConfig.getStatesMap().get(mnpDNode.state);
            // mark current as visited.
            keysOfVisitedNdNodes.add(CommonUtils.getObserverNodeIdenticalKeyMultiFaulty(mnpDNode));
            List<String> currFailureTypes = new ArrayList<>(mnpDNode.failureTypes);
            applyingTransitions(mnpDNode, pNode, dfaConfig, failureTypes, currFailureTypes);

            queue.addAll(getUnvisitedNextNdNodesMultiFaulty(mnpDNode, keysOfVisitedNdNodes, queue));
        }

        return mfNdRoot;
    }

    // recursively applying transitions with the failure types for the given multi-faulty nd-observer node.
    private void applyingTransitions(MultiFaultyNDDFANode curr, DFANode pNode,
                                     DFAConfig dfaConfig, String[] failureTypes,
                                     List<String> currFailureTypes) {
        Character[] symbols = pNode.getTransitions().keySet().toArray(new Character[0]);
        for (char s : symbols) {
            int faulty = isFaultyEvent(s, dfaConfig);
            DFANode nextDFANode = pNode.navigate(s, dfaConfig);
            if (faulty > 0) {
                // add faulty types.
                currFailureTypes.remove(failureTypes[0]);
                currFailureTypes.add(failureTypes[faulty]);
                applyingTransitions(curr, nextDFANode, dfaConfig, failureTypes, currFailureTypes);
                // rollback failureTypes since other states does not contains this faulty event.
                currFailureTypes.remove(failureTypes[faulty]);
                if (currFailureTypes.isEmpty())
                    currFailureTypes.addAll(curr.failureTypes);
            } else {
                curr.addTransition(s,
                        addNewNdDfaNodeToMapMultiFaulty(nextDFANode.getState(), currFailureTypes));
            }
        }
    }

    // Add new multi-faulty nd-observer node to map, and returns the identical key for
    // the added node.
    private String addNewNdDfaNodeToMapMultiFaulty(int state, List<String> failureTypes) {
        List<String> failureTypeTobeAdded = failureTypes.stream().distinct().collect(Collectors.toList());
        String ikey = CommonUtils.getObserverNodeIdenticalKeyMultiFaulty(state, failureTypeTobeAdded);
        // add new nd-observer node to map
        if (!multiFaultyNDDFANodeMap.containsKey(ikey)) {
            MultiFaultyNDDFANode newNode = new MultiFaultyNDDFANode(state);
            newNode.addFailureTypes(failureTypeTobeAdded);
            multiFaultyNDDFANodeMap.put(ikey, newNode);
        }
        return ikey;
    }

    // Returns a List of unvisited multi-faulty nd-observer nodes for the given curr node.
    private List<MultiFaultyNDDFANode> getUnvisitedNextNdNodesMultiFaulty(MultiFaultyNDDFANode curr,
                                                                          Set<String> keysOfVisitedNdNodes,
                                                                          Deque<MultiFaultyNDDFANode> targetQueue) {
        return curr.transitions.stream()
                .map(t -> multiFaultyNDDFANodeMap.get(t.nextKey))
                .filter(n -> !keysOfVisitedNdNodes.contains(
                        CommonUtils.getObserverNodeIdenticalKeyMultiFaulty(n)) &&
                        !targetQueue.contains(n))
                .distinct()
                .collect(Collectors.toList());
    }

    // this method is a generalization of the computeComposition method for single faulty mode.
    private void computeCompositionMultiFaulty(MultiFaultyNDDFANode ndRoot) {
        // multiply faulty mode is considered.
        MultiFaultyCompositeNode compositeRoot = new MultiFaultyCompositeNode(ndRoot.state, ndRoot.state);
        compositeRoot.addFirstFailureTypes(ndRoot.getFailureTypes());
        compositeRoot.addSecondFailureTypes(ndRoot.getFailureTypes());
        multiFaultyCompositeNodeMap.put(CommonUtils.getCompositeNodeIdenticalKeyMultiFaulty(compositeRoot),
                compositeRoot);

        // stores key of the visited composited nodes.
        Set<String> keysOfVisitedCompositedNodes = new HashSet<>();

        // bfs traversal approach is taken.
        Deque<MultiFaultyCompositeNode> queue = new ArrayDeque<>();
        queue.offer(compositeRoot);

        while (!queue.isEmpty()) {
            MultiFaultyCompositeNode pmcNode = queue.poll();
            // mark current node as visited.
            keysOfVisitedCompositedNodes.add(CommonUtils.getCompositeNodeIdenticalKeyMultiFaulty(pmcNode));

            MultiFaultyNDDFANode firstNdNode = multiFaultyNDDFANodeMap.get(
                    CommonUtils.getObserverNodeIdenticalKeyMultiFaulty(pmcNode.firstState,
                            pmcNode.firstFailureTypes));
            MultiFaultyNDDFANode secondNdNode = multiFaultyNDDFANodeMap.get(
                    CommonUtils.getObserverNodeIdenticalKeyMultiFaulty(pmcNode.secondState,
                            pmcNode.secondFailureTypes));

            // if two nd-observer nodes have the same states and labels
            if (isSameState(firstNdNode, secondNdNode)) {
                List<Character> events = getEventSymbols(firstNdNode);
                for (char s : events) {
                    MultiFaultyNDDFANode[] nextNdNodes = getNextMultiFaultyNDDFANodes(firstNdNode, s);
                    for (MultiFaultyNDDFANode outer : nextNdNodes)
                        for (MultiFaultyNDDFANode inner : nextNdNodes)
                            pmcNode.addTransition(s,
                                    addNewCompositeNodeToMapMultiFaulty(
                                            outer.state, outer.failureTypes, inner.state, inner.failureTypes));
                }
            } else {
                // obtains common event labels of two nd-dfa nodes.
                List<Character> fEvents = getEventSymbols(firstNdNode);
                List<Character> sEvents = getEventSymbols(secondNdNode);
                fEvents.retainAll(sEvents);
                for (char s : fEvents) {
                    MultiFaultyNDDFANode[] fNextNdNodes = getNextMultiFaultyNDDFANodes(firstNdNode, s);
                    MultiFaultyNDDFANode[] sNextNdNodes = getNextMultiFaultyNDDFANodes(secondNdNode, s);
                    for (MultiFaultyNDDFANode outer : fNextNdNodes)
                        for (MultiFaultyNDDFANode inner : sNextNdNodes)
                            pmcNode.addTransition(s,
                                    addNewCompositeNodeToMapMultiFaulty(
                                            outer.state, outer.failureTypes,
                                            inner.state, inner.failureTypes));
                }
            }
            // add unvisited next nodes of current composited node to queue.
            queue.addAll(getUnvisitedNextCompNodesMultiFaulty(pmcNode, keysOfVisitedCompositedNodes));
        }
    }

    // returns a list of events of the given multi-faulty nd-observer node.
    private List<Character> getEventSymbols(MultiFaultyNDDFANode curr) {
        return curr.transitions.stream()
                .map(t -> t.symbol)
                .distinct()
                .collect(Collectors.toList());
    }

    // returns an array of the next nodes of the given multi-faulty nd-observer node.
    private MultiFaultyNDDFANode[] getNextMultiFaultyNDDFANodes(MultiFaultyNDDFANode curr, char symbol) {
        return curr.transitions.stream()
                .filter(t -> t.symbol == symbol)
                .map(t -> multiFaultyNDDFANodeMap.get(t.nextKey))
                .toArray(MultiFaultyNDDFANode[]::new);
    }

    // returns true if the given two nodes have the same states and labels
    private boolean isSameState(MultiFaultyNDDFANode nodeA, MultiFaultyNDDFANode nodeB) {
        return nodeA.state == nodeB.state && isSameFailureTypes(nodeA.failureTypes, nodeB.failureTypes);
    }

    // returns true if the given two list have the same failure types.
    private boolean isSameFailureTypes(List<String> firstFailureTypes, List<String> secondFailureTypes) {
        if (firstFailureTypes.size() == secondFailureTypes.size()) {
            List<String> retained = firstFailureTypes.stream()
                    .filter(secondFailureTypes::contains)
                    .collect(Collectors.toList());
            return retained.size() == firstFailureTypes.size();
        }
        return false;
    }

    // add a new composite node to map (multi-faulty mode)
    private String addNewCompositeNodeToMapMultiFaulty(int firstState, List<String> firstFailureTypes,
                                                       int secondState, List<String> secondFailureTypes) {
        String ikey = CommonUtils.getCompositeNodeIdenticalKeyMultiFaulty(
                firstState, firstFailureTypes, secondState, secondFailureTypes);
        if (!multiFaultyCompositeNodeMap.containsKey(ikey)) {
            MultiFaultyCompositeNode newNode = new MultiFaultyCompositeNode(firstState, secondState);
            newNode.addFirstFailureTypes(firstFailureTypes);
            newNode.addSecondFailureTypes(secondFailureTypes);
            multiFaultyCompositeNodeMap.put(ikey, newNode);
        }
        return ikey;
    }

    // Returns a list of unvisited composited nodes of the given node.
    private List<MultiFaultyCompositeNode> getUnvisitedNextCompNodesMultiFaulty(MultiFaultyCompositeNode curr,
                                                                                Set<String> keysOfVisitedNodes) {
        return curr.transitions.stream()
                .filter(t -> !keysOfVisitedNodes.contains(t.nextKey))
                .map(t -> multiFaultyCompositeNodeMap.get(t.nextKey))
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
          // single faulty-mode without extra normal
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-01-06 10:47:30_czE2OmZzNDphczg6ZmVzMg==_config");
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-01-06 10:48:17_czE4OmZzNDphczE0OmZlczI=_config");

         // single faulty-mode with extra normal (small state set)
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-01-09 22:54:38_czE4OmZzNDphczE2OmZlczI=_config");
        // single faulty mode with extra normal (big state set)
        Optional<Object[]> loaded = CommonUtils
                .loadDFAConfigs("2020-03-16 23:13:34_czgwOmZzODphczE4OmZlczQ=_config");

        // multi-faulty mode with extra normal (small state set)
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-01-14 14:09:47_czE3OmZzNDphczE0OmZlczI=_config");
        // multi-faulty mode with extra normal (big state set)
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-03-14 15:24:26_czgwOmZzODphczIwOmZlczQ=_config");
        if (loaded.isPresent()) {
            Object[] res = loaded.get();

            DFANode testRootNode = (DFANode) res[0];
            DFAConfig testDfaConfig = (DFAConfig) res[1];
            // print out the basic infos of the loaded dfa.
            CommonUtils.printDfaConfigs(testDfaConfig);
            Diagnoser dfaDiagnoser = NeotypeDiagnoser.getInstance();
            System.out.println("is diagnosable? -> " + dfaDiagnoser.isDiagnosable(testRootNode, testDfaConfig));
        }
    }
}
