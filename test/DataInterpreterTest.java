//package com.vogella.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.auburn.pFogSim.util.DataInterpreter;
import edu.boun.ConstantsClass;
import edu.boun.edgecloudsim.core.SimSettings;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Properties;

class DataInterpreterTest {

    DataInterpreter DI;

    /*@BeforeEach
    void setUp() throws IOException {
        DI = new DataInterpreter();
    }*/

    @Test
    @DisplayName("Distance between 2 points with latitudes and longitudes")
    void testMeasureLongLat() {
        double expectedValue = 7195.0;
        double actualValue = DataInterpreter.measure(41.88927215, -87.76571187, 41.9531739, -87.77938682);//10.0000001;
        double delta = 10.0; // The allowed difference
        assertEquals(expectedValue, actualValue, delta, "Values should be approximately equal");
    }

    @Test
    @DisplayName("Distance between 2 points with latitudes, longitudes, and altitudes")
    void testMeasureLongLatAlt() {
        double expectedValue = 410.6354628838632;
        double actualValue = DataInterpreter.measure(35.3524, 135.0302, 100.0, 35.3532, 135.0305, 500.0);//10.0000001;
        double delta = 10.0; // The allowed difference
        assertEquals(expectedValue, actualValue, delta, "Values should be approximately equal");
    }

    @Test
    @DisplayName("Simulation space testing")
    void testSimulationSpace() {
        try {
            DI = new DataInterpreter();
        }catch( IOException ioException ) {
            System.out.println("Exception: "+ioException);
        }
        String[] file = ConstantsClass.files;
        double minimumLatitude = 90.0;
        double maximumLatitude = -90.0;
        double minimumLongitude = 180.0;
        double maximumLongitude = -180.0;
        double latitude = 0.0;
        double longitude = 0.0;
        int numOfFields;
        try{
            String line;
            for(int i=0; i < file.length; i++){
                BufferedReader in = new BufferedReader(new FileReader(file[i]));
                // reading files in specified directory
                String[] values = null;
                Integer rowIndex = 0;
                Integer colIndex = 0;
                // This assigns the data to the 2D array
                // The program keeps looping through until the line read in by the console contains no data in it i.e. the end of the file.
                while ( ( line = in.readLine()) != null ){
                    String[] current_Record = line.split(",");
                    if(rowIndex == 0) {
                        // Counts the number of fields in the csv file.
                        //numOfFields = current_Record.length;
                        //System.out.println("Number of fields = "+numOfFields);
                    }else{
                        values = current_Record;
                        //System.out.println(values);
                        colIndex = 0;

                        for (String str : values) {
                            //employee_Data[rowCount][y] = str;
                            if(colIndex > 0){
                                //System.out.print(", ");
                                if(colIndex == 1) latitude = Double.parseDouble(str);
                                if(colIndex == 2) longitude =Double.parseDouble(str);
                            }else{
                                //System.out.println();
                            }
                            // The field index variable, colIndex is incremented in every loop.
                            colIndex++;
                        }
                        if(latitude > maximumLatitude) maximumLatitude = latitude;
                        if(latitude < minimumLatitude) minimumLatitude = latitude;
                        if(longitude > maximumLongitude) maximumLongitude = longitude;
                        if(longitude < minimumLongitude) minimumLongitude = longitude;
                    }
                    // The record index variable, rowIndex is incremented in every loop.
                    rowIndex++;
                    //System.out.println(rowIndex);
                }
                // This frees up the BufferedReader file descriptor resources
                in.close();
            }
            /*System.out.println();
            System.out.println("Minimum Latitude = "+minimumLatitude+", Maximum Latitude = "+maximumLatitude);
            System.out.println("Minimum Longitude = "+minimumLongitude+", Maximum Longitude = "+maximumLongitude);*/
        }catch( IOException ioException ) {
            System.out.println("Exception: "+ioException);
        }
        double[] expectedArray = {minimumLongitude, maximumLongitude, minimumLatitude, maximumLatitude};
        double[] actualArray = DataInterpreter.getSimulationSpace();
        double delta = 0.005; // The allowed difference
        Assert.assertArrayEquals("Arrays should be equal within delta", expectedArray, actualArray, delta);
    }

    @Test
    @DisplayName("Max Levels testing")
    void testGetMaxLevels() {
        Integer expectedValue = ConstantsClass.MAX_LEVELS;
        Integer actualValue = DataInterpreter.getMAX_LEVELS();
        assertEquals(expectedValue, actualValue, "Values should be exactly equal.");
    }

    @Test
    @DisplayName("Input Type testing")
    void testGetInputType() {
        String expectedValue = ConstantsClass.inputType;
        String actualValue = DataInterpreter.getInputType();
        assertEquals(expectedValue, actualValue, "Values should be exactly equal.");
    }

    @Test
    @DisplayName("Are Mobile Devices Moving testing")
    void testAreMobileDevicesMoving() {
        //System.out.println("aasfvgs");
        boolean expectedValue = ConstantsClass.movingMobileDevices;
        //System.out.println("fhfgjh");
        boolean actualValue = DataInterpreter.areMobileDevicesMoving();
        //System.out.println("uogkyg");
        assertEquals(expectedValue, actualValue, "Values should be exactly equal.");
    }


