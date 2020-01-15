package com.rovo98;

import org.junit.jupiter.api.Test;

public class RunningLogsGeneratingTester {

    @Test
    void generatingLogs() {
        DFAConstructor dfaConstructor = SimpleDFAConstructor.getInstance();
        RunningLogsGenerator.setVerbose(true);
        RunningLogsGenerator.generate(
                30,
                dfaConstructor.constructRandomDFA(50, 100, false),
                dfaConstructor.getDFAConfig());
    }
}
