package edu.boun.edgecloudsim.sample_voronoi_app;

//import javafx.util.Pair;

import org.apache.commons.math3.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

import static edu.boun.edgecloudsim.sample_voronoi_app.KDTree.*;
import static edu.boun.edgecloudsim.sample_voronoi_app.KDTree.fogNodesDic;

public class Voronoi {
    boolean debug = false;

    private List <Point> sites;
    public List <Edge> edges; // edges on Voronoi diagram
    PriorityQueue<Event> events; // priority queue represents sweep line
    Parabola root; // binary search tree represents beach line
    public List <Point> partitionVertices; // vertices of Voronoi partitions
    private List <Edge> partitionEdges; // edges of Voronoi partitions
    public HashMap<Point, List<Point>> VoronoiPartitionVertexMap = new HashMap<>();
    public HashMap<Point, List<Pair<Point, Point>>> VoronoiPartitionEdgeMap = new HashMap<>();
    private HashMap<Point, List<Point>> VoronoiNeighborSiteMap = new HashMap<>();
    public ArrayList<Point> fogNodesLongLat = new ArrayList<>();
    public ArrayList<Point> sortedFogNodesLongLat = new ArrayList<>();
    public ArrayList<Point> fogNodesXY = new ArrayList<>();
    public ArrayList<Point> sortedFogNodesXY = new ArrayList<>();
    private ArrayList<Point> mobileDevicePoints = new ArrayList<Point>();
    private HashMap<Point, String> fogNodesDic = new HashMap<Point, String>();
    private HashMap<Point, String> mobileDeviceDic = new HashMap<Point, String>();


    // size of StdDraw window
    double width = 1; //Original
    //double width = -100;
    double height = 1; //Original
    //double height = -100;

    double epsilon = 1.0e-4;

    double ycurr; // current y-coord of sweep line

    boolean showPartitionVertexLabel = true;//false;
    boolean showPartitionEdgeLabel = false;//true;//false;


    // Create a class constructor for the Voronoi class using fog node coordinates as a json file
    public Voronoi (String  fogNodeFileName) {
        setFogNodesLongLat(fogNodeFileName);
        this.sites = fogNodesLongLat;
        edges = new ArrayList<Edge>();
        partitionVertices = new ArrayList<Point>();
        partitionEdges = new ArrayList<Edge>();
        generateVoronoi();
    }

    // Create a class constructor for the Voronoi class using fog node points
    public Voronoi (List <Point> sites) {
        this.sites = sites;
        edges = new ArrayList<Edge>();
        partitionVertices = new ArrayList<Point>();
        partitionEdges = new ArrayList<Edge>();
        generateVoronoi();
    }

    // The method getVoronoiPartitionVertexMap() returns the List of VoronoiPartitionVertexMap.
    public HashMap<Point, List<Point>> getVoronoiPartitionVertexMap(){
        return this.VoronoiPartitionVertexMap;
    }

    // The method getVoronoiPartitionEdgeMap() returns the List of VoronoiNeighborEdgeMap.
    public HashMap<Point, List<Pair<Point, Point>>> getVoronoiPartitionEdgeMap(){
        return this.VoronoiPartitionEdgeMap;
    }

    // The method getVoronoiNeighborSiteMap() returns the List of VoronoiNeighborSiteMap.
    public HashMap<Point, List<Point>> getVoronoiNeighborSiteMap(){
        return this.VoronoiNeighborSiteMap;
    }

    // The method List<Point> sortArray(List<Point> inputList) sorts the inputList
    // containing the list of points with 2 coordinates such as (x, y) or
    // (Longitude, Latitude) lexicographically.
    public List<Point> sortArray(List<Point> inputList){
        List<Point> retList;
        Collections.sort(inputList, new PointComparator());
        if(debug) System.out.println("Sorted lexicographically: " + inputList);
        return inputList;
    }

