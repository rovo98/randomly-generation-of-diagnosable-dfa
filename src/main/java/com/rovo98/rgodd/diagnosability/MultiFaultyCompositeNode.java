package com.rovo98.rgodd.diagnosability;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A modification of the class {@link CompositeNode} which considers the multiply faulty mode for
 * <br/>
 * the given constructed random dfa.
 * <br />
 * <br />
 * More details, see jiang's paper - <a href="https://ieeexplore.ieee.org/document/940942">
 *     A Polynomial Algorithm for Testing Diagnosability of Discrete-Event Systems</a>.
 * <br />
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2020.01.01
 */
public class MultiFaultyCompositeNode {
    int firstState;
    int secondState;
    List<String> firstFailureTypes;
    List<String> secondFailureTypes;
    List<Transition> transitions;

    // default constructor.
    public MultiFaultyCompositeNode(int firstState, int secondState) {
        this.firstState = firstState;
        this.secondState = secondState;
        this.firstFailureTypes = new ArrayList<>();
        this.secondFailureTypes = new ArrayList<>();
        this.transitions = new LinkedList<>();
    }

    /**
     * Add a list of the new failure types for the first state.
     *
     * @param failureTypes A list of the new failure types to be added.
     */
    public void addFirstFailureTypes(List<String> failureTypes) {
        // TODO: basic validation may needed.
        for (String ft : failureTypes) {
            if (this.firstFailureTypes.contains(ft))
                continue;
            this.firstFailureTypes.add(ft);
        }
    }

    /**
     * Add a list of the new failure types for the second state
     * of current node.
     *
     * @param failureTypes A list of the new failure types to be added.
     */
    public void addSecondFailureTypes(List<String> failureTypes) {
        // TODO: basic validation may needed.
        for (String ft : failureTypes) {
            if (this.secondFailureTypes.contains(ft))
                continue;
            this.secondFailureTypes.add(ft);
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
    public int getFirstState() {
        return firstState;
    }

    public int getSecondState() {
        return secondState;
    }

    public List<String> getFirstFailureTypes() {
        return firstFailureTypes;
    }

    public List<String> getSecondFailureTypes() {
        return secondFailureTypes;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }
}
