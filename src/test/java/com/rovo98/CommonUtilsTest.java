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

        if (res.isPresent()) {
            Object[] result = res.get();
            System.out.println(Arrays.toString(result));
            DFANode readDfaNode = (DFANode) result[0];
            DFAConfig readDfaDFAConfig = (DFAConfig) result[1];
            System.out.println(readDfaNode.state);
            System.out.println(readDfaDFAConfig.stateSize);
        }
    }

    @Test
    void testLoadingDfaConfigs() {
//        Optional<Object[]> result = CommonUtils.loadDFAConfigs(
//                "2020-01-06 10:47:30_czE2OmZzNDphczg6ZmVzMg==_config");
        Optional<Object[]> result = CommonUtils.loadDFAConfigs(
                "2020-01-06 10:48:17_czE4OmZzNDphczE0OmZlczI=_config");
//        Optional<Object[]> result = CommonUtils.loadDFAConfigs(
//                "2020-01-06 10:48:57_czEzOmZzNDphczc6ZmVzMg==_config");
        Assertions.assertTrue(result.isPresent());
        Object[] res = result.get();
        DFANode root = (DFANode) res[0];
        DFAConfig dfaConfig = (DFAConfig) res[1];

        System.out.println(root);
        System.out.println(dfaConfig);

        System.out.println(dfaConfig.statesMap);

        CommonUtils.printDfaConfigs(dfaConfig);
        RunningLogsGenerator.setVerbose(true);
        RunningLogsGenerator.setMinSteps(10);
        RunningLogsGenerator.setMaxSteps(15);
        RunningLogsGenerator.generate(10, root, dfaConfig);
    }
}
