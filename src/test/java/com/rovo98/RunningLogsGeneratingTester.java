package com.rovo98;

import com.rovo98.utils.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class RunningLogsGeneratingTester {

    @Test
    void generatingLogs() {
        DFAConstructor dfaConstructor = SimpleDFAConstructor.getInstance();
        RunningLogsGenerator.setVerbose(true);
        RunningLogsGenerator.setMinSteps(45);
//        RunningLogsGenerator.setMaxSteps(40);
        RunningLogsGenerator.generate(
                20,
                dfaConstructor.constructRandomDFAExtraNormalWithDiagnosability(
                        50, 80, true),
                dfaConstructor.getDFAConfig());
    }

    @Test
    void generatingLogsForExperiment() {

        // single faulty-mode with extra normal (small state set)
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-01-09 22:54:38_czE4OmZzNDphczE2OmZlczI=_config");
        // single faulty mode with extra normal (big state set)
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-03-16 23:13:34_czgwOmZzODphczE4OmZlczQ=_config");
//
        // multi-faulty mode with extra normal (small state set)
        Optional<Object[]> loaded = CommonUtils
                .loadDFAConfigs("2020-01-14 14:09:47_czE3OmZzNDphczE0OmZlczI=_config");
        // multi-faulty mode with extra normal (big state set)
//        Optional<Object[]> loaded = CommonUtils
//                .loadDFAConfigs("2020-03-14 15:24:26_czgwOmZzODphczIwOmZlczQ=_config");
        Assertions.assertTrue(loaded.isPresent());
        Object[] config = loaded.get();

        DFANode dfaRoot = (DFANode) config[0];
        DFAConfig dfaConfig = (DFAConfig) config[1];

        CommonUtils.printDfaConfigs(dfaConfig);
        RunningLogsGenerator.setMinSteps(10);
        RunningLogsGenerator.setMaxSteps(200);
        RunningLogsGenerator.setVerbose(false);
        RunningLogsGenerator.generate(40_000, dfaRoot, dfaConfig, true);
    }
}