    //@RepeatedTest(3)
    @Test
    @DisplayName("Set Max Level testing")
    void testSetMaxLevels() {
        Integer tempMaxLevel = 5;
        DataInterpreter.setMAX_LEVELS(tempMaxLevel);
        assertEquals(tempMaxLevel, DataInterpreter.getMAX_LEVELS(), "Testing with setting max levels multiple times: 1");
        tempMaxLevel = 15;
        DataInterpreter.setMAX_LEVELS(tempMaxLevel);
        assertEquals(tempMaxLevel, DataInterpreter.getMAX_LEVELS(), "Testing with setting max levels multiple times: 2");
        tempMaxLevel = 59;
        DataInterpreter.setMAX_LEVELS(tempMaxLevel);
        assertEquals(tempMaxLevel, DataInterpreter.getMAX_LEVELS(), "Testing with setting max levels multiple times: 3");
    }

    @Test
    @DisplayName("Get files testing")
    void testGetFiles(){
        String[] expectedArray = ConstantsClass.files;
        String[] actualArray = DataInterpreter.getFiles();
        Assert.assertArrayEquals("Arrays should be equal within delta", expectedArray, actualArray);
    }

    @Test
    @DisplayName("Set files testing")
    void testSetFiles(){
        String[] testFiles = {"a.csv", "bcd.java", "efg.xml"};
        String[] expectedArray = testFiles;
        DataInterpreter.setFiles(testFiles);
        String[] actualArray = DataInterpreter.getFiles();
        Assert.assertArrayEquals("Arrays should be equal", expectedArray, actualArray);
    }

    @Test
    @DisplayName("Get Node Specs testing")
    void testGetNodeSpecs(){
        //String[] testFiles = {"a.csv", "bcd.java", "efg.xml"};
        String propertiesFile = ConstantsClass.propertiesFile;
        try {
            DI = new DataInterpreter();
        }catch( IOException ioException ) {
            System.out.println("Exception: "+ioException);
        }
        try {
            InputStream input = new FileInputStream(propertiesFile);
            Properties prop = new Properties();
            int MAX_LEVELS = ConstantsClass.MAX_LEVELS;
            String[][] expectedArray = new String[MAX_LEVELS][20];
            prop.load(input);
            String[][] actualArray = DataInterpreter.getNodeSpecs();
            for(int i=0; i<MAX_LEVELS; i++){
                for(int j=0; j<20; j++){
                    if(MAX_LEVELS-i-1 == 0 && j==3) expectedArray[MAX_LEVELS-i-1][j] = ""+ConstantsClass.oneGbRouterCost;
                    else if (MAX_LEVELS-i-1 == 0 && j==8) expectedArray[MAX_LEVELS-i-1][j] = Boolean.toString(SimSettings.getInstance().isMOVING_SCHOOL());
                    else if (MAX_LEVELS-i-1 == 1 && j==3) expectedArray[MAX_LEVELS-i-1][j] = ""+ConstantsClass.oneGbRouterCost;
                    else if (MAX_LEVELS-i-1 == 1 && j==8) expectedArray[MAX_LEVELS-i-1][j] = Boolean.toString(SimSettings.getInstance().isMOVING_COMMUNITY_CENTER());
                    else if (MAX_LEVELS-i-1 == 2 && j==3) expectedArray[MAX_LEVELS-i-1][j] = ""+ConstantsClass.tenGbRouterCost;
                    else if (MAX_LEVELS-i-1 == 2 && j==8) expectedArray[MAX_LEVELS-i-1][j] = Boolean.toString(SimSettings.getInstance().isMOVING_LIBRARY());
                    else if (MAX_LEVELS-i-1 == 3 && j==3) expectedArray[MAX_LEVELS-i-1][j] = ""+ConstantsClass.tenGbRouterCost;
                    else if (MAX_LEVELS-i-1 == 3 && j==8) expectedArray[MAX_LEVELS-i-1][j] = Boolean.toString(SimSettings.getInstance().isMOVING_WARD());
                    else if (MAX_LEVELS-i-1 == 4 && j==3) expectedArray[MAX_LEVELS-i-1][j] = ""+ConstantsClass.tenGbRouterCost;
                    else if (MAX_LEVELS-i-1 == 4 && j==8) expectedArray[MAX_LEVELS-i-1][j] = Boolean.toString(SimSettings.getInstance().isMOVING_UNIVERSITY());
                    else if (MAX_LEVELS-i-1 == 5 && j==3) expectedArray[MAX_LEVELS-i-1][j] = ""+ConstantsClass.hundredGbRouterCost;
                    else if (MAX_LEVELS-i-1 == 5 && j==8) expectedArray[MAX_LEVELS-i-1][j] = Boolean.toString(SimSettings.getInstance().isMOVING_CITY_HALL());
                    else if (MAX_LEVELS-i-1 == 6 && j==3) expectedArray[MAX_LEVELS-i-1][j] = ""+ConstantsClass.hundredGbRouterCost;
                    else if (MAX_LEVELS-i-1 == 6 && j==8) expectedArray[MAX_LEVELS-i-1][j] = Boolean.toString(SimSettings.getInstance().isMOVING_COMMUNITY_CENTER());
                    else if (MAX_LEVELS-i-1 == 7 && j==3) expectedArray[MAX_LEVELS-i-1][j] = ""+ConstantsClass.hundredGbRouterCost;
                    else if (MAX_LEVELS-i-1 == 7 && j==8) expectedArray[MAX_LEVELS-i-1][j] = Boolean.toString(SimSettings.getInstance().isMOVING_CLOUD());
                    else expectedArray[MAX_LEVELS-i-1][j]=prop.getProperty(("nodeSpecs_"+(i+1)+"_"+j));
                    //System.out.println(expectedArray[MAX_LEVELS-i-1][j]+" ? "+actualArray[MAX_LEVELS-i-1][j]);
                }
            }
            Assert.assertArrayEquals("Arrays should be equal", expectedArray, actualArray);
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
