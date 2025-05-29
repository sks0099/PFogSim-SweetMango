package edu.boun.edgecloudsim.sample_voronoi_app;

//import javafx.util.Pair;
//import org.apache.commons.lang3.tuple.Pair;

import org.apache.commons.math3.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import static edu.boun.edgecloudsim.sample_voronoi_app.KDTree.*;

public class VoronoiPlot extends JPanel {

    private int width = 800;
    private int heigth = 400;
    private int padding = 2;//25;
    private int labelPadding = 2;//25;
    private Color lineColor = new Color(44, 102, 230, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private Color fogNodeColor = new Color(255, 0, 0, 200);
    private Color mobileDeviceColor = new Color(135, 206, 235, 255);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 1;
    private int numberYDivisions = 10;
    private List<Double> scores;

    private ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> fogNodePts;
    private ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> mobileDevicePts;
    private List<edu.boun.edgecloudsim.sample_voronoi_app.Edge> edges;

    private static String fogNodeFileName = "fogNode_loc.json";
    private static String mobileDeviceFileName = "mobile_loc.json";

    public static double centroid_x =  0.0;
    public static double centroid_y = 0.0;
    public static double scaling_factor = 1.0;
    public static Point getScaledPoint(Point pointXY){
        //Point scaledPoint = new Point();
        double scaledPointX = (pointXY.x - centroid_x)*scaling_factor;
        double scaledPointY = (pointXY.y - centroid_y)*scaling_factor;
        return (new Point(scaledPointX, scaledPointY));
    }
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
        //scaling_factor = 1.0; //0.000032;
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
            //double centroid_x =  0.0;
            //double centroid_y = 0.0;
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
            /*for (edu.boun.edgecloudsim.sample_voronoi_app.Point p: fogNodePoints) {
                //System.out.println(p.x + ", "+ p.y);
                p.x -= centroid_x;
                p.x *= scaling_factor;
                p.y -= centroid_y;
                p.y *= scaling_factor;
            }*/
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

            for (edu.boun.edgecloudsim.sample_voronoi_app.Point p: fogNodePoints) {
                //System.out.println(p.x + ", "+ p.y);
                p.x -= centroid_x;
                p.x *= scaling_factor;
                p.y -= centroid_y;
                p.y *= scaling_factor;
            }

            //translate the points according to the centroid and add them to the list
            for (edu.boun.edgecloudsim.sample_voronoi_app.Point p: mobileDevicePoints) {
                //System.out.println(p.x + ", "+ p.y);
                p.x -= centroid_x;
                p.x *= scaling_factor;
                p.y -= centroid_y;
                p.y *= scaling_factor;
            }

            //Voronoi fog_node_voronoi = new Voronoi(fogNodePoints);
            Voronoi fog_node_voronoi = new Voronoi(fogNodeFileName);
            System.out.println(fog_node_voronoi.fogNodesLongLat);
            List<Point> fogNodesLongLatList = new ArrayList<>(fog_node_voronoi.fogNodesLongLat);
            fog_node_voronoi.sortedFogNodesLongLat = new ArrayList<>(fog_node_voronoi.sortArray(fogNodesLongLatList));
            System.out.println("fog_node_voronoi.sortedFogNodesLongLat = \n"+fog_node_voronoi.sortedFogNodesLongLat);
            fog_node_voronoi.setFogNodeXYCoordinate(fogNodesLongLatList);
            fog_node_voronoi.setSortedFogNodesXY(fog_node_voronoi.fogNodesXY);
            System.out.println("fog_node_voronoi.setSortedFogNodesXY = \n"+fog_node_voronoi.sortedFogNodesXY);
            //fog_node_voronoi.set
            /*List<Point> testList = new ArrayList();
            List<Point> sortedList = new ArrayList();
            testList.add(new Point(1.0,1.0));
            testList.add(new Point(2.0, 3.0));
            testList.add(new Point(2.0,2.999));
            testList.add(new Point(4.0,4.0));
            sortedList = fog_node_voronoi.sortArray(testList);
            System.out.println("sortedlist = "+sortedList);
            List<Point> testList1 = new ArrayList();
            List<Point> sortedList1 = new ArrayList();
            testList1.add(new Point(2.0,2.999));
            testList1.add(new Point(4.0,4.0));
            testList1.add(new Point(1.0,1.0));
            testList1.add(new Point(2.0, 3.0));
            sortedList1 = fog_node_voronoi.sortArray(testList1);
            System.out.println("First List equals to Second list: " +
                    sortedList.equals(sortedList1));*/
            //System.exit(24);
            //Point host = fog_node_voronoi.getHost(new Point(41.99191752,-87.7983019), fogNodeFileName);
            List<Point> devicesToBeHosted = new ArrayList<>();
            ArrayList<Pair<Point, Point>> deviceToBeHosted_Host_PairList = new ArrayList<>();
            devicesToBeHosted.add(new Point(-87.7983019, 41.99191752));
            devicesToBeHosted.add(new Point(-87.7483019, 41.99));
            devicesToBeHosted.add(new Point(-87.7483019, 41.79));
            devicesToBeHosted.add(new Point(-87.73, 41.89));
            devicesToBeHosted.add(new Point(-87.68, 41.79));
            devicesToBeHosted.add(new Point(-87.70, 41.91));
            devicesToBeHosted.add(new Point(-87.65, 41.91));
            for(Point devToBeHosted: devicesToBeHosted) {
                //Point devToBeHosted = ;
                Point host = fog_node_voronoi.getHostLongLat(devToBeHosted);
                //System.out.println("host = ("+host.x+","+host.y+")");
                Point devToBeHostedXY = devToBeHosted.convertLongLatPointToXYCoordinates();
                Point hostXY = fog_node_voronoi.getHostXY(devToBeHostedXY);
                //System.out.println("hostXY = ("+hostXY.x+","+hostXY.y+")");
                //System.exit(25);
                Point devToBeHostedXYScaled = getScaledPoint(devToBeHostedXY);
                Point hostXYScaled = getScaledPoint(hostXY);
                Pair<Point, Point> deviceToBeHosted_Host_Pair = new Pair<>(devToBeHostedXYScaled, hostXYScaled);
                deviceToBeHosted_Host_PairList.add(deviceToBeHosted_Host_Pair);
            }
            //fog_node_voronoi.plot(fogNodePoints, mobileDevicePoints, devToBeHostedXYScaled, hostXYScaled);
            fog_node_voronoi.plot(fogNodePoints, mobileDevicePoints, deviceToBeHosted_Host_PairList);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    /*public VoronoiPlot(List<Double> scores) {
        this.scores = scores;
    }*/

    public VoronoiPlot(ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> fogNodePts,
                       ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> mobileDevicePts,
                       List<edu.boun.edgecloudsim.sample_voronoi_app.Edge> edges) {
        this.fogNodePts = fogNodePts;
        this.mobileDevicePts = mobileDevicePts;
        this.edges = edges;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double xMinVal = getXMinVal(fogNodePts, mobileDevicePts);
        double xMaxVal = getXMaxVal(fogNodePts, mobileDevicePts);
        double yMinVal = getYMinVal(fogNodePts, mobileDevicePts);
        double yMaxVal = getYMaxVal(fogNodePts, mobileDevicePts);
        double xCentroid = getXCentroid(fogNodePts, mobileDevicePts);
        double yCentroid = getYCentroid(fogNodePts, mobileDevicePts);
        //System.out.println("xCentroid = "+xCentroid+", yCentroid = "+yCentroid);
        /*double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (scores.size() - 1);
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());*/
        //System.out.println("xMaxVal = "+xMaxVal+", xMinVal = "+xMinVal);
        //System.out.println("yMaxVal = "+yMaxVal+", yMinVal = "+yMinVal);
        double xScale = ((double) getWidth() - 2 * padding - labelPadding) / (xMaxVal - xMinVal);
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (yMaxVal - yMinVal);
        //System.out.println("width = "+getWidth()+", height = "+getHeight());
        List<edu.boun.edgecloudsim.sample_voronoi_app.Point> graphFNPoints = new ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point>();
        List<edu.boun.edgecloudsim.sample_voronoi_app.Point> graphMDPoints = new ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point>();
        List<double[]> graphEdges = new ArrayList<>();


        for (edu.boun.edgecloudsim.sample_voronoi_app.Point fnp: fogNodePts){
            double x1 = (fnp.x - xCentroid) * xScale + padding;
            double y1 = (fnp.y - yCentroid) * yScale + padding;
            graphFNPoints.add(new edu.boun.edgecloudsim.sample_voronoi_app.Point(x1, y1));
        }

        for (edu.boun.edgecloudsim.sample_voronoi_app.Point mdp: mobileDevicePts){
            double x1 = (mdp.x - xCentroid) * xScale + padding;
            double y1 = (mdp.y - yCentroid) * yScale + padding;
            graphMDPoints.add(new edu.boun.edgecloudsim.sample_voronoi_app.Point(x1, y1));
        }

        //System.out.println("Number of edges = "+edges.size());
        double xStart = 0.0;
        double yStart = 0.0;
        double xEnd = 0.0;
        double yEnd = 0.0;
        for (int i = 0; i < edges.size(); i++) {
            /*System.out.println(edges.get(i));
            System.out.println(edges.get(i).start);
            System.out.println(edges.get(i).end);*/
            xStart = (edges.get(i).start.x - xCentroid) * xScale + padding;
            yStart = (edges.get(i).start.y - yCentroid) * yScale + padding;
            xEnd = (edges.get(i).end.x - xCentroid) * xScale + padding;
            yEnd = (edges.get(i).end.y - yCentroid) * yScale + padding;
            double[] xy = new double[]{xStart, yStart, xEnd, yEnd};
            graphEdges.add(xy);
        }
        // draw white background
        g2.setColor(Color.WHITE);
        //g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        //g2.fillRect(-getWidth() + (2 * padding) + labelPadding, -getHeight() + 2 * padding + labelPadding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.translate(getWidth()/2, getHeight()/2);
        g2.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.

        // and for x axis


        // create x and y axes
        /*g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);
        */

        Stroke oldStroke = g2.getStroke();
        g2.setColor(fogNodeColor);
        g2.setStroke(GRAPH_STROKE);

        g2.setStroke(oldStroke);

        Label label = new Label("");
        int node_count = 0;
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point gpfn: graphFNPoints) {
            double x = gpfn.x;// - pointWidth / 2;
            double y = gpfn.y;// - pointWidth / 2;
            //double div_x = 0.2;
            //double div_y = 0.2;
            int POINT_SIZE = 4;


            //int ovalW = pointWidth;
            //int ovalH = pointWidth;
            //System.out.println("x = "+x+", y = "+y);
            //Shape point =  new Ellipse2D.Double(x/div_x, y/div_y, POINT_SIZE, POINT_SIZE);
            Shape point =  new Ellipse2D.Double(x, -y, POINT_SIZE, POINT_SIZE);

            //System.out.println(point);
            g2.setColor(Color.RED);
            g2.draw(point);
            /*if(node_count == 0)  g2.setColor(Color.RED);
            if(node_count == 1)  g2.setColor(Color.GREEN);
            if(node_count == 2)  g2.setColor(Color.BLUE);
            if(node_count == 3)  g2.setColor(Color.BLACK);*/

            // Fill the ellipse
            g2.fill(point);
            label.setText("("+x+","+y+")");
            g2.drawString("("+new DecimalFormat("#.###").format(x)+","+new DecimalFormat("#.###").format(y)+")", (float)(gpfn.x+5.0), (float)(-gpfn.y+3.0));
            node_count += 1;
        }

        g2.setColor(mobileDeviceColor);
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point gpmd: graphMDPoints) {
            double x = gpmd.x - pointWidth / 2;
            double y = gpmd.y - pointWidth / 2;
            int POINT_SIZE = 1;

            Shape point =  new Ellipse2D.Double(x, -y, POINT_SIZE, POINT_SIZE);

            g2.draw(point);

            // Fill the ellipse
            g2.fill(point);
        }
        g2.setColor(lineColor);
        for (int i = 0; i < graphEdges.size(); i++) {
            xStart = graphEdges.get(i)[0];// - pointWidth / 2;
            yStart = graphEdges.get(i)[1];// - pointWidth / 2;
            xEnd = graphEdges.get(i)[2];// - pointWidth / 2;
            yEnd = graphEdges.get(i)[3];// - pointWidth / 2;
            Line2D line = new Line2D.Double(xStart, -yStart, xEnd, -yEnd);

            g2.draw(line);
        }
    }

    //    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(width, heigth);