    // The method getHost(Point devToBeHosted, String fogNodeFileName) returns
    // the fog node's (Longitude, Latitude) coordinates for a given IoT/Mobile device
    // devToBeHosted and the json file with the longitudes/latitudes of fogNodes.
    // IoT/Mobile Device is identified by its coordinates. In the future, other
    // parameters such as latency, CPU, RAM, Memory storage requirements will
    // be taken into account. Host assignment will change according to the requirement(s).
    public Point getHost(Point devToBeHosted, String fogNodeFileName){
        Point host = new Point();

        ArrayList<Point> fogNodelist = new ArrayList<Point>();
        //HashMap<Point, ArrayList<Point>> rsltsDic = new HashMap<Point, ArrayList<Point>>();
        for(Point p: fogNodePoints){
            fogNodelist.add(new Point(p.x,p.y));
            System.out.print("fog node: (" + host.x + "," + host.y + ") ");
        }

        KDTree kdtree = new KDTree(fogNodesLongLat);
        host = kdtree.getNearestFogNode(fogNodeFileName, devToBeHosted);
        if(debug) System.out.print("Host for ("+devToBeHosted.x+","+devToBeHosted.y+") = (" + host.x + "," + host.y + ") ");

        return host;
    }

    // The method getHostLongLat(Point devToBeHosted) returns the fog node's
    // (Longitude, Latitude) coordinates for a given IoT/Mobile device
    // devToBeHosted. IoT/Mobile Device is identified by its coordinates
    // In the future, other parameters such as latency, CPU, RAM, Memory storage
    // requirements will be taken into account. Host assignment will change
    // according to the requirement(s).
    public Point getHostLongLat(Point devToBeHosted){
        Point host = new Point();
        //int k_nearest = 1;
        //coordinateProjection(fogNodeFileName, devToBeHosted);
        ArrayList<Point> fogNodelist = new ArrayList<Point>();
        //HashMap<Point, ArrayList<Point>> rsltsDic = new HashMap<Point, ArrayList<Point>>();
        for(Point p: fogNodePoints){
            fogNodelist.add(new Point(p.x,p.y));
            System.out.print("fog node: (" + host.x + "," + host.y + ") ");
        }
        /*System.out.println("fogNodelist.size() = "+fogNodelist.size());
        int pointCnt = 0;
        for(Point fogNode: fogNodelist){
            pointCnt++;
            System.out.println("testlist points "+pointCnt+": "+fogNode.x+", "+fogNode.y);
        }*/
        //System.exit(99);
        KDTree kdtree = new KDTree(fogNodesLongLat);//fogNodelist);
        host = kdtree.getNearestFogNode(fogNodesLongLat, devToBeHosted);
        if(debug) System.out.print("Host for ("+devToBeHosted.x+","+devToBeHosted.y+") = (" + host.x + "," + host.y + ") ");

        /*pointCnt = 0;
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
            //System.out.print(" => " + p+": ");
            for(Point x: rsltsDic.get(p)){
                //System.out.println("("+p.x+","+p.y+") ("+devToBeHosted.x+","+devToBeHosted.y+") "+ p.equals(devToBeHosted));
                if(p.equals(devToBeHosted)) {
                    System.out.print(" => " + p+": ");
                    System.out.print("(" + x.x + "," + x.y + ") ");
                    host = x;
                }
            }
            System.out.println();
        }*/
        return host;
    }

    // The method getHostXY(Point devToBeHosted) returns the fog node's
    // (x, y) coordinates for a given IoT/Mobile device
    // devToBeHosted. IoT/Mobile Device is identified by its coordinates
    // In the future, other parameters such as latency, CPU, RAM, Memory storage
    // requirements will be taken into account. Host assignment will change
    // according to the requirement(s).
    public Point getHostXY(Point devToBeHosted){
        Point host = new Point();
        //int k_nearest = 1;
        //coordinateProjection(fogNodeFileName, devToBeHosted);
        ArrayList<Point> fogNodelist = new ArrayList<Point>();
        //HashMap<Point, ArrayList<Point>> rsltsDic = new HashMap<Point, ArrayList<Point>>();
        for(Point p: fogNodePoints){
            fogNodelist.add(new Point(p.x,p.y));
            if(debug) System.out.print("fog node: (" + host.x + "," + host.y + ") ");
        }
        /*System.out.println("fogNodelist.size() = "+fogNodelist.size());
        int pointCnt = 0;
        for(Point fogNode: fogNodelist){
            pointCnt++;
            System.out.println("testlist points "+pointCnt+": "+fogNode.x+", "+fogNode.y);
        }*/
        //System.exit(99);
        KDTree kdtree = new KDTree(fogNodesXY);//fogNodelist);
        host = kdtree.getNearestFogNode(fogNodesXY, devToBeHosted);
        if(debug) System.out.print("Host for ("+devToBeHosted.x+","+devToBeHosted.y+") = (" + host.x + "," + host.y + ") ");

        /*pointCnt = 0;
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
            //System.out.print(" => " + p+": ");
            for(Point x: rsltsDic.get(p)){
                //System.out.println("("+p.x+","+p.y+") ("+devToBeHosted.x+","+devToBeHosted.y+") "+ p.equals(devToBeHosted));
                if(p.equals(devToBeHosted)) {
                    System.out.print(" => " + p+": ");
                    System.out.print("(" + x.x + "," + x.y + ") ");
                    host = x;
                }
            }
            System.out.println();
        }*/
        return host;
    }

