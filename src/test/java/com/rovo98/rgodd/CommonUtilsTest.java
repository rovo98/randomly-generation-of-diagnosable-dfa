package com.rovo98.rgodd;

import com.rovo98.rgodd.utils.CommonUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonUtilsTest {
    @Test
    void testSavingDfaConfigs() {
        DFANode root = new DFANode(1);
        DFAConfig testDFAConfig = new DFAConfig();
        testDFAConfig.setStateSize(100);

        CommonUtils.saveDFAConfigs("testfile.dat", root, testDFAConfig);

        Optional<Object[]> res = CommonUtils.loadDFAConfigs("testfile.dat");

        assertTrue(res.isPresent());
        Object[] result = res.get();
        assertNotNull(result);

        System.out.println(Arrays.toString(result));
        DFANode readDfaNode = (DFANode) result[0];
        DFAConfig readDfaDFAConfig = (DFAConfig) result[1];
        System.out.println(readDfaNode.state);
        System.out.println(readDfaDFAConfig.stateSize);
    }

    @Test
    void testLoadingDfaConfigAndGeneratingLogs() {
        // Only using diagnosable DFA to generate logs make sense.
        // example without extra normal component and multi-faulty mode.
//        Optional<Object[]> result = CommonUtils.loadDFAConfigs(
//                "2020-01-06 10:48:17_czE4OmZzNDphczE0OmZlczI=_config");

        // example with extra normal component but without multi-faulty mode.
//        Optional<Object[]> result = CommonUtils.loadDFAConfigs(
//                "2020-01-09 22:54:38_czE4OmZzNDphczE2OmZlczI=_config");

        // example with extra normal component and multi-faulty mode.
        Optional<Object[]> result = CommonUtils.loadDFAConfigs(
                "2020-01-14 14:09:47_czE3OmZzNDphczE0OmZlczI=_config");
        assertTrue(result.isPresent());
        Object[] res = result.get();
        DFANode root = (DFANode) res[0];
        DFAConfig dfaConfig = (DFAConfig) res[1];

        assertNotNull(root);
        assertNotNull(dfaConfig);

        CommonUtils.printDfaConfigs(dfaConfig);
        RunningLogsGenerator runningLogsGenerator = new RunningLogsGenerator(20, 40, true);
        runningLogsGenerator.generate(10, root, dfaConfig, false);
    }

    @Test
    void testLoadingDfaConfig() {
//        Optional<Object[]> result = CommonUtils.loadDFAConfigs(
//                "2020-01-09 22:54:38_czE4OmZzNDphczE2OmZlczI=_config");

        Optional<Object[]> result = CommonUtils.loadDFAConfigs(
                "2020-01-14 14:09:47_czE3OmZzNDphczE0OmZlczI=_config");

        assertTrue(result.isPresent());
        Object[] res = result.get();
        DFANode root = (DFANode) res[0];
        DFAConfig dfaConfig = (DFAConfig) res[1];

        assertNotNull(root);
        assertNotNull(dfaConfig);

        CommonUtils.printDfaConfigs(dfaConfig);
    }
}
