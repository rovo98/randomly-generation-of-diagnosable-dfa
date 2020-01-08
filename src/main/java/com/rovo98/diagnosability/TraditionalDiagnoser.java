package com.rovo98.diagnosability;

import com.rovo98.DFAConfig;
import com.rovo98.DFANode;

/**
 * Testing dfa's diagnosability by constructing a traditional diagnoser.
 *
 * A diagnoser is described in book 'Introduction to Discrete-Event Systems'
 *
 * And you can also found it in Sampath's paper 'Diagnosability of Discrete-Event Systems'
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.27
 */
public class TraditionalDiagnoser implements Diagnoser{
    @Override
    public boolean isDiagnosable(DFANode root, DFAConfig dfaConfig) {
        return false;
    }
}
