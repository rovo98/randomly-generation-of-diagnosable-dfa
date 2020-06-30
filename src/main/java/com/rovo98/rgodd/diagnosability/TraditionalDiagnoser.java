package com.rovo98.rgodd.diagnosability;

import com.rovo98.rgodd.DFAConfig;
import com.rovo98.rgodd.DFANode;

/**
 * Testing dfa's diagnosability by constructing a traditional diagnoser.
 * <br />
 * <br />
 * A diagnoser is described in book 'Introduction to Discrete-Event Systems'
 * <br />
 * <br />
 * And you can also found it in Sampath's paper - <a href="https://ieeexplore.ieee.org/document/412626">
 * Diagnosability of Discrete-Event Systems</a>
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.27
 */
public class TraditionalDiagnoser implements Diagnoser {
    @Override
    public boolean isDiagnosable(DFANode root, DFAConfig dfaConfig) {
        // TODO: code implementation should in the following.
        throw new UnsupportedOperationException("This method is not supported in current implementation.");
        // Diag(G) = Obs(G || G_label)
        // 1. compute parallel composition of the dfa and its labels automata.
        // 2. constructs the observer of the above machine.
    }
}
