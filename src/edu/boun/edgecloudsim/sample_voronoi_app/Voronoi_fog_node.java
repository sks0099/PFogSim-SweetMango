package edu.boun.edgecloudsim.sample_voronoi_app;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Objects;


public class Voronoi_fog_node {
    private static String fogNodeFileName = "fogNode_loc.json";
    private static String mobileDeviceFileName = "mobile_loc.json";


    public static void createFogNodePlot(String fogNodeFileName, String mobileDeviceFileName){
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = new JSONObject();
        double longitude = 0.0;
        double latitude = 0.0;
        double longitudeCheck = 0.0;
        double latitudeCheck = 0.0;
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        double R = 6378137.0; //in meter at equator
        double scaling_factor = 1.0; //0.000032;
        //double x_translation = -6.0;
        //double y_translation = 152.0;

        //double scaling_factor_m = 0.000024;
        //double x_translation_m = -4.5;
        //double y_translation_m = 114.0;

        ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> fogNodePoints = new ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point>();
        ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> mobileDevicePoints = new ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point>();

        try {
            // JSON object.
            // Key value pairs are unordered.
            // JSONObject supports java.util.Map interface.
            //jsonObject.get(new FileReader(fileName));
            Reader reader = new FileReader(fogNodeFileName);
            Object obj = jsonParser.parse(reader);//new FileReader("./"+fileName));
            jsonObject = (JSONObject) obj;
            int num_nodes = jsonObject.size();
            System.out.println("num_nodes = "+num_nodes);
            String key = "";
            int j = 0;
            boolean found=false;
            double sum_longitude = 0.0;
            double sum_latitude = 0.0;
            double centroid_longitude = 0.0;
            double centroid_latitude = 0.0;
            double centroid_x =  0.0;
            double centroid_y = 0.0;
            double x_min = 0.0;
            double x_max = 0.0;
            double y_min = 0.0;
            double y_max = 0.0;
            for(int i=0; i<num_nodes; i++) {
                key = Integer.toString(i);
                //System.out.println("key="+key);
                if (jsonObject.containsKey(key)) {
                    //System.out.println(jsonObject.get(key));
                    JSONObject childObject = (JSONObject)((JSONArray)jsonObject.get(key)).get(0);
                    //System.out.println(childObject);
                    //System.out.println(childObject.get("longitude"));
                    //System.out.println(childObject.get("latitude"));
                    //System.out.println(childObject.get("altitude"));
                    latitude = Double.parseDouble(childObject.get("latitude").toString());
                    longitude = Double.parseDouble(childObject.get("longitude").toString());
                    sum_latitude += latitude;
                    sum_longitude += longitude;
                    //Check if does not exist
                    found = false;
                    //while(!found && i > 0){
                    //    j = i - 1;
                    for(j = 0; j<i-1; j++){
                        JSONObject childObjectCheck = (JSONObject)((JSONArray)jsonObject.get(Integer.toString(j))).get(0);
                        //System.out.println(childObject);
                        //System.out.println(childObject.get("longitude"));
                        //System.out.println(childObject.get("latitude"));
                        //System.out.println(childObject.get("altitude"));
                        latitudeCheck = Double.parseDouble(childObjectCheck.get("latitude").toString());

                        longitudeCheck = Double.parseDouble(childObjectCheck.get("longitude").toString());

                        if(Objects.equals(latitude, latitudeCheck) && Objects.equals(longitude, longitudeCheck)){
                            found = true;
                            break;
                        }
                    }

                    //System.exit(0);
                    //}
                    if(!found) {
                        x = R * Math.cos(Math.PI * latitude / 180.0) * Math.cos(Math.PI * longitude / 180.0);
                        y = R * Math.cos(Math.PI * latitude / 180.0) * Math.sin(Math.PI * longitude / 180.0);
                        if (x < x_min) x_min = x;
                        if (x > x_max) x_max = x;
                        if (y < y_min) y_min = y;
                        if (y > y_max) y_max = y;
                        edu.boun.edgecloudsim.sample_voronoi_app.Point pt = new edu.boun.edgecloudsim.sample_voronoi_app.Point(x, y);
                        fogNodePoints.add(pt);
                    }
                    /*x = R * cos(lat) * cos(lon)
                    y = R * cos(lat) * sin(lon)
                    z = R *sin(lat)*/
                }else{
                    System.out.println(key+" doesn't exist.");
                }
            }
            centroid_longitude = sum_longitude/num_nodes;
            centroid_latitude = sum_latitude/num_nodes;
            System.out.println("centroid_longitude = "+centroid_longitude+" centroid_latitude = "+centroid_latitude);
            centroid_x =  scaling_factor * R * Math.cos(Math.PI * centroid_latitude / 180.0) * Math.cos(Math.PI * centroid_longitude / 180.0);;
            centroid_y = scaling_factor * R * Math.cos(Math.PI * centroid_latitude / 180.0) * Math.sin(Math.PI * centroid_longitude / 180.0);;
            System.out.println("centroid_x = "+centroid_x+" centroid_y = "+centroid_y);
            //scaling_factor = 1024.0/(10.0* Math.max((x_max-x_min),(y_max-y_min)));
            scaling_factor = 1024.0/(3*Math.max((x_max-x_min),(y_max-y_min)));
            System.out.println("scaling_factor = "+scaling_factor);
            //System.exit(0);

            //translate the points according to the centroid and add them to the list
            for (edu.boun.edgecloudsim.sample_voronoi_app.Point p: fogNodePoints) {
                //System.out.println(p.x + ", "+ p.y);
                p.x -= centroid_x;
                p.x *= scaling_factor;
                p.y -= centroid_y;
                p.y *= scaling_factor;
            }
            /*for (Point p: fogNodePoints) {
                System.out.println(p.x + ", "+ p.y);
            }
            System.exit(0);*/

            reader = new FileReader(mobileDeviceFileName);
            obj = jsonParser.parse(reader);//new FileReader("./"+fileName));
            jsonObject = (JSONObject) obj;
            System.out.println("jsonObject.size() = "+jsonObject.size());
            num_nodes = jsonObject.size();
            key = "";
            j = 0;

            sum_longitude = 0.0;
            sum_latitude = 0.0;
            centroid_longitude = 0.0;
            centroid_latitude = 0.0;
            centroid_x =  0.0;
            centroid_y = 0.0;
            x_min = 0.0;
            x_max = 0.0;
            y_min = 0.0;
            y_max = 0.0;

            found=false;
            for(int i=0; i<num_nodes; i++) {
                key = Integer.toString(i);
                if (jsonObject.containsKey(key)) {
                    //System.out.println(jsonObject.get(key));
                    JSONObject childObject = (JSONObject)((JSONArray)jsonObject.get(key)).get(0);
                    //System.out.println(childObject);
                    //System.out.println(childObject.get("longitude"));
                    //System.out.println(childObject.get("latitude"));
                    //System.out.println(childObject.get("altitude"));
                    latitude = Double.parseDouble(childObject.get("latitude").toString());
                    longitude = Double.parseDouble(childObject.get("longitude").toString());
                    sum_latitude += latitude;
                    sum_longitude += longitude;
                    //Check if does not exist
                    found = false;
                    //while(!found && i > 0){
                    //    j = i - 1;
                    for(j = 0; j<i-1; j++){
                        JSONObject childObjectCheck = (JSONObject)((JSONArray)jsonObject.get(Integer.toString(j))).get(0);
                        //System.out.println(childObject);
                        //System.out.println(childObject.get("longitude"));
                        //System.out.println(childObject.get("latitude"));
                        //System.out.println(childObject.get("altitude"));
                        latitudeCheck = Double.parseDouble(childObjectCheck.get("latitude").toString());
                        longitudeCheck = Double.parseDouble(childObjectCheck.get("longitude").toString());
                        if(Objects.equals(latitude, latitudeCheck) && Objects.equals(longitude, longitudeCheck)){
                            found = true;
                            break;
                        }
                    }
                    //}
                    if(!found) {
                        x = R * Math.cos(Math.PI * latitude / 180.0) * Math.cos(Math.PI * longitude / 180.0);
                        y = R * Math.cos(Math.PI * latitude / 180.0) * Math.sin(Math.PI * longitude / 180.0);
                        if (x < x_min) x_min = x;
                        if (x > x_max) x_max = x;
                        if (y < y_min) y_min = y;
                        if (y > y_max) y_max = y;
                        edu.boun.edgecloudsim.sample_voronoi_app.Point pt = new edu.boun.edgecloudsim.sample_voronoi_app.Point(x, y);
                        mobileDevicePoints.add(pt);
                    }
                }else{
                    System.out.println(key+" doesn't exist.");
                }
            }
            //System.out.println("points = "+fogNodePoints+"\n"+mobileDevicePoints);
            centroid_longitude = sum_longitude/num_nodes;
            centroid_latitude = sum_latitude/num_nodes;
            System.out.println("centroid_longitude = "+centroid_longitude+" centroid_latitude = "+centroid_latitude);
            centroid_x =  R * Math.cos(Math.PI * centroid_latitude / 180.0) * Math.cos(Math.PI * centroid_longitude / 180.0);;
            centroid_y = R * Math.cos(Math.PI * centroid_latitude / 180.0) * Math.sin(Math.PI * centroid_longitude / 180.0);;
            System.out.println("centroid_x = "+centroid_x+" centroid_y = "+centroid_y);
            //scaling_factor = 1024.0/(10.0* Math.max((x_max-x_min),(y_max-y_min)));
            scaling_factor = 1024.0/(3*Math.max((x_max-x_min),(y_max-y_min)));
            System.out.println("scaling_factor = "+scaling_factor);
            //System.exit(0);

            //translate the points according to the centroid and add them to the list
            for (edu.boun.edgecloudsim.sample_voronoi_app.Point p: mobileDevicePoints) {
                //System.out.println(p.x + ", "+ p.y);
                p.x -= centroid_x;
                p.x *= scaling_factor;
                p.y -= centroid_y;
                p.y *= scaling_factor;
            }

            Voronoi fogNode_mobileDev_voronoi = new Voronoi(fogNodePoints);
            //fog_node_voronoi.plot(fogNodePoints, mobileDevicePoints);
            //fogNode_mobileDev_voronoi.plotSwing(fogNodePoints, mobileDevicePoints);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            createFogNodePlot(fogNodeFileName, mobileDeviceFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
