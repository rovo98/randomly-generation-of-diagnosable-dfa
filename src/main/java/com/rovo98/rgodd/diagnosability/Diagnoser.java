package com.rovo98.rgodd.diagnosability;

import com.rovo98.rgodd.DFAConfig;
import com.rovo98.rgodd.DFANode;

/**
 * Defining basic behaviors of the diagnoser constructing from the given DFA.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.27
 */
public interface Diagnoser {
    /**
     * Returns true if the given dfa is diagnosable
     *
     * @param root      the root node of the give dfa.
     * @param dfaConfig the configuration of the constructed dfa (containing overall nodes in dfa).
     * @return true if the given dfa is diagnosable; otherwise false.
     */
    boolean isDiagnosable(DFANode root, DFAConfig dfaConfig);
}
