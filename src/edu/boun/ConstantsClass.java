package edu.boun;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConstantsClass {
    public static final String APPLICATION_NAME = "PFogSim";
    public static final int VERSION_CODE = 1;
    public static int MAX_LEVELS = 7;
    public static String[] files= {
            "Google_Cloud_DC.csv",
            "Chicago_CityHall.csv",
            "Chicago_Universities.csv",
            "Chicago_Wards.csv",
            "Chicago_Libraries.csv",
            "Chicago_Connect.csv",
            "Chicago_Schools.csv"};
    public static String inputType = "gps";
    public static boolean movingMobileDevices = false;
    public static String propertiesFile = "scripts/sample_application/config/default_config.properties";
    public static double tenGbRouterCost = 151.67/2692915200.0 * 100; // $/Mb numbers taken from cisco ASR 901 10G router at $151.67 per month
    public static double oneGbRouterCost = 88.23/269291520.0 * 100; // $/Mb numbers taken from cisco ASR 901 1G router at $88.23 per month
    public static double hundredGbRouterCost = 646.51/26929152000.0 * 100; // $/Mb numbers taken from cisco ASR 1013 100G router at $646.51 per month
    // Shaik modified - Multiplied above three costs for routers' data transfer by 1000 to reflect the service provider costs & profit, in addition to router monthly lease fee.
    public static String nodeXmlFile = "node_test.xml";
    public static String linksXmlFile = "links_test.xml";
    private String[] SIMULATION_SCENARIOS;
    private boolean generateTaskCreationCSV;
    private boolean generatMobileDeviceFogNodePlot;
    private static int min_number_of_mobile_devices;
    private static int max_number_of_mobile_devices;
    private static int mobile_device_counter_size;

    public ConstantsClass() {
        // Prevent instantiation
    }


    public String[] getSIMULATION_SCENARIOS() {
        InputStream propInput = null;
        try {
            propInput = new FileInputStream(propertiesFile);

            // load a properties file
            Properties prop = new Properties();
            prop.load(propInput);
            SIMULATION_SCENARIOS = prop.getProperty("simulation_scenarios").split(",");
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        return SIMULATION_SCENARIOS;
    }

    public boolean isGenerateTaskCreationCSV(){
        InputStream propInput = null;
        try {
            propInput = new FileInputStream(propertiesFile);

            // load a properties file
            Properties prop = new Properties();
            prop.load(propInput);
            generateTaskCreationCSV = prop.getProperty("generate_task_creation_csv").equals("true");
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        return generateTaskCreationCSV;
    }

    public boolean isGenerateMobileDeviceFogNodePlot(){
        InputStream propInput = null;
        try {
            propInput = new FileInputStream(propertiesFile);

            // load a properties file
            Properties prop = new Properties();
            prop.load(propInput);
            generatMobileDeviceFogNodePlot = prop.getProperty("generate_json_for_mobile_device_fog_node_plot").equals("true");
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        return generatMobileDeviceFogNodePlot;
    }

    public int getMin_number_of_mobile_devices(){
        InputStream propInput = null;
        try {
            propInput = new FileInputStream(propertiesFile);

            // load a properties file
            Properties prop = new Properties();
            prop.load(propInput);
            min_number_of_mobile_devices = Integer.parseInt((prop.getProperty("min_number_of_mobile_devices")));
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        return min_number_of_mobile_devices;
    }

    public int getMax_number_of_mobile_devices(){
        InputStream propInput = null;
        try {
            propInput = new FileInputStream(propertiesFile);

            // load a properties file
            Properties prop = new Properties();
            prop.load(propInput);
            max_number_of_mobile_devices = Integer.parseInt((prop.getProperty("max_number_of_mobile_devices")));
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        return max_number_of_mobile_devices;
    }

    public int getMobile_device_counter_size(){
        InputStream propInput = null;
        try {
            propInput = new FileInputStream(propertiesFile);

            // load a properties file
            Properties prop = new Properties();
            prop.load(propInput);
            mobile_device_counter_size = Integer.parseInt((prop.getProperty("mobile_device_counter_size")));
        }catch (IOException ex) {
            ex.printStackTrace();
        }
        return mobile_device_counter_size;
    }
}

