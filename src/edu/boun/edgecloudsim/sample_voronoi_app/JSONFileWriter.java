package edu.boun.edgecloudsim.sample_voronoi_app;
//import com.fasterxml.jackson.databind.JsonSerializer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.io.FileWriter;
import java.io.IOException;
/**
 * @author Crunchify.com
 * Simplifying JSON File Handling in Java: A Step-by-Step Guide with Logging
 */
public class JSONFileWriter {
    //private static FileWriter crunchifyFile;
    private static FileWriter myFile;
    @SuppressWarnings("unchecked")
    public String createFile(String fileName){
        //JSONObject fileName = new JSONObject();
        //JSONObject jsonObj = new JSONObject();
        try {
            // JSON object.
            // Key value pairs are unordered.
            // JSONObject supports java.util.Map interface.
            JSONObject jsonObj = new JSONObject();

            JSONObject ele = new JSONObject();
            ele.put("name", "dummy");
            ele.put("longitude", 0.12);
            ele.put("latitude", 0.13);
            ele.put("altitude", 0.0);

            // Constructs a FileWriter given a file name, using the platform's default charset
            myFile = new FileWriter(fileName);
            myFile.write(jsonObj.toJSONString());
            myLog("Successfully Copied JSON Object to File...");
            myLog("\nJSON Object: " + jsonObj);
            myFile.flush();
            myFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public void updateFile(String fileName, String name, double longitude, double latitude, double altitude){
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = new JSONObject();
        try {
            // JSON object.
            // Key value pairs are unordered.
            // JSONObject supports java.util.Map interface.
            //jsonObject.get(new FileReader(fileName));
            Reader reader = new FileReader(fileName);
            Object obj = jsonParser.parse(reader);//new FileReader("./"+fileName));
            jsonObject = (JSONObject) obj;
            System.out.println("jsonObject.size() = "+jsonObject.size());
            int objIndex = jsonObject.size();

            //Object obj = jsonParser.parse(new FileReader(fileName));
            System.out.println("jsonObject = "+jsonObject);

            //int jsonLength = obj.keys(obj).length;
            //int count = Object.keys(obj).length;
            JSONArray jsonArray = new JSONArray();
            //jsonArray.add(jsonObject);
            /*JSONArray jsonArray = null;
            if(obj != null){
                //createFile(fileName);
                jsonArray = (JSONArray) obj;

                System.out.println(jsonArray);
            }else{
                jsonArray = new JSONArray();
            }*/

            JSONObject ele = new JSONObject();
            //ele.put("name", name);
            ele.put("longitude", longitude);
            ele.put("latitude", latitude);
            ele.put("altitude", altitude);

            jsonArray.add(ele);

            System.out.println("jsonArray = "+jsonArray);

            jsonObject.put(objIndex, jsonArray);
            System.out.println("jsonObject = "+jsonObject);

            // Constructs a FileWriter given a file name, using the platform's default charset

            FileWriter myFile = new FileWriter(fileName);

            myFile.write(jsonObject.toJSONString());
            myFile.flush();
            myFile.close();

            //System.exit(0);


        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        //return fileName;
    }
    static public void myLog(String str) {
        System.out.println(str);
    }

    public static void main(String[] args) {
        try {
            // JSON object.
            // Key value pairs are unordered.
            // JSONObject supports java.util.Map interface.
            JSONObject crunchifyObj = new JSONObject();
            crunchifyObj.put("Name", "Crunchify.com");
            crunchifyObj.put("Author", "App Shah");
            JSONArray crunchifyCompany = new JSONArray();
            crunchifyCompany.add("Company: Facebook");
            crunchifyCompany.add("Company: PayPal");
            crunchifyCompany.add("Company: Google");
            crunchifyObj.put("Company List", crunchifyCompany);
            // Constructs a FileWriter given a file name, using the platform's default charset
            FileWriter crunchifyFile = new FileWriter("crunchify.txt");
            crunchifyFile.write(crunchifyObj.toJSONString());
            crunchifyLog("Successfully Copied JSON Object to File...");
            crunchifyLog("\nJSON Object: " + crunchifyObj);
            crunchifyObj.get("Name");
            crunchifyObj.put("Name", "Test");
            crunchifyLog("\nJSON Object: " + crunchifyObj);
            crunchifyFile.flush();
            crunchifyFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static public void crunchifyLog(String str) {
        System.out.println(str);
    }
}