    // The method generateVoronoi() generates Voronoi partitions, Voronoi Partition Vertex Map,
    // Voronoi Partition Edge Map, and Voronoi Neighbor Site Map.
    private void generateVoronoi() {

        events = new PriorityQueue <Event>();
        for (Point p : sites) {
            events.add(new Event(p, Event.SITE_EVENT));
            VoronoiPartitionVertexMap.put(p, null);
            VoronoiNeighborSiteMap.put(p, null);
            VoronoiPartitionEdgeMap.put(p, null);
        }

        // process events (sweep line)
        int count = 0;
        while (!events.isEmpty()) {
            //System.out.println();
            Event e = events.remove();
            ycurr = e.p.y;
            count++;
            if (e.type == Event.SITE_EVENT) {
                if(debug) System.out.println(count + ". SITE_EVENT " + e.p);
                handleSite(e.p);
            }
            else {
                if(debug) System.out.println(count + ". CIRCLE_EVENT " + e.p);
                handleCircle(e);
            }
        }

        ycurr = width+height;

        endEdges(root); // close off any dangling edges

        // get rid of those crazy infinite lines
        for (Edge e: edges){
            if (e.neighbor != null) {
                e.start = e.neighbor.end;
                e.neighbor = null;
            }
        }
    }

    // end all unfinished edges
    private void endEdges(Parabola p) {
        if (p.type == Parabola.IS_FOCUS) {
            p = null;
            return;
        }

        double x = getXofEdge(p);
        p.edge.end = new Point (x, p.edge.slope*x+p.edge.yint);
        if(debug) System.out.println("p.edge : "+p.edge.start+", "+p.edge.end);
        Edge edgeReverse = new Edge(p.edge.end, p.edge.site_right, p.edge.site_left);
        edgeReverse.end = new Point(p.edge.start.x, p.edge.start.y);
        if(debug) System.out.println("p.edgeReverse : "+edgeReverse.start+", "+edgeReverse.end);
        if(!edges.contains(p.edge) && !edges.contains(edgeReverse)) {
            for(Edge e: edges){
                if(debug) System.out.println(e);
            }
            edges.add(p.edge);
        }

        endEdges(p.child_left);
        endEdges(p.child_right);

        p = null;
    }

    // processes site event
    private void handleSite(Point p) {
        // base case
        if (root == null) {
            root = new Parabola(p);
            return;
        }

        // find parabola on beach line right above p
        Parabola par = getParabolaByX(p.x);
        if (par.event != null) {
            events.remove(par.event);
            par.event = null;
        }

        // create new dangling edge; bisects parabola focus and p
        Point start = new Point(p.x, getY(par.point, p.x));
        Edge el = new Edge(start, par.point, p);
        Edge er = new Edge(start, p, par.point);
        el.neighbor = er;
        er.neighbor = el;
        par.edge = el;
        par.type = Parabola.IS_VERTEX;

        // replace original parabola par with p0, p1, p2
        Parabola p0 = new Parabola (par.point);
        Parabola p1 = new Parabola (p);
        Parabola p2 = new Parabola (par.point);

        par.setLeftChild(p0);
        par.setRightChild(new Parabola());
        par.child_right.edge = er;
        par.child_right.setLeftChild(p1);
        par.child_right.setRightChild(p2);

        checkCircleEvent(p0);
        checkCircleEvent(p2);
    }

