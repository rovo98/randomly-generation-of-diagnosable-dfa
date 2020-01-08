package com.rovo98;

import com.rovo98.exceptions.SymbolNotFound;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Complete Deterministic Finite Automata node representation.
 *
 * For simplicity, Do not use generic type
 * 1. using primitive type int as state type.
 * 2. Character type as symbol's type.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019-12-21
 */
public class DFANode implements Serializable {
    private static final long serialVersionUID = -3258151480171964615L;
    int state;
    Map<Character, Integer> transitions;

    // unique constructor (a initial state is needed)
    public DFANode(int state) {
        this.state = state;
        this.transitions = new HashMap<>();
    }

    /**
     * Add a new transition to current node.
     *
     * @param symbol    the label of the transition.
     * @param nextState the next state of the transition (A DFA node)
     */
    public void addTransition(char symbol, int nextState) {
        // TODO: symbol and state value may needed to be check.
        this.transitions.put(symbol, nextState);
    }

    /**
     * Navigates to the next state according the given {@code symbol}
     *
     * @param symbol    the label of one specified transition of current node.
     * @param dfaConfig the configuration of the constructed dfa.
     * @return DFA node with the next state of the specified transition.
     * @throws SymbolNotFound if the given symbol of specified transition not found.
     */
    public DFANode navigate(char symbol, DFAConfig dfaConfig) {
        if (!transitions.containsKey(symbol))
            throw new SymbolNotFound("symbol of specified transition not found: " + symbol);
        return dfaConfig.statesMap.get(transitions.get(symbol));
    }

    // TODO: code refactoring may needed. current implementation is less elegant.

    // getters.
    public int getState() {
        return state;
    }

    public Map<Character, Integer> getTransitions() {
        return transitions;
    }
}
