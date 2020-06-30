package com.rovo98.rgodd.diagnosability;

import java.util.Objects;

/**
 * Representation for transition in observer and composited finite automata.
 * <br/>
 * Used by {@link NDDFANode} and {@link CompositeNode}.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.27
 */
public class Transition {
    char symbol;        // event label of the transition.
    String nextKey;     // representing next node. (identical key)

    public Transition(char symbol, String key) {
        this.symbol = symbol;
        this.nextKey = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transition)) return false;
        Transition that = (Transition) o;
        return symbol == that.symbol &&
                Objects.equals(nextKey, that.nextKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, nextKey);
    }

    // getters and setters.
    public char getSymbol() {
        return symbol;
    }

    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    public String getNextKey() {
        return nextKey;
    }

    public void setNextKey(String nextKey) {
        this.nextKey = nextKey;
    }
}