    // process circle event
    private void handleCircle(Event e) {

        // find p0, p1, p2 that generate this event from left to right
        Parabola p1 = e.arc;
        Parabola xl = Parabola.getLeftParent(p1);
        Parabola xr = Parabola.getRightParent(p1);
        Parabola p0 = Parabola.getLeftChild(xl);
        Parabola p2 = Parabola.getRightChild(xr);

        // remove associated events since the points will be altered
        if (p0.event != null) {
            events.remove(p0.event);
            p0.event = null;
        }
        if (p2.event != null) {
            events.remove(p2.event);
            p2.event = null;
        }

        Point p = new Point(e.p.x, getY(p1.point, e.p.x)); // new vertex

        // end edges!
        xl.edge.end = p;
        xr.edge.end = p;
        edges.add(xl.edge);
        edges.add(xr.edge);

        // start new bisector (edge) from this vertex on which ever original edge is higher in tree
        Parabola higher = new Parabola();
        Parabola par = p1;
        while (par != root) {
            par = par.parent;
            if (par == xl) higher = xl;
            if (par == xr) higher = xr;
        }
        higher.edge = new Edge(p, p0.point, p2.point);

        // delete p1 and parent (boundary edge) from beach line
        Parabola gparent = p1.parent.parent;
        if (p1.parent.child_left == p1) {
            if(gparent.child_left  == p1.parent) gparent.setLeftChild(p1.parent.child_right);
            if(gparent.child_right == p1.parent) gparent.setRightChild(p1.parent.child_right);
        }
        else {
            if(gparent.child_left  == p1.parent) gparent.setLeftChild(p1.parent.child_left);
            if(gparent.child_right == p1.parent) gparent.setRightChild(p1.parent.child_left);
        }

        Point op = p1.point;
        p1.parent = null;
        p1 = null;

        checkCircleEvent(p0);
        checkCircleEvent(p2);
    }

    // adds circle event if foci a, b, c lie on the same circle
    private void checkCircleEvent(Parabola b) {

        Parabola lp = Parabola.getLeftParent(b);
        Parabola rp = Parabola.getRightParent(b);

        if (lp == null || rp == null) return;

        Parabola a = Parabola.getLeftChild(lp);
        Parabola c = Parabola.getRightChild(rp);

        if (a == null || c == null || a.point == c.point) return;

        if (ccw(a.point,b.point,c.point) != 1) return;

        // edges will intersect to form a vertex for a circle event
        Point start = getEdgeIntersection(lp.edge, rp.edge);
        if (start == null) return;
        partitionVertices.add(start);
        if(debug) {
            System.out.println("Point a: " + a.point);
            System.out.println("Point b: " + b.point);
            System.out.println("Point c: " + c.point);
            System.out.println("intersection coordinates: " + start);// sks0099 added this line for debugging
        }
        ArrayList<Point> VertexPointA = new ArrayList<>();
        ArrayList<Point> VertexPointB = new ArrayList<>();
        ArrayList<Point> VertexPointC = new ArrayList<>();
        if(VoronoiPartitionVertexMap.get(a.point) != null){
            //ArrayList<Point> VertexPoint = new ArrayList<>();
        //}else {
            VertexPointA = new ArrayList<>(VoronoiPartitionVertexMap.get(a.point));//new ArrayList<>();
        }
        if(VoronoiPartitionVertexMap.get(b.point) != null){
            VertexPointB = new ArrayList<>(VoronoiPartitionVertexMap.get(b.point));//new ArrayList<>();
        }
        if(VoronoiPartitionVertexMap.get(c.point) != null){
            VertexPointC = new ArrayList<>(VoronoiPartitionVertexMap.get(c.point));//new ArrayList<>();
        }

        // Adding points to the ArrayList
        VertexPointA.add(start);
        VoronoiPartitionVertexMap.put(a.point, VertexPointA);

        VertexPointB.add(start);
        VoronoiPartitionVertexMap.put(b.point, VertexPointB);

        VertexPointC.add(start);
        VoronoiPartitionVertexMap.put(c.point, VertexPointC);

        //Neighbor addition begins
        ArrayList<Point> NeighborPointA = new ArrayList<>();
        ArrayList<Point> NeighborPointB = new ArrayList<>();
        ArrayList<Point> NeighborPointC = new ArrayList<>();
        if(VoronoiNeighborSiteMap.get(a.point) != null){
            NeighborPointA = new ArrayList<>(VoronoiNeighborSiteMap.get(a.point));
        }
        if(VoronoiNeighborSiteMap.get(b.point) != null){
            NeighborPointB = new ArrayList<>(VoronoiNeighborSiteMap.get(b.point));
        }
        if(VoronoiNeighborSiteMap.get(c.point) != null){
            NeighborPointC = new ArrayList<>(VoronoiNeighborSiteMap.get(c.point));
        }

        // Adding points to the ArrayList
        if(!NeighborPointA.contains(b.point)) NeighborPointA.add(b.point);
        if(!NeighborPointA.contains(c.point)) NeighborPointA.add(c.point);
        VoronoiNeighborSiteMap.put(a.point, NeighborPointA);

        if(!NeighborPointB.contains(a.point)) NeighborPointB.add(a.point);
        if(!NeighborPointB.contains(c.point)) NeighborPointB.add(c.point);
        VoronoiNeighborSiteMap.put(b.point, NeighborPointB);

        if(!NeighborPointC.contains(a.point)) NeighborPointC.add(a.point);
        if(!NeighborPointC.contains(b.point)) NeighborPointC.add(b.point);
        VoronoiNeighborSiteMap.put(c.point, NeighborPointC);
        //Neighbor addition ends

        // compute radius
        double dx = b.point.x - start.x;
        double dy = b.point.y - start.y;
        double d = Math.sqrt((dx*dx) + (dy*dy));
        if (start.y + d < ycurr) return; // must be after sweep line

        Point ep = new Point(start.x, start.y + d);
        if(debug) System.out.println("added circle event "+ ep);

        // add circle event
        Event e = new Event (ep, Event.CIRCLE_EVENT);
        e.arc = b;
        b.event = e;
        events.add(e);
    }

