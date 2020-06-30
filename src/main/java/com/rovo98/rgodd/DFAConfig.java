package com.rovo98.rgodd;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Configurations for constructing random DFA and generating running logs.
 * <br/>
 * This configuration is a part of DFA constructor, e.g. {@link SimpleDFAConstructor}.<br />
 * also used by log generator. see {@link RunningLogsGenerator}.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.21
 */
public class DFAConfig implements Serializable {
    private static final long serialVersionUID = -7017149802736561435L;
    int stateSize;      // the number of the all states.
    int faultyStateSize; // the number of faulty states.(needed to be much less than above.)
    int[] states;       // stores all states of the DFA.
    char[] alphabet;   // stores all labels of transitions.
    int[] faultyEvents; // stores the index of the faulty events.

    char[] observableEvents;
    char[] unobservableEvents;   // faults.
    Map<Integer, DFANode> statesMap = new HashMap<>(); // stores all states nodes of DFA.

    String alphabetSpace = "abcdefghijklmnopqrstuvwxyz"; // possible observable events space.

    // Flag to show whether the constructed dfa uses extra normal component.
    boolean extraNormal = false;

    // Flag to show whether the constructed dfa is multi-faulty or not.
    boolean multiFaulty = false;

    // getters and setters.
    public int getStateSize() {
        return stateSize;
    }

    public void setStateSize(int stateSize) {
        this.stateSize = stateSize;
    }

    public int getFaultyStateSize() {
        return faultyStateSize;
    }

    public void setFaultyStateSize(int faultyStateSize) {
        this.faultyStateSize = faultyStateSize;
    }

    public int[] getStates() {
        return states;
    }

    public void setStates(int[] states) {
        this.states = states;
    }

    public char[] getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(char[] alphabet) {
        this.alphabet = alphabet;
    }

    public int[] getFaultyEvents() {
        return faultyEvents;
    }

    public void setFaultyEvents(int[] faultyEvents) {
        this.faultyEvents = faultyEvents;
    }

    public char[] getObservableEvents() {
        return observableEvents;
    }

    public void setObservableEvents(char[] observableEvents) {
        this.observableEvents = observableEvents;
    }

    public char[] getUnobservableEvents() {
        return unobservableEvents;
    }

    public void setUnobservableEvents(char[] unobservableEvents) {
        this.unobservableEvents = unobservableEvents;
    }

    public Map<Integer, DFANode> getStatesMap() {
        return statesMap;
    }

    public void setStatesMap(Map<Integer, DFANode> statesMap) {
        this.statesMap = statesMap;
    }

    public String getAlphabetSpace() {
        return alphabetSpace;
    }

    public void setAlphabetSpace(String alphabetSpace) {
        this.alphabetSpace = alphabetSpace;
    }

    public boolean isExtraNormal() {
        return extraNormal;
    }

    public void setExtraNormal(boolean extraNormal) {
        this.extraNormal = extraNormal;
    }

    public boolean isMultiFaulty() {
        return multiFaulty;
    }

    public void setMultiFaulty(boolean multiFaulty) {
        this.multiFaulty = multiFaulty;
    }
}
