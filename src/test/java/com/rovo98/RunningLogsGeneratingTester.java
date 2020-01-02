package com.rovo98;

import org.junit.jupiter.api.Test;

public class RunningLogsGeneratingTester {

    @Test
    void generatingLogs() {
        RunningLogsGenerator.setVerbose(true);
        DFAConstructor dfaConstructor = new SimpleDFAConstructor();
        RunningLogsGenerator.generate(
                50,
                dfaConstructor,
                50, 100,
                false);
    }
}