    // first thing we learned in this class :P
    public int ccw(Point a, Point b, Point c) {
        double area2 = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
        if (area2 < 0) return -1;
        else if (area2 > 0) return 1;
        else return  0;
    }

    // returns intersection of the lines of with vectors a and b
    private Point getEdgeIntersection(Edge a, Edge b) {

        if (b.slope == a.slope && b.yint != a.yint) return null;

        double x = (b.yint - a.yint)/(a.slope - b.slope);
        double y = a.slope*x + a.yint;

        return new Point(x, y); // Checked and found correct by sks0099 on 20250127
    }

    // returns current x-coordinate of an unfinished edge
    private double getXofEdge (Parabola par) {
        //find intersection of two parabolas

        Parabola left = Parabola.getLeftChild(par);
        Parabola right = Parabola.getRightChild(par);

        Point p = left.point;
        Point r = right.point;

        double dp = 2*(p.y - ycurr);
        double a1 = 1/dp;
        double b1 = -2*p.x/dp;
        double c1 = (p.x*p.x + p.y*p.y - ycurr*ycurr)/dp;

        double dp2 = 2*(r.y - ycurr);
        double a2 = 1/dp2;
        double b2 = -2*r.x/dp2;
        double c2 = (r.x*r.x + r.y*r.y - ycurr*ycurr)/dp2;

        double a = a1-a2;
        double b = b1-b2;
        double c = c1-c2;

        double disc = b*b - 4*a*c;
        double x1 = (-b + Math.sqrt(disc))/(2*a);
        double x2 = (-b - Math.sqrt(disc))/(2*a);

        double ry;
        if (p.y > r.y) ry = Math.max(x1, x2); //Original
        //if (p.y < r.y) ry = Math.max(x1, x2);
        else ry = Math.min(x1, x2);

        return ry; // Checked and found correct by sks0099 on 20250128
    }

    // returns parabola above this x coordinate in the beach line
    private Parabola getParabolaByX (double xx) {
        Parabola par = root;
        double x = 0;
        while (par.type == Parabola.IS_VERTEX) {
            x = getXofEdge(par);
            if (x>xx) par = par.child_left; // Original
            //if (x<xx) par = par.child_left;
            else par = par.child_right;
        }
        return par;
    }

    // find corresponding y-coordinate to x on parabola with focus p
    private double getY(Point p, double x) {
        // determine equation for parabola around focus p
        double dp = 2*(p.y - ycurr);
        double a1 = 1/dp;
        double b1 = -2*p.x/dp;
        double c1 = (p.x*p.x + p.y*p.y - ycurr*ycurr)/dp;
        return (a1*x*x + b1*x + c1); // Checked and found OK by sks0099 on 20250128
    }

