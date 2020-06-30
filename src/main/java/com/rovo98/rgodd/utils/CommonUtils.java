package com.rovo98.rgodd.utils;

import com.rovo98.rgodd.DFAConfig;
import com.rovo98.rgodd.DFANode;
import com.rovo98.rgodd.diagnosability.CompositeNode;
import com.rovo98.rgodd.diagnosability.MultiFaultyCompositeNode;
import com.rovo98.rgodd.diagnosability.MultiFaultyNDDFANode;
import com.rovo98.rgodd.diagnosability.NDDFANode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Common utils.
 *
 * @author rovo98
 * @version 1.0.0
 * @since 2019.12.21
 */
public class CommonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);

    // this class can not be instanced.
    private CommonUtils() {
    }

    /**
     * Saved constructed dfa configurations.
     *
     * @param filename  name of the file to save the given objects.
     * @param rootNode  root node of the constructed dfa.
     * @param dfaConfig Config object of the dfa.
     */
    public static void saveDFAConfigs(String filename, DFANode rootNode, DFAConfig dfaConfig) {
        // basic checking
        if (filename.contains(".."))
            throw new IllegalArgumentException("Invalid filename is given");
        try {
            Properties config = new Properties();
            config.load(CommonUtils.class.getClassLoader().getResourceAsStream("AppConfigs.properties"));
            String location = config.getProperty("dfa.storageLocation");

            Path folder = Paths.get(location);
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
                LOGGER.debug("Target folder not founded, created successfully!");
            }

            // ## removing the existing file.
            Path f = Paths.get(location, filename);
            if (Files.exists(f)) {
                Files.delete(f);
                LOGGER.debug("File already existed, deleting it first.");
            }

            filename = location + File.separator + filename;
            File file = new File(filename);

            LOGGER.debug("dfa config file to be saved: {}", filename);

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(rootNode);
            oos.writeObject(dfaConfig);
            oos.close();

            LOGGER.debug("dfa config file saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loaded saved dfa configurations from file.
     *
     * @param filename name of the file to be loaded in specified path.
     * @return Optional contains the loaded objects(dfaRoot, dfaConfig) if success;
     * otherwise empty Optional.
     */
    public static Optional<Object[]> loadDFAConfigs(String filename) {
        // basic checking
        if (filename.contains(".."))
            throw new IllegalArgumentException("Invalid filename is given");
        LOGGER.debug("dfa config file to be loaded : {}", filename);
        try {
            Properties config = new Properties();
            config.load(CommonUtils.class.getClassLoader().getResourceAsStream("AppConfigs.properties"));
            String location = config.getProperty("dfa.storageLocation");
            filename = location + File.separator + filename;
            File file = new File(filename);

            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            Object[] result = new Object[2];
            for (int i = 0; i < result.length; i++)
                result[i] = ois.readObject();
            ois.close();
            return Optional.of(result);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Returns default formatted dfa filename.
     *
     * @param dfaConfig configuration of the dfa constructor.
     * @return default formatted dfa filename.
     */
    public static String generateDefaultDFAName(DFAConfig dfaConfig) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String basicInfo = "s" + dfaConfig.getStateSize() + ":fs" + dfaConfig.getFaultyStateSize() +
                ":as" + dfaConfig.getAlphabet().length + ":fes" + dfaConfig.getFaultyEvents().length;
        String filename = Base64.getEncoder().encodeToString(basicInfo.getBytes());
        return df.format(new Date()).concat("_").concat(filename);
    }

    /**
     * print out the basic infos of the given dfa configurations.
     *
     * @param dfaConfig the configuration of the constructed dfa.
     */
    public static void printDfaConfigs(DFAConfig dfaConfig) {
        System.out.println("Generated overall state size and faulty state size:" +
                dfaConfig.getStateSize() + ", " + dfaConfig.getFaultyStateSize() + ".");

        System.out.println("Chosen alphabet size is " + dfaConfig.getAlphabet().length);

        System.out.println("Chosen faulty event size: " + dfaConfig.getFaultyEvents().length);

        System.out.println("Generated alphabet set: " + Arrays.toString(dfaConfig.getAlphabet()));
        System.out.println("Faulty events (index): " + Arrays.toString(dfaConfig.getFaultyEvents()));

        System.out.println("selected observable events : " + Arrays.toString(dfaConfig.getObservableEvents()));
        System.out.println("selected unobservable events : " + Arrays.toString(dfaConfig.getUnobservableEvents()));
        System.out.println("Is extra normal: " + dfaConfig.isExtraNormal());
        System.out.println("Is multi-faulty mode: " + dfaConfig.isMultiFaulty());
    }

    /**
     * Returns a simple key for identifying observer node in a map.
     * <br />
     * REMARKS: only used for single faulty mode.
     *
     * @param state       the state of the observer node.
     * @param failureType the failure type of the observer node.
     * @return A simple key for identifying observer node in a map.
     */
    public static String getObserverNodeIdenticalKey(int state, String failureType) {
        return "(" + state + "," + failureType + ")";
    }

    /**
     * Returns a simple key for identifying the given Nd-dfa node.
     * <br />
     * REMARKS: only used for single faulty mode.
     *
     * @param node a nd-dfa node.
     * @return A simple key for identifying the given Nd-dfa node in a map
     */
    public static String getObserverNodeIdenticalKey(NDDFANode node) {
        return getObserverNodeIdenticalKey(node.getState(), node.getFailureType());
    }

    /**
     * Returns a simple key for identifying observer node in a map.
     * <br />
     * REMARKS: only used for multi-faulty mode.
     *
     * @param state        the state of the observer node.
     * @param failureTypes A list of failure types of the observer node.
     * @return A simple key for identifying observer node in a map.
     */
    public static String getObserverNodeIdenticalKeyMultiFaulty(int state, List<String> failureTypes) {
        // sort the given failureTypes list first
        Collections.sort(failureTypes);
        // returned example: (1, {F1,F2})
        String key = "(" + state + ",{";
        for (String ft : failureTypes)
            key = key.concat(ft).concat(",");
        key = key.substring(0, key.length() - 1);
        return key.concat("})");
    }

    /**
     * Returns a simple key for identifying observer node in a map.
     * <br />
     * REMARKS: only used for multi-faulty mode.
     *
     * @param node A multi-faulty nd-observer node.
     * @return A simple key for identifying observer node in a map.
     */
    public static String getObserverNodeIdenticalKeyMultiFaulty(MultiFaultyNDDFANode node) {
        return getObserverNodeIdenticalKeyMultiFaulty(node.getState(), node.getFailureTypes());
    }

    /**
     * Returns a simple key for identifying composite node in a map.
     * <br />
     * REMARKS: only used for single faulty mode.
     *
     * @param firstState        the first state of the composite node.
     * @param firstFailureType  the first failure type attached with the first state.
     * @param secondState       the second state of the composite node.
     * @param secondFailureType the second failure type attached with the second state.
     * @return A simple key for identifying composite node in a map.
     */
    public static String getCompositeNodeIdenticalKey(int firstState, String firstFailureType,
                                                      int secondState, String secondFailureType) {
        return "(" + getObserverNodeIdenticalKey(firstState, firstFailureType) + "," +
                getObserverNodeIdenticalKey(secondState, secondFailureType) + ")";
    }

    /**
     * Returns a simple key for identifying the given composited node.
     * <br />
     * REMARKS: only used for single faulty mode.
     *
     * @param node a composited dfa node.
     * @return A simple key for identifying the given composited node in a map.
     */
    public static String getCompositeNodeIdenticalKey(CompositeNode node) {
        return getCompositeNodeIdenticalKey(node.getFirstState(), node.getFirstFailureType(),
                node.getSecondState(), node.getSecondFailureType());
    }

    /**
     * Returns a simple key for identifying composite node in a map.
     * <br />
     * REMARKS: only used for multi-faulty mode.
     *
     * @param firstState         the first state of the composite node.
     * @param firstFailureTypes  A list of the failure types for the first state.
     * @param secondState        the second state of the composite node.
     * @param secondFailureTypes A list of the failure types for the second state.
     * @return A simple key for identifying composite node in a map.
     */
    public static String getCompositeNodeIdenticalKeyMultiFaulty(int firstState, List<String> firstFailureTypes,
                                                                 int secondState, List<String> secondFailureTypes) {
        // sort the given failureTypes list first
        Collections.sort(firstFailureTypes);
        Collections.sort(secondFailureTypes);
        // returned example: ((1,{F1}),(20,{F1,F2}))
        return "(" + getObserverNodeIdenticalKeyMultiFaulty(firstState, firstFailureTypes) + "," +
                getObserverNodeIdenticalKeyMultiFaulty(secondState, secondFailureTypes) + ")";
    }

    /**
     * Returns a simple key for identifying composite node in a map.
     * <br />
     * REMARKS: only used for multi-faulty mode.
     *
     * @param node A composited node.
     * @return A simple key for identifying composite node in a map.
     */
    public static String getCompositeNodeIdenticalKeyMultiFaulty(MultiFaultyCompositeNode node) {
        return getCompositeNodeIdenticalKeyMultiFaulty(
                node.getFirstState(), node.getFirstFailureTypes(),
                node.getSecondState(), node.getSecondFailureTypes());
    }
}
