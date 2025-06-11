package edu.boun.edgecloudsim.sample_voronoi_app;

import edu.boun.edgecloudsim.sample_voronoi_app.Point;
import edu.boun.edgecloudsim.sample_voronoi_app.Voronoi;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

import static java.lang.Math.PI;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.PI;
import static java.lang.Math.max;

class Algs {
    public static double DummyAlg(ArrayList<Point> points, int granularity)
    {
        boolean debug = false;
        int mapsize = granularity+1;
        double trate = granularity/50.0;
        int len = points.size();
        double bestRadius = 25*25*(PI*Math.sqrt(3)/6.0);
        bestRadius = Math.sqrt(bestRadius/len);
        bestRadius = bestRadius*trate;
        boolean[][] map = new boolean[mapsize][mapsize];
        int ValidCount = 0;
        for(int i=0;i<mapsize;i++)
            for(int j=0;j<mapsize;j++) {
                map[i][j] = false;
                if((i-(double)mapsize/2)*(i-(double)mapsize/2)+(j-(double)mapsize/2)*(j-(double)mapsize/2) <= (double)mapsize*mapsize/4)
                    ValidCount++;
            }
        for(Point p:points)
        {
            double tx = p.x*trate;
            double ty = p.y*trate;
            int sx = (int)(tx - bestRadius);
            int sy = (int)(ty - bestRadius);
            for(int r=0;r<bestRadius*2+1;r++)
            {
                for(int c=0;c<bestRadius*2+1;c++)
                {
                    int rx = sx+r;
                    int ry = sy+c;
                    if(rx>=0 && rx<mapsize && ry>=0 && ry<mapsize)
                        if((tx-rx)*(tx-rx)+(ty-ry)*(ty-ry)<=bestRadius*bestRadius)
                            map[ry][rx]=true;
                }
            }
        }
        int covered = 0;
        for(int i=0;i<mapsize;i++)
            for(int j=0;j<mapsize;j++)
                if(map[i][j]==true)
                    covered ++;
        return (double)covered/ValidCount;
    }
}

class KDNode{
    public KDNode(){
        dividePoint = null;
        leftChild = null;
        rightChild = null;
    }
    Point dividePoint;
    KDNode leftChild;
    KDNode rightChild;
    boolean xaxis;
}

/*class dPoint{
    double x,y;
    public dPoint(){x=0;y=0;};
    public dPoint(double dx, double dy){x=dx;y=dy;}
    public dPoint(float dx, float dy){x=dx;y=dy;}
    public dPoint Copy(){
        return new dPoint(x,y);
    }
}*/

public class KDTree{

    protected KDNode ROOT;
    protected ArrayList<KDNode> nn;
    protected double[] nnsdist;
    protected int k;

    private static String fogNodeFileName = "fogNode_loc.json";
    private static String mobileDeviceFileName = "mobile_loc.json";

    static ArrayList<Point> fogNodePoints = new ArrayList<Point>();
    static ArrayList<Point> mobileDevicePoints = new ArrayList<Point>();
    static HashMap<Point, String> fogNodesDic = new HashMap<Point, String>();
    static HashMap<Point, String> mobileDeviceDic = new HashMap<Point, String>();
    boolean debug = false;

