package com.rovo98;

import org.junit.jupiter.api.Test;

public class RunningLogsGeneratingTester {

    @Test
    void generatingLogs() {
        RunningLogsGenerator.setVerbose(true);
        DFAConstructor dfaConstructor = SimpleDFAConstructor.getInstance();
        RunningLogsGenerator.generate(
                50,
                dfaConstructor.constructRandomDFA(50, 100),
                dfaConstructor.getDFAConfig());
    }
}
