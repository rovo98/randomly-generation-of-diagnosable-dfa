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
     * @param minXNum the minimum number of the states in DFA.
     * @param maxXNum the maximum number of the states in DFA.
     * @return constructed random DFA.
     */
    DFANode constructRandomDFA(int minXNum, int maxXNum);

    /**
     * Returns a constructed random DFA, and also saves its configurations to specified file.
     *
     * @param minXNum    the minimum number of the states in DFA.
     * @param maxXNum    the maximum number of the states in DFA.
     * @param saveConfig control to whether to save the configurations or not.
     * @return A constructed random DFA.
     */
    DFANode constructRandomDFA(int minXNum, int maxXNum, boolean saveConfig);

    /**
     * Modification of the {@code constructRandomDFA} method, which adds an extra normal component
     * for every faulty component.
     *
     * @param minXNum the minimum number of the states in DFA (without extra component).
     * @param maxXNum the maximum number of the states in DFA (without extra component).
     * @return A constructed random DFA with extra normal component.
     */
    DFANode constructRandomDFAExtraNormal(int minXNum, int maxXNum);

    /**
     * Modification of the {@code constructRandomDFA} method, which adds an extra normal component
     * for every faulty component.
     *
     * @param minXNum    the minimum number of the states in DFA (without extra component).
     * @param maxXNum    the maximum number of the states in DFA (without extra component).
     * @param saveConfig control to whether to save the configurations of the constructed dfa or not.
     * @return A constructed random DFA with extra normal component.
     */
    DFANode constructRandomDFAExtraNormal(int minXNum, int maxXNum, boolean saveConfig);

    /**
     * Returns a constructed random DFA with diagnosability.
     *
     * @param minXNum the minimum number of the states in DFA.
     * @param maxXNum the maximum number of the states in DFA.
     * @return A constructed random DFA with diagnosability.
     */
    DFANode constructRandomDFAWithDiagnosability(int minXNum, int maxXNum);

    /**
     * Returns a constructed random DFA with diagnosability, and also saves its configurations
     * to specified file.
     *
     * @param minXNum    the minimum number of the states in DFA.
     * @param maxXNum    the maximum number of the states in DFA.
     * @param saveConfig control to whether to save the configurations or not.
     * @return A constructed random DFA with diagnosability.
     */
    DFANode constructRandomDFAWithDiagnosability(int minXNum, int maxXNum, boolean saveConfig);

    /**
     * Modification of the {@code constructRandomDFAWithDiagnosability} method, which adds an extra
     * normal component for the faulty components.
     *
     * @param minXNum the minimum number of the states in DFA (without the extra normal component).
     * @param maxXNum the maximum number of the states in DFA (without the extra normal component).
     * @return A constructed random DFA with diagnosability.
     */
    DFANode constructRandomDFAExtraNormalWithDiagnosability(int minXNum, int maxXNum);

    /**
     * Modification of the {@code constructRandomDFAWithDiagnosability} method, which adds an extra
     * normal component for the faulty components.
     *
     * @param minXNum    the minimum number of the states in DFA (without the extra normal component).
     * @param maxXNum    the maximum number of the states in DFA (without the extra normal component).
     * @param saveConfig control to whether to save the configurations of the constructed dfa or not.
     * @return A constructed random DFA with diagnosability.
     */
    DFANode constructRandomDFAExtraNormalWithDiagnosability(int minXNum, int maxXNum, boolean saveConfig);

    /**
     * Returns the configuration of the DFA constructor.
     *
     * @return configuration of current DFA constructor, an object of  {@code Config}.
     */
    DFAConfig getDFAConfig();
}
