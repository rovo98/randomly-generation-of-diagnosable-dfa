package com.rovo98.diagnosability;

import java.util.LinkedList;
import java.util.List;

/**
 * Definition of nondeterministic dfa node for testing diagnosability using jiang's method.
 * <br />
 * <br />
 * simple representation of graph node in a observer obtained from the given G (N-DFA)
 * <br />
 * <br />
 * More details, see jiang's paper - <a href="https://ieeexplore.ieee.org/document/940942">
 *     A Polynomial Algorithm for Testing Diagnosability of Discrete-Event Systems</a>}
 * <br />
 *
 * REMARK: Only single faulty mode is considered.
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.27
 */
public class NDDFANode {
    int state;
    // since the dfa we constructed only has single faulty mode.
    // using one string is enough, otherwise String[] is prefer.
    String failureType;
    // since observer node contains different transitions with the same event label (symbol).
    List<Transition> transitions;

    // default constructor for initializing a observer node.
    public NDDFANode(int state, String failureType) {
        this.state = state;
        this.failureType = failureType;
        this.transitions = new LinkedList<>();
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

    public String getFailureType() {
        return failureType;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }
}