    public static void coordinateProjection(String fogNodeFileName, String mobileDeviceFileName){
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
        double scaling_factor = 1; //0.000032;
        //double x_translation = -6.0;
        //double y_translation = 152.0;

        //double scaling_factor_m = 0.000024;
        //double x_translation_m = -4.5;
        //double y_translation_m = 114.0;

//        ArrayList<Point> fogNodePoints = new ArrayList<Point>();
//        ArrayList<Point> mobileDevicePoints = new ArrayList<Point>();

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
                        Point pt = new Point(x, y);
                        fogNodePoints.add(pt);
                        fogNodesDic.put(pt, key);
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
            scaling_factor = 1024.0/(10.0* Math.max((x_max-x_min),(y_max-y_min)));
//            scaling_factor = 1024.0/(Math.max((x_max-x_min),(y_max-y_min)));
            System.out.println("scaling_factor = "+scaling_factor);
            //System.exit(0);

            //translate the points according to the centroid and add them to the list
            for (Point p: fogNodePoints) {
                //System.out.println(p.x + ", "+ p.y);
                String dic_value = fogNodesDic.get(p);
                Point original_p = new Point(p.x, p.y);
                p.x -= centroid_x;
                p.x *= scaling_factor;
                p.y -= centroid_y;
                p.y *= scaling_factor;
                fogNodesDic.put(p, dic_value);
                fogNodesDic.remove(original_p);

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
                        Point pt = new Point(x, y);
                        mobileDevicePoints.add(pt);
                        mobileDeviceDic.put(pt, key);
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
//            scaling_factor = 1024.0/(10.0* Math.max((x_max-x_min),(y_max-y_min)));
            scaling_factor = 1024.0/(Math.max((x_max-x_min),(y_max-y_min)));
            System.out.println("scaling_factor = "+scaling_factor);
            System.out.println("-----------------------------");
            System.out.println();
            System.out.println();

            System.out.println("fog node number and its projected coordinates");
            for (Point p : fogNodesDic.keySet()) {
                System.out.println("fog node "+ fogNodesDic.get(p)+ ": " + p);
            }
            System.out.println();
            System.out.println();
            System.out.println("-----------------------------");
            //System.exit(0);

            //translate the points according to the centroid and add them to the list
            for (Point p: mobileDevicePoints) {
                //System.out.println(p.x + ", "+ p.y);
                String dic_value = mobileDeviceDic.get(p);
                Point original_p = new Point(p.x, p.y);
                p.x -= centroid_x;
                p.x *= scaling_factor;
                p.y -= centroid_y;
                p.y *= scaling_factor;
                mobileDeviceDic.put(p, dic_value);
                mobileDeviceDic.remove(original_p);
            }


//            double distance_mobile_fog_node; // without square root operation for speed up
//            Map<Integer,List<Integer>> dic_rslts = new HashMap<>();
//            int index_mobile=0;
//
//            for (Point m: mobileDevicePoints) {
//                //System.out.println(p.x + ", "+ p.y);
//                ArrayList<Double> temp_distance = new ArrayList<>();
//                for (Point f: fogNodePoints){
//                    distance_mobile_fog_node = (m.x-f.x)*(m.x-f.x) + (m.y-f.y)*(m.y-f.y);
//                    temp_distance.add(distance_mobile_fog_node);
//                }
//                List<Integer> indices = new ArrayList<>(IntStream.range(0, temp_distance.size()).boxed().toList());
//                Collections.sort(indices, (k, l) -> temp_distance.get(k).compareTo(temp_distance.get(l)));
//                dic_rslts.put(index_mobile, indices);
//                index_mobile++;
//            }
//
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            String json = gson.toJson(dic_rslts);
//            try (FileWriter writer = new FileWriter("rslts_dict.json")) {
//                writer.write(json);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }



//            Voronoi fog_node_voronoi = new Voronoi(fogNodePoints);
//            fog_node_voronoi.plot(fogNodePoints, mobileDevicePoints);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void coordinateProjection(String fogNodeFileName, Point mobileDevice){
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
        double scaling_factor = 1; //0.000032;

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
                        Point pt = new Point(x, y);
                        fogNodePoints.add(pt);
                        //fogNodesDic.put(pt, key);
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
            scaling_factor = 1024.0/(10.0* Math.max((x_max-x_min),(y_max-y_min)));
//            scaling_factor = 1024.0/(Math.max((x_max-x_min),(y_max-y_min)));
            System.out.println("scaling_factor = "+scaling_factor);
            //System.exit(0);

            //translate the points according to the centroid and add them to the list
            for (Point p: fogNodePoints) {
                //System.out.println(p.x + ", "+ p.y);
                String dic_value = fogNodesDic.get(p);
                Point original_p = new Point(p.x, p.y);
                p.x -= centroid_x;
                p.x *= scaling_factor;
                p.y -= centroid_y;
                p.y *= scaling_factor;
                fogNodesDic.put(p, dic_value);
                //fogNodesDic.remove(original_p);

            }

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

            latitude = mobileDevice.y;//Double.parseDouble(childObject.get("latitude").toString());
            longitude = mobileDevice.x;//.parseDouble(childObject.get("longitude").toString());
            sum_latitude += latitude;
            sum_longitude += longitude;


            x = R * Math.cos(Math.PI * latitude / 180.0) * Math.cos(Math.PI * longitude / 180.0);
            y = R * Math.cos(Math.PI * latitude / 180.0) * Math.sin(Math.PI * longitude / 180.0);
            if (x < x_min) x_min = x;
            if (x > x_max) x_max = x;
            if (y < y_min) y_min = y;
            if (y > y_max) y_max = y;
            Point pt = new Point(x, y);
            mobileDevicePoints.add(pt);

            mobileDeviceDic.put(pt, key);


            //System.out.println("points = "+fogNodePoints+"\n"+mobileDevicePoints);
            centroid_longitude = sum_longitude/num_nodes;
            centroid_latitude = sum_latitude/num_nodes;
            System.out.println("centroid_longitude = "+centroid_longitude+" centroid_latitude = "+centroid_latitude);
            centroid_x =  R * Math.cos(Math.PI * centroid_latitude / 180.0) * Math.cos(Math.PI * centroid_longitude / 180.0);;
            centroid_y = R * Math.cos(Math.PI * centroid_latitude / 180.0) * Math.sin(Math.PI * centroid_longitude / 180.0);;
            System.out.println("centroid_x = "+centroid_x+" centroid_y = "+centroid_y);
//            scaling_factor = 1024.0/(10.0* Math.max((x_max-x_min),(y_max-y_min)));
            scaling_factor = 1024.0/(Math.max((x_max-x_min),(y_max-y_min)));
            System.out.println("scaling_factor = "+scaling_factor);
            System.out.println("-----------------------------");
            System.out.println();
            System.out.println();

            System.out.println("fog node number and its projected coordinates");
            for (Point p : fogNodesDic.keySet()) {
                System.out.println("fog node "+ fogNodesDic.get(p)+ ": " + p);
            }
            System.out.println();
            System.out.println();
            System.out.println("-----------------------------");
            //System.exit(0);

            //translate the points according to the centroid and add them to the list
            for (Point p: mobileDevicePoints) {
                //System.out.println(p.x + ", "+ p.y);
                String dic_value = mobileDeviceDic.get(p);
                Point original_p = new Point(p.x, p.y);
                p.x -= centroid_x;
                p.x *= scaling_factor;
                p.y -= centroid_y;
                p.y *= scaling_factor;
                mobileDeviceDic.put(p, dic_value);
                mobileDeviceDic.remove(original_p);
                System.out.println("mobileDevicePoint = ("+pt.x+","+pt.y+")");
            }

//            Voronoi fog_node_voronoi = new Voronoi(fogNodePoints);
//            fog_node_voronoi.plot(fogNodePoints, mobileDevicePoints);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public KDTree(ArrayList<Point> pointlist){
        ROOT = new KDNode();
        BuildTree(pointlist, ROOT, 0, pointlist.size(), true, true);
    }
    public void DFS()
    {
        DFS(ROOT.leftChild);
    }
    public ArrayList<Point> KNN(int k, Point center){
        this.k = k;
        this.nn = new ArrayList<>();
        this.nnsdist = new double[k];
        KNN(center, ROOT.leftChild);
        ArrayList<Point> knn = new ArrayList<>();
        for(KDNode x:nn)
        {
            knn.add(x.dividePoint.Copy());
        }
        return knn;
    }
    protected void KNN(Point center, KDNode root)
    {
        if(root==null)
            return;
//        System.out.println(root.dividePoint.x+","+root.dividePoint.y+(root.xaxis?",x,":",y,")+nn.size());
        //if goto left part
        boolean leftpart = root.xaxis && center.x<root.dividePoint.x || !root.xaxis && center.y<root.dividePoint.y;
        if(leftpart)
            KNN(center, root.leftChild);
        else
            KNN(center, root.rightChild);
        double cdist = (center.x-root.dividePoint.x)*(center.x-root.dividePoint.x)+
                (center.y-root.dividePoint.y)*(center.y-root.dividePoint.y);
        if(nn.size()<k)
        {
            nnsdist[nn.size()] = cdist;
            nn.add(root);
        }
        else
        {
            int maxidx = 0;
            for(int i=1;i<k;i++)
                if(nnsdist[i]>nnsdist[maxidx])
                    maxidx = i;
            //if nearer, replace the maximun distance nn point with current point
            if(nnsdist[maxidx]>cdist)
            {
                nn.set(maxidx, root);
                nnsdist[maxidx]=cdist;
            }
            if(root.xaxis && nnsdist[maxidx]<Math.abs(center.x-root.dividePoint.x))
                return;
            if(!root.xaxis && nnsdist[maxidx]<Math.abs(center.y-root.dividePoint.y))
                return;
        }
        if(leftpart)
            KNN(center, root.rightChild);
        else
            KNN(center, root.leftChild);
    }
    protected void DFS(KDNode root)
    {
        System.out.println(root.dividePoint.x+","+root.dividePoint.y);
        if(root.leftChild!=null)
            DFS(root.leftChild);
        if(root.rightChild!=null)
            DFS(root.rightChild);
    }
    protected void BuildTree(ArrayList<Point> pointlist,KDNode root , int start, int end, boolean xaxis, boolean left){
        if(start>=end)
            return;
        int mid = start;
        if(start+1<end) {
            if (xaxis)
                pointlist.subList(start, end).sort((Point p1, Point p2) -> Double.compare(p1.x, p2.x));
            else
                pointlist.subList(start, end).sort((Point p1, Point p2) -> Double.compare(p1.y, p2.y));
            mid = (start + end) / 2;
        }
        KDNode node = new KDNode();
        node.dividePoint = new Point(pointlist.get(mid).x, pointlist.get(mid).y);
        node.xaxis = xaxis;
        if(left)
            root.leftChild = node;
        else
            root.rightChild = node;
        BuildTree(pointlist,node,start,mid,!xaxis,true);
        BuildTree(pointlist,node,mid+1,end,!xaxis,false);
    }

    public Point getNearestFogNode(String fogNodeFileName, Point givenPoint){
        Point nearestFogNode = new Point();
        int k_nearest = 1;
        //coordinateProjection(fogNodeFileName, mobileDeviceFileName);
        coordinateProjection(fogNodeFileName, givenPoint);
        ArrayList<Point> testlist = new ArrayList<Point>();
        HashMap<Point, ArrayList<Point>> rsltsDic = new HashMap<Point, ArrayList<Point>>();

        for(Point p: fogNodePoints){
            testlist.add(new Point(p.x,p.y));
        }
        KDTree tree = new KDTree(testlist);

        for(Point p: mobileDevicePoints){
            ArrayList<Point> knn = tree.KNN(k_nearest,new Point(p.x,p.y));
            rsltsDic.put(p, knn);
        }

        for(Point p: rsltsDic.keySet()){
            if(debug) System.out.print("Mobile Device "+mobileDeviceDic.get(p)+": Fog Node ");

            for(Point x: rsltsDic.get(p)){
                Point pt = new Point((double)x.x, (double)x.y);
                String temp = fogNodesDic.get(pt);
                if(debug) System.out.print(fogNodesDic.get(pt)+' ');
            }
            System.out.print(" => " + p+": ");
            for(Point x: rsltsDic.get(p)){
                //if(p.equals(new Point(0.8452383852704767, -0.0051038269039367))) {
                if(debug) System.out.print("(" + x.x + "," + x.y + ") ");
                nearestFogNode = x;
                //}
            }
            if(debug) System.out.println();
        }
        return nearestFogNode;
    }

    public Point getNearestFogNode(List<Point> fogNodeLongLat, Point devicePoint){
        Point nearestFogNode = new Point();
        int k_nearest = 1;
        //coordinateProjection(fogNodeFileName, mobileDeviceFileName);
        //coordinateProjection(fogNodeFileName, givenPoint);
        ArrayList<Point> testlist = new ArrayList<Point>();
        HashMap<Point, ArrayList<Point>> rsltsDic = new HashMap<Point, ArrayList<Point>>();

        for(Point p: fogNodeLongLat){
            testlist.add(new Point(p.x,p.y));
        }
        KDTree tree = new KDTree(testlist);

        //for(Point p: mobileDevicePoints){
        ArrayList<Point> knn = tree.KNN(k_nearest,new Point(devicePoint.x,devicePoint.y));
        rsltsDic.put(devicePoint, knn);
        //}

        for(Point p: rsltsDic.keySet()){
            if(debug) System.out.print("Mobile Device "+mobileDeviceDic.get(p)+": Fog Node ");

            for(Point x: rsltsDic.get(p)){
                Point pt = new Point((double)x.x, (double)x.y);
                String temp = fogNodesDic.get(pt);
                if(debug) System.out.print(fogNodesDic.get(pt)+' ');
            }
            System.out.print(" => " + p+": ");
            for(Point x: rsltsDic.get(p)){
                //if(p.equals(new Point(0.8452383852704767, -0.0051038269039367))) {
                if(debug) System.out.print("(" + x.x + "," + x.y + ") ");
                nearestFogNode = x;
                //}
            }
            if(debug) System.out.println();
        }
        return nearestFogNode;
    }

    public static void main(String[] args) {
        int k_nearest = 1;
        coordinateProjection(fogNodeFileName, mobileDeviceFileName);
        ArrayList<Point> testlist = new ArrayList<Point>();
        HashMap<Point, ArrayList<Point>> rsltsDic = new HashMap<Point, ArrayList<Point>>();

        for(Point p: fogNodePoints){
            testlist.add(new Point(p.x,p.y));
        }
        KDTree tree = new KDTree(testlist);

        for(Point p: mobileDevicePoints){
            ArrayList<Point> knn = tree.KNN(k_nearest,new Point(p.x,p.y));
            rsltsDic.put(p, knn);
        }

        for(Point p: rsltsDic.keySet()){
            System.out.print("Mobile Device "+mobileDeviceDic.get(p)+": Fog Node ");

            for(Point x: rsltsDic.get(p)){
                Point pt = new Point((double)x.x, (double)x.y);
                String temp = fogNodesDic.get(pt);
                System.out.print(fogNodesDic.get(pt)+' ');
            }
            System.out.print(" => " + p+": ");
            for(Point x: rsltsDic.get(p)){
                //if(p.equals(new Point(0.8452383852704767, -0.0051038269039367))) {
                    System.out.print("(" + x.x + "," + x.y + ") ");
                //}
            }
            System.out.println();
        }
    }
}

