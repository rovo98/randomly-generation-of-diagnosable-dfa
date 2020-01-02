package com.rovo98;

import java.util.HashMap;
import java.util.Map;

/**
 * Configurations for constructing random DFA and generating running logs.
 *
 * This configuration is a part of DFA constructor. also used by generator.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.21
 */
public class Config {
    int stateSize;      // the number of the all states.
    int faultyStateSize; // the number of faulty states.(needed to be much less than above.)
    int[] states;       // stores all states of the DFA.
    char[] alphabet;   // stores all labels of transitions.
    int[] faultyEvents; // stores the index of the faulty events.

    char[] observableEvents;
    char[] unobservableEvents;   // faults.
    Map<Integer, DFANode> statesMap = new HashMap<>(); // stores all states DFA nodes.

    String alphabetSpace = "abcdefghijklmnopqrstuvwxyz"; // possible observable events space.
}
