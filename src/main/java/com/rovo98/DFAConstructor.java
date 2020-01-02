package com.rovo98;

/**
 * Interface defines the behaviors of RandomDFAConstructors.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019-12-21
 */
public interface DFAConstructor {

    /**
     * Returns a constructed random DFA
     *
     * @param minXNum minimum number of the states in DFA.
     * @param maxXNum maximum number of the states in DFA.
     * @return constructed random DFA.
     */
    DFANode constructRandomDFA(int minXNum, int maxXNum);

    /**
     * Returns the configuration of the DFA constructor.
     *
     * @return configuration of current DFA constructor, an object of  {@code Config}.
     */
    Config getConfig();
}