    public void printPartitionInfo(){
        DecimalFormat df = new DecimalFormat("0.0000");
        System.out.println("\nPrinting of Voronoi Partition Vertices starts ... ");
        //VoronoiPartitionVertexMap.forEach((key, value) -> System.out.println(df.format(key.x) + " : " + df.format(value.x)));
        for (Map.Entry<Point, List<Point>> entry : VoronoiPartitionVertexMap.entrySet()) {
            Point key = entry.getKey();
            ArrayList<Point> points = new ArrayList<>(entry.getValue());
            System.out.print("("+df.format(key.x)+","+df.format(key.y)+")" + ": [");
            for (int i = 0; i < points.size(); i++) {
                if(i > 0) System.out.print(",");
                System.out.print("("+df.format(points.get(i).x)+","+df.format(points.get(i).y)+")");
            }
            System.out.print("]");
            System.out.println("");
            //(" + df.format(point.x) + ", " + df.format(point.y) + ")");
        }
        System.out.println("Printing of Voronoi Partition Vertices ends ... \n");

        System.out.println("\nPrinting of Voronoi Partition Edges starts ... ");
        //VoronoiPartitionVertexMap.forEach((key, value) -> System.out.println(df.format(key.x) + " : " + df.format(value.x)));
        for (Map.Entry<Point, List<Pair<Point,Point>>> entry : VoronoiPartitionEdgeMap.entrySet()) {
            Point key = entry.getKey();
            if(entry.getValue() == null){
                System.out.print("("+df.format(key.x)+","+df.format(key.y)+")" + ": null");
            }else{
                ArrayList<Pair<Point,Point>> pointPairs = new ArrayList<>(entry.getValue());
                System.out.print("("+df.format(key.x)+","+df.format(key.y)+")" + ": [");
                for (int i = 0; i < pointPairs.size(); i++) {
                    if (i > 0) System.out.print(",");
                    System.out.print("[("+df.format(pointPairs.get(i).getFirst().x)+","+df.format(pointPairs.get(i).getFirst().y)+")");
                    System.out.print(",("+df.format(pointPairs.get(i).getSecond().x)+","+df.format(pointPairs.get(i).getSecond().y)+")]");
                }
                System.out.print("]");
            }

            System.out.println("");
            //(" + df.format(point.x) + ", " + df.format(point.y) + ")");
        }
        System.out.println("Printing of Voronoi Partition Edges ends ... \n");
    }

    public void printPartitionNeighborInfo(){
        DecimalFormat df = new DecimalFormat("0.0000");
        System.out.println("\nPrinting of Voronoi Partition Neighbors starts ... ");
        for (Map.Entry<Point, List<Point>> entry : VoronoiNeighborSiteMap.entrySet()) {
            Point key = entry.getKey();
            if(entry.getValue() == null){
                System.out.print("("+df.format(key.x)+","+df.format(key.y)+")" + ": null");
            }else{
                ArrayList<Point> points = new ArrayList<>(entry.getValue());
                System.out.print("("+df.format(key.x)+","+df.format(key.y)+")" + ": [");
                for (int i = 0; i < points.size(); i++) {
                    if(i > 0) System.out.print(",");
                    System.out.print("("+df.format(points.get(i).x)+","+df.format(points.get(i).y)+")");
                }
                System.out.print("]");
            }
            System.out.println("");
        }
        System.out.println("Printing of Voronoi Partition Neighbors ends ... \n");
    }

