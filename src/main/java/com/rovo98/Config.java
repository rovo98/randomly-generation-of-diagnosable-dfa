package com.rovo98;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Configurations for constructing random DFA and generating running logs.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.21
 */
public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    static int stateSize;      // the number of the all states.
    static int faultyStateSize; // the number of faulty states.(needed to be much less than above.)
    static int[] states;       // stores all states of the DFA.
    static char[] alphabet;   // stores all labels of transitions.
    static int[] faultyEvents; // stores the index of the faulty events.

    static char[] observableEvents;
    static char[] unobservableEvents;   // faults.
    static Map<Integer, DFANode> statesMap = new HashMap<>(); // stores all states DFA nodes.

    static String alphabetSpace = "abcdefghijklmnopqrstuvwxyz";

    /**
     * Initialization is needed before constructing DFA.
     *
     * @param minXSize maximum number of the states
     * @param maxXSize minimum number of the states
     */
    static void initialization(int minXSize, int maxXSize) {
        Random r = new Random();
        stateSize = r.nextInt((maxXSize - minXSize) + 1) + minXSize;
        faultyStateSize = stateSize / 10;

        LOGGER.debug("Generated overall state size and faulty state size: {}, {}", stateSize, faultyStateSize);

        states = new int[stateSize];
        for (int i = 0; i < stateSize; i++) {
            states[i] = i;
        }
        // alphabet size: range[10 ~ 20]
        int alphabetSize = r.nextInt(11) + 10;
        alphabet = new char[alphabetSize];

        LOGGER.debug("Chosen alphabet size is {}", alphabetSize);

        // randomly fill the alphabet
        int alphabetSpaceLen = alphabetSpace.length();
        boolean[] tempFlags = new boolean[alphabetSpaceLen];
        int tempIndex;
        for (int i = 0; i < alphabet.length; i++) {
            tempIndex = r.nextInt(alphabetSpaceLen);
            while (tempFlags[tempIndex]) {
                tempIndex = r.nextInt(alphabetSpaceLen);
            }
            tempFlags[tempIndex] = true;
            alphabet[i] = alphabetSpace.charAt(tempIndex);
        }
        // faulty event size (make sure less than a half of alphabet size)
        // 4 ~ 6
        int faultSize = 4;
        if (alphabetSize > 15 && (faultyStateSize / faultSize) > 3) {
            faultSize = faultSize + r.nextInt(2) + 1;
        }
        faultyEvents = new int[faultSize];

        LOGGER.debug("Chosen faulty event size: {}", faultSize);

        boolean[] chosenFaultMarks = new boolean[alphabetSize];
        int choose;
        for (int i = 0; i < faultSize; i++) {
            choose = r.nextInt(alphabetSize);
            while (chosenFaultMarks[choose])
                choose = r.nextInt(alphabetSize);
            chosenFaultMarks[choose] = true;
            faultyEvents[i] = choose;
        }

        LOGGER.debug("Generated alphabet set: {}", Arrays.toString(alphabet));
        LOGGER.debug("Faulty events (index): {}", Arrays.toString(faultyEvents));

        Set<Integer> faultyEventIndexSet = new HashSet<>();
        for (int fi : faultyEvents)
            faultyEventIndexSet.add(fi);

        int ui = 0;
        int oi = 0;
        observableEvents = new char[alphabetSize - faultSize];
        unobservableEvents = new char[faultSize];
        for (int i = 0; i < alphabetSize; i++) {
            if (faultyEventIndexSet.contains(i))
                unobservableEvents[ui++] = alphabet[i];
            else
                observableEvents[oi++] = alphabet[i];
        }
        LOGGER.debug("selected observable events : {}", Arrays.toString(observableEvents));
        LOGGER.debug("selected unobservable events : {}", Arrays.toString(unobservableEvents));
    }
}