//    }
    private double getMinScore() {
        double minScore = Double.MAX_VALUE;
        for (Double score : scores) {
            minScore = Math.min(minScore, score);
        }
        return minScore;
    }

    private double getMaxScore() {
        double maxScore = Double.MIN_VALUE;
        for (Double score : scores) {
            maxScore = Math.max(maxScore, score);
        }
        return maxScore;
    }

    private double getXMinVal(ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> fogNodePts, ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> mobileDevicePts) {
        double minXVal = Double.MAX_VALUE;
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point fnp: fogNodePts) {
            minXVal = Math.min(minXVal, fnp.x);
        }
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point mdp: mobileDevicePts) {
            minXVal = Math.min(minXVal, mdp.x);
        }
        return minXVal;
    }

    private double getXMaxVal(ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> fogNodePts, ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> mobileDevicePts) {
        double maxXVal = Double.MIN_VALUE;
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point fnp: fogNodePts) {
            maxXVal = Math.max(maxXVal, fnp.x);
        }
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point mdp: mobileDevicePts) {
            maxXVal = Math.max(maxXVal, mdp.x);
        }
        return maxXVal;
    }

    private double getYMinVal(ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> fogNodePts, ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> mobileDevicePts) {
        double minYVal = Double.MAX_VALUE;
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point fnp: fogNodePts) {
            minYVal = Math.min(minYVal, fnp.y);
        }
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point mdp: mobileDevicePts) {
            minYVal = Math.min(minYVal, mdp.y);
        }
        return minYVal;
    }

    private double getYMaxVal(ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> fogNodePts, ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> mobileDevicePts) {
        double maxYVal = Double.MIN_VALUE;
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point fnp: fogNodePts) {
            maxYVal = Math.max(maxYVal, fnp.y);
        }
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point mdp: mobileDevicePts) {
            maxYVal = Math.max(maxYVal, mdp.y);
        }
        return maxYVal;
    }

    private double getXCentroid(ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> fogNodePts, ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> mobileDevicePts) {
        //double xCentroid = 0.0;
        int count = 0;
        double sumX = 0.0;
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point fnp: fogNodePts) {
            //System.out.println("fnp.x = "+fnp.x);
            sumX += fnp.x;
            count += 1;
            //System.out.println("sumX = "+sumX+", count = "+count);
        }
        return (sumX/count);
    }

    private double getYCentroid(ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> fogNodePts, ArrayList<edu.boun.edgecloudsim.sample_voronoi_app.Point> mobileDevicePts) {
        //double xCentroid = 0.0;
        int count = 0;
        double sumY = 0.0;
        for (edu.boun.edgecloudsim.sample_voronoi_app.Point fnp: fogNodePts) {
            sumY += fnp.y;
            count += 1;
        }
        return (sumY/count);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                /*int k_nearest = 1;
                coordinateProjection(fogNodeFileName, mobileDeviceFileName);
                ArrayList<Point> testlist = new ArrayList<Point>();
                HashMap<Point, ArrayList<Point>> rsltsDic = new HashMap<Point, ArrayList<Point>>();
                for(Point p: fogNodePoints){
                    testlist.add(new Point(p.x,p.y));
                }
                System.out.println("testlist.size() = "+testlist.size());
                int pointCnt = 0;
                for(Point p: testlist){
                    pointCnt++;
                    System.out.println("testlist points "+pointCnt+": "+p.x+", "+p.y);
                }
                //System.exit(99);
                KDTree tree = new KDTree(testlist);
                pointCnt = 0;
                for(Point p: mobileDevicePoints){
                    ArrayList<Point> knn = tree.KNN(k_nearest,new Point(p.x,p.y));

                    for(Point pk: knn){
                        pointCnt++;
                        //System.out.println("knn points "+pointCnt+": "+pk.x+", "+pk.y);
                    }
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
                        System.out.print("("+x.x+","+x.y+") ");
                    }
                    System.out.println();
                }*/

                createFogNodePlot(fogNodeFileName, mobileDeviceFileName);
                //createAndShowGui();
            }
        });
    }
}