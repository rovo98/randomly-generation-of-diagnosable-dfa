package com.rovo98.rgodd.diagnosability;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A modification of the class {@link NDDFANode} which considers multiply faulty mode for the given
 * <br/>
 * constructed random DFA.
 * <br />
 * <br />
 * More details, see jiang's paper - <a href="https://ieeexplore.ieee.org/document/940942">
 *     A Polynomial Algorithm for Testing Diagnosability of Discrete-Event Systems</a>}
 * <br />
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2020.01.01
 */
public class MultiFaultyNDDFANode {
    int state;
    List<String> failureTypes;
    List<Transition> transitions;

    // default constructor.
    public MultiFaultyNDDFANode(int state) {
        this.state = state;
        this.failureTypes = new ArrayList<>();
        this.transitions = new LinkedList<>();
    }

    /**
     * Add a new failure type to current node.
     *
     * @param failureType the failure type to be added.
     */
    public void addFailureType(String failureType) {
        // TODO: basic validation for the given parameter may needed.
        // ignoring the added failureType
        if (this.failureTypes.contains(failureType))
            return;
        this.failureTypes.add(failureType);
    }

    /**
     * Add a list of new failure types to current node.
     *
     * @param failureTypes A list of the failure types.
     */
    public void addFailureTypes(List<String> failureTypes) {
        // TODO: basic validation for the given parameter may needed.
        // ignoring the added failure types.
        for (String ft : failureTypes) {
            if (this.failureTypes.contains(ft))
                continue;
            this.failureTypes.add(ft);
        }
    }

    /**
     * Add a new transition to current observer node.
     *
     * @param symbol the event label of the transition.
     * @param next   identical key of the next observer node.
     */
    public void addTransition(char symbol, String next) {
        // TODO: basic validation for the given parameters may needed.
        Transition newTransition = new Transition(symbol, next);
        // ignore added transitions.
        if (this.transitions.contains(newTransition))
            return;
        this.transitions.add(newTransition);
    }

    // getters.
    public int getState() {
        return state;
    }

    public List<String> getFailureTypes() {
        return failureTypes;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }
}