    // The method setFogNodesLongLat converts from json file input
    // to (Longitude, Latitude) coordinates of the ndoes and
    // sets fogNodesLongLat accordingly
    private void setFogNodesLongLat(String fogNodeFileName){
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = new JSONObject();
        double longitude = 0.0;
        double latitude = 0.0;
        double longitudeCheck = 0.0;
        double latitudeCheck = 0.0;
        double x = 0.0;
        double y = 0.0;

        try {
            Reader reader = new FileReader(fogNodeFileName);
            Object obj = jsonParser.parse(reader);
            jsonObject = (JSONObject) obj;
            int num_nodes = jsonObject.size();
            System.out.println("num_nodes = "+num_nodes);
            String key = "";
            boolean found=false;
            for(int i=0; i<num_nodes; i++) {
                key = Integer.toString(i);
                if (jsonObject.containsKey(key)) {
                    JSONObject childObject = (JSONObject)((JSONArray)jsonObject.get(key)).get(0);
                    latitude = Double.parseDouble(childObject.get("latitude").toString());
                    longitude = Double.parseDouble(childObject.get("longitude").toString());

                    //Check if does not exist
                    found = false;
                    for(int j = 0; j<i-1; j++){
                        JSONObject childObjectCheck = (JSONObject)((JSONArray)jsonObject.get(Integer.toString(j))).get(0);
                        latitudeCheck = Double.parseDouble(childObjectCheck.get("latitude").toString());
                        longitudeCheck = Double.parseDouble(childObjectCheck.get("longitude").toString());
                        if(Objects.equals(latitude, latitudeCheck) && Objects.equals(longitude, longitudeCheck)){
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        x = longitude;
                        y = latitude;
                        Point pt = new Point(x, y);
                        fogNodesLongLat.add(pt);
                    }
                }else{
                    System.out.println(key+" doesn't exist.");
                }
            }

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    // The method setSortedFogNodesLongLat sorts of the nodes with
    // (Longitude, Latitude) coordinates by Longitude first and Latitude next (with the same Longitude)
    public void setSortedFogNodesLongLat(List<Point> fogNodesLongLat){
        if(fogNodesLongLat.size()>0) {
            List<Point> fogNodesLongLatList = new ArrayList<>(fogNodesLongLat);
            sortedFogNodesLongLat = new ArrayList<>(sortArray(fogNodesLongLatList));
        }
    }

    // The method setFogNodeXYCoordinate converts from (Long, Lat) coordinates
    // to x-y coordinates and sets fogNodesXY accordingly
    public void setFogNodeXYCoordinate(List<Point> fogNodesLongLat){
        List<Point> retList;
        if(fogNodesLongLat.size()>0) {
            for (int i = 0; i < fogNodesLongLat.size(); i++) {
                fogNodesXY.add(fogNodesLongLat.get(i).convertLongLatPointToXYCoordinates());
            }
        }
    }

    // The method setSortedFogNodesXY sorts of the nodes with
    // (x, y) coordinates by x first and y next (with the same x coordinate)
    public void setSortedFogNodesXY(List<Point> fogNodesXY){
        if(fogNodesXY.size()>0) {
            List<Point> fogNodesXYList = new ArrayList<>(fogNodesXY);
            sortedFogNodesXY = new ArrayList<>(sortArray(fogNodesXYList));
        }
    }
    // main method is an independent method to test the functioning of
    // Voronoi class by providing random inputs
    // takes one command line argument N
    // draws Voronoi diagram of N randomly generated sites
    public static void main(String[] args) {
        int N = 1;
        if(args.length == 0){
            N = 8;
        }else {
            Integer.parseInt(args[0]);
        }

        ArrayList<Point> points = new ArrayList<Point>();

        Random gen = new Random();

        for (int i = 0; i < N; i++){
            double x = gen.nextDouble();
            double y = gen.nextDouble();
            points.add(new Point(x, y));
        }
        /*N=3;
        double x = 0.88;//11686;
        double y = 0.46;//1104;
        points.add(new Point(x, y));
        x = 0.99;//183796;
        y = 0.53;//37285;
        points.add(new Point(x, y));
        x = 0.40;//094034;
        y = 0.39;//505668;
        points.add(new Point(x, y));*/

        long start = System.currentTimeMillis();//s.elapsedTime();
        Voronoi diagram = new Voronoi (points);
        long stop = System.currentTimeMillis();

        System.out.println((stop-start)+" ms");


        // draw results
        //StdDraw stdDraw = new StdDraw();

        StdDraw.setPenRadius(.005);
        for (Point p: points) {
            System.out.println(p.x + ", "+ p.y);
            StdDraw.point(p.x, p.y);
        }
        StdDraw.setPenRadius(.002);
        for (Edge e: diagram.edges) {
            System.out.println(e.start.x+", "+e.start.y+", "+e.end.x+", "+e.end.y);
            StdDraw.line(e.start.x, e.start.y, e.end.x, e.end.y);
        }
    }
}