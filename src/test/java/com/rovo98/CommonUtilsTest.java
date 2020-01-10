package com.rovo98;

import com.rovo98.utils.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

public class CommonUtilsTest {
    @Test
    void testSavingDfaConfigs() {
        DFANode root = new DFANode(1);
        DFAConfig testDFAConfig = new DFAConfig();
        testDFAConfig.setStateSize(100);

        CommonUtils.saveDFAConfigs("testfile.dat", root, testDFAConfig);

        Optional<Object[]> res = CommonUtils.loadDFAConfigs("testfile.dat");

        Assertions.assertTrue(res.isPresent());
        Object[] result = res.get();
        Assertions.assertNotNull(result);

        System.out.println(Arrays.toString(result));
        DFANode readDfaNode = (DFANode) result[0];
        DFAConfig readDfaDFAConfig = (DFAConfig) result[1];
        System.out.println(readDfaNode.state);
        System.out.println(readDfaDFAConfig.stateSize);
    }

    @Test
    void testLoadingDfaConfigs() {
        // Only using diagnosable DFA to generate logs make sense.
//        Optional<Object[]> result = CommonUtils.loadDFAConfigs(
//                "2020-01-06 10:48:17_czE4OmZzNDphczE0OmZlczI=_config");

        Optional<Object[]> result = CommonUtils.loadDFAConfigs(
                "2020-01-09 22:54:38_czE4OmZzNDphczE2OmZlczI=_config");
        Assertions.assertTrue(result.isPresent());
        Object[] res = result.get();
        DFANode root = (DFANode) res[0];
        DFAConfig dfaConfig = (DFAConfig) res[1];

        Assertions.assertNotNull(root);
        Assertions.assertNotNull(dfaConfig);

        CommonUtils.printDfaConfigs(dfaConfig);
        RunningLogsGenerator.setVerbose(false);
        RunningLogsGenerator.setMinSteps(10);
        RunningLogsGenerator.setMaxSteps(40);
        RunningLogsGenerator.generate(100_000, root, dfaConfig, true);
    }
}
