package com.rovo98.diagnosability;

import java.util.LinkedList;
import java.util.List;

/**
 * Definition of the composited node via a composition using a observer and itself.
 * -> Observer node representation, see {@link NDDFANode}.
 * <br />
 * <br />
 * More details, see jiang's paper - <a href="https://ieeexplore.ieee.org/document/940942">
 *     A Polynomial Algorithm for Testing Diagnosability of Discrete-Event Systems</a>}
 * <br />
 * REMARK: Single faulty mode is only considered.
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.27
 */
public class CompositeNode {
    int firstState;
    int secondState;
    String firstFailureType;
    String secondFailureType;
    List<Transition> transitions;


    // default constructor for initializing a composite node.
    public CompositeNode(int firstState, String firstFailureType, int secondState, String secondFailureType) {
        this.firstState = firstState;
        this.secondState = secondState;
        this.firstFailureType = firstFailureType;
        this.secondFailureType = secondFailureType;
        this.transitions = new LinkedList<>();
    }

    /**
     * add a new transition to current composite node.
     *
     * @param symbol the event label of the transition.
     * @param next   identical key of the next composite node.
     */
    public void addTransition(char symbol, String next) {
        // TODO: basic validation for the given parameters is needed.
        Transition newTransition = new Transition(symbol, next);
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

    public String getFirstFailureType() {
        return firstFailureType;
    }

    public String getSecondFailureType() {
        return secondFailureType;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }
}
