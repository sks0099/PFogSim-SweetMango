package edu.boun.edgecloudsim.sample_voronoi_app;

//import javafx.util.Pair;

import org.apache.commons.math3.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

public class Voronoi {

    List <Point> sites;
    List <Edge> edges; // edges on Voronoi diagram
    PriorityQueue<Event> events; // priority queue represents sweep line
    Parabola root; // binary search tree represents beach line
    List <Point> partitionVertices; // vertices of Voronoi partitions
    List <Edge> partitionEdges; // edges of Voronoi partitions
    HashMap<Point, List<Point>> VoronoiPartitionVertexMap = new HashMap<>();
    HashMap<Point, List<Pair<Point, Point>>> VoronoiPartitionEdgeMap = new HashMap<>();
    HashMap<Point, List<Point>> VoronoiNeighborSiteMap = new HashMap<>();


    // size of StdDraw window
    double width = 1; //Original
    //double width = -100;
    double height = 1; //Original
    //double height = -100;

    double epsilon = 1.0e-4;

    double ycurr; // current y-coord of sweep line

    boolean showPartitionVertexLabel = true;//false;
    boolean showPartitionEdgeLabel = true;//false;

    public Voronoi (List <Point> sites) {
        this.sites = sites;
        edges = new ArrayList<Edge>();
        partitionVertices = new ArrayList<Point>();
        partitionEdges = new ArrayList<Edge>();
        generateVoronoi();
    }
    /*double x;
    double y;

    public Point myPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        //System.out.println(x+", "+point.x);
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }*/
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
                System.out.println(count + ". SITE_EVENT " + e.p);
                handleSite(e.p);
            }
            else {
                System.out.println(count + ". CIRCLE_EVENT " + e.p);
                handleCircle(e);
            }
        }

        ycurr = width+height;

        endEdges(root); // close off any dangling edges

        // get rid of those crazy inifinte lines
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
        System.out.println("p.edge : "+p.edge.start+", "+p.edge.end);
        Edge edgeReverse = new Edge(p.edge.end, p.edge.site_right, p.edge.site_left);
        edgeReverse.end = new Point(p.edge.start.x, p.edge.start.y);
        System.out.println("p.edgeReverse : "+edgeReverse.start+", "+edgeReverse.end);
        if(!edges.contains(p.edge) && !edges.contains(edgeReverse)) {
            for(Edge e: edges){
                System.out.println(e);
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
        System.out.println("Point a: "+a.point);
        System.out.println("Point b: "+b.point);
        System.out.println("Point c: "+c.point);
        System.out.println("intersection coordinates: "+start);// sks0099 added this line for debugging
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
        System.out.println("added circle event "+ ep);

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
        System.out.println("Printing of Voronoi Partition Vertices starts ... ");
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
        System.out.println("Printing of Voronoi Partition Vertices ends ... ");

        System.out.println("Printing of Voronoi Partition Edges starts ... ");
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
        System.out.println("Printing of Voronoi Partition Edges ends ... ");
    }

    public void printPartitionNeighborInfo(){
        DecimalFormat df = new DecimalFormat("0.0000");
        System.out.println("Printing of Voronoi Partition Neighbors starts ... ");
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
        System.out.println("Printing of Voronoi Partition Neighbors ends ... ");
    }

    public void plot(ArrayList<Point> fogNodePts, ArrayList<Point> mobileDevicePts){
        //ArrayList<Point> points = new ArrayList<Point>();

        /*Random gen = new Random();

        for (int i = 0; i < N; i++){
            double x = gen.nextDouble();
            double y = gen.nextDouble();
            points.add(new Point(x, y));
        }*/

        long start = System.currentTimeMillis();//s.elapsedTime();
        Voronoi diagram = new Voronoi (fogNodePts);
        long stop = System.currentTimeMillis();

        System.out.println((stop-start)+" ms");

        // draw results
        //StdDraw stdDraw = new StdDraw();
        StdDraw.setXscale(-1.0, 1.0);
        StdDraw.setYscale(-1.0, 1.0);
        //StdDraw.setCanvasSize(1536, 1024);
        StdDraw.setCanvasSize(900, 900);
        StdDraw.setPenRadius(.008);
        StdDraw.setPenColor(Color.RED);
        String labelText = "";
        Font currentFont = StdDraw.getFont();
        Font newFont = currentFont.deriveFont(currentFont.getSize() * 0.7F);
        //b.setFont(b.getFont().deriveFont(18.0f));
        for (Point p: fogNodePts) {
            //System.out.println(p.x + ", "+ p.y);
            StdDraw.point(p.x, p.y);
            if(showPartitionVertexLabel) {
                labelText = "(" + new DecimalFormat("#.###").format(p.x) + "," + new DecimalFormat("#.###").format(p.y) + ")";
                StdDraw.setFont(newFont);
                StdDraw.setFont(newFont.deriveFont(Font.BOLD));
                StdDraw.text((p.x + 0.04), (p.y + 0.02), labelText);
                StdDraw.setFont(currentFont);
            }
        }
        StdDraw.setPenRadius(.002);
        //StdDraw.setPenColor(Color.BLACK);
        int edgeCount = 0; //Added by sks0099 on 20250115
        int totalEdgeCount = diagram.edges.size();//Added by sks0099 on 20250115
        System.out.println("Total Edge count = "+totalEdgeCount);
        for (Edge e: diagram.edges) {
            System.out.println("edgeCount = "+edgeCount);
            System.out.println(e.start.x+", "+e.start.y+", "+e.end.x+", "+e.end.y);
            if(!Double.isNaN(e.start.x)) {
                //if(edgeCount < (totalEdgeCount/2 + 2)) {//Added by sks0099 on 20250115
                    StdDraw.setPenColor(Color.BLACK);
                    StdDraw.line(e.start.x, e.start.y, e.end.x, e.end.y);
                    if(showPartitionEdgeLabel) {
                        StdDraw.setFont(newFont);
                        StdDraw.setFont(newFont.deriveFont(Font.BOLD));
                        StdDraw.setPenColor(Color.BLUE);
                        labelText = "(" + new DecimalFormat("#.###").format(e.start.x) + "," + new DecimalFormat("#.###").format(e.start.y) + ")";
                        StdDraw.text((e.start.x - 0.02), (e.start.y + 0.02), labelText);
                        labelText = "(" + new DecimalFormat("#.###").format(e.end.x) + "," + new DecimalFormat("#.###").format(e.end.y) + ")";
                        StdDraw.text((e.end.x - 0.02), (e.end.y + 0.02), labelText);
                        labelText = "Edge " + edgeCount;
                        StdDraw.text(((e.start.x+e.end.x)/2 - 0.02), ((e.start.y+e.end.y)/2 + 0.02), labelText);
                        StdDraw.setFont(currentFont);
                    }
                //}//Added by sks0099 on 20250115
                edgeCount += 1;//Added by sks0099 on 20250115
            }
        }

        StdDraw.setPenColor(Color.GREEN);
        for (Point vp: partitionVertices) {
            //System.out.println(mp.x + ", "+ mp.y);
            //StdDraw.point(vp.x, vp.y);
            StdDraw.setFont(newFont);
            if(showPartitionVertexLabel) {
                StdDraw.setFont(newFont.deriveFont(Font.BOLD));
                labelText = "(" + new DecimalFormat("#.##").format(vp.x) + "," + new DecimalFormat("#.##").format(vp.y) + ")";
                StdDraw.text((vp.x + 0.02), (vp.y - 0.02), labelText);
                StdDraw.setFont(currentFont);
            }
        }

        StdDraw.setPenColor(Color.cyan);
        StdDraw.setPenRadius(0.005);
        for (Point mp: mobileDevicePts) {
            //System.out.println(mp.x + ", "+ mp.y);
            StdDraw.point(mp.x, mp.y);
        }

    }

    private static void createAndShowGui(ArrayList<Point> fogNodePts, ArrayList<Point> mobileDevicePts, List<Edge> edges) {
        //List<Double> scores = new ArrayList<>();
        //List<Point> PartitionVertices = fogNode;
        /*Random random = new Random();
        int maxDataPoints = 40;
        int maxScore = 10;
        for (int i = 0; i < maxDataPoints; i++) {
            scores.add((double) random.nextDouble() * maxScore);
//            scores.add((double) i);
        }*/
        VoronoiPlot mainPanel = new VoronoiPlot(fogNodePts, mobileDevicePts, edges);
        //mainPanel.setPreferredSize(new Dimension(800, 600));
        mainPanel.setPreferredSize(new Dimension(800, 800));
        JFrame frame = new JFrame("VoronoiPlot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        //frame.getContentPane().setBackground(new Color(255, 255, 255));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    public void plotSwing(ArrayList<Point> fogNodePts, ArrayList<Point> mobileDevicePts){

        long start = System.currentTimeMillis();//s.elapsedTime();
        Voronoi diagram = new Voronoi (fogNodePts);
        long stop = System.currentTimeMillis();
        int edgeCount = 0;

        ArrayList<Point> edgeListStart = new ArrayList<>();
        ArrayList<Point> edgeListEnd = new ArrayList<>();
        for (Edge e: diagram.edges) {
            //System.out.println("edgeCount = "+edgeCount);
            //System.out.println("start = "+e.start);
            edgeListStart.add(e.start);
            edgeListEnd.add(e.end);
            edgeCount += 1;
        }
        //System.out.println(e.start.x+", "+e.start.y+", "+e.end.x+", "+e.end.y);
        for (Map.Entry<Point, List<Point>> entry : VoronoiPartitionVertexMap.entrySet()) {
            Point key = entry.getKey();
            ArrayList<Edge> edgeList = new ArrayList<>(diagram.edges);

            ArrayList<Point> points = new ArrayList<>(entry.getValue());
            for (int i = 0; i < points.size(); i++) {
                Point p = points.get(i);
                //System.out.println("p = "+p);
                //if(edgeListStart.contains(p)){
                //    System.out.println("p found = "+p+", index = "+edgeListStart.indexOf(p));
                //}
                for(int j = 0; j < edgeListStart.size(); j++){
                    if(edgeListStart.get(j).equals(p)){
                        //System.out.println("p found = "+p+", index = "+j+" End point = "+edgeListEnd.get(j));
                        if(points.contains(edgeListEnd.get(j))){
                            System.out.println("Voronoi partition edge found for site = "+key+" with start = "+p+" and end = "+edgeListEnd.get(j));
                            Pair<Point, Point> tempPair = new Pair<>(p, edgeListEnd.get(j));
                            if(VoronoiPartitionEdgeMap.get(key)==null) {
                                ArrayList<Pair<Point, Point>> tempPairList = new ArrayList<>();//(tempPair));//tempPair);
                                tempPairList.add(tempPair);
                                VoronoiPartitionEdgeMap.put(key, tempPairList);
                            }else{
                                ArrayList<Pair<Point, Point>> tempPairList = new ArrayList<>(VoronoiPartitionEdgeMap.get(key));//(tempPair));//tempPair);

                                boolean edgeAlreadyExists = false;
                                //Check if edge already exists
                                for(int k = 0; k < tempPairList.size(); k++){
                                    if((Math.abs(tempPairList.get(k).getFirst().x-tempPair.getFirst().x) < epsilon &&
                                            Math.abs(tempPairList.get(k).getFirst().y-tempPair.getFirst().y) < epsilon &&
                                            Math.abs(tempPairList.get(k).getSecond().x-tempPair.getSecond().x) < epsilon &&
                                            Math.abs(tempPairList.get(k).getSecond().y-tempPair.getSecond().y) < epsilon) ||
                                    (Math.abs(tempPairList.get(k).getSecond().x-tempPair.getFirst().x) < epsilon &&
                                            Math.abs(tempPairList.get(k).getSecond().y-tempPair.getFirst().y) < epsilon &&
                                            Math.abs(tempPairList.get(k).getFirst().x-tempPair.getSecond().x) < epsilon &&
                                            Math.abs(tempPairList.get(k).getFirst().y-tempPair.getSecond().y) < epsilon)) {
                                        edgeAlreadyExists = true;
                                        break;
                                    }
                                }
                                if(!edgeAlreadyExists) {
                                    tempPairList.add(tempPair);
                                    VoronoiPartitionEdgeMap.put(key, tempPairList);
                                }
                            }
                        }
                    }
                    /*else{
                        System.out.println("p not found = "+p+", index = "+j+", "+edgeListStart.get(j));
                    }*/
                }
            }
            /*ArrayList<Point> tempListStart = new ArrayList<>(edgeListStart);
            ArrayList<Point> tempListEnd = new ArrayList<>(edgeListEnd);
            ArrayList<Point> intersection = new ArrayList<>(points);
            System.out.println("tempListStart[0] = "+tempListStart.get(0)+", points[0] = "+points.get(0));
            System.out.println(intersection.size());
            intersection.retainAll(tempListStart);
            System.out.println(intersection.size());
            intersection = new ArrayList<>(points);
            System.out.println(intersection.size());
            intersection.retainAll(tempListEnd);
            System.out.println(intersection.size());*/
            //System.exit(345);
                /*System.out.print("("+df.format(key.x)+","+df.format(key.y)+")" + ": [");
                for (int i = 0; i < points.size(); i++) {
                    if(i > 0) System.out.print(",");
                    System.out.print("("+df.format(points.get(i).x)+","+df.format(points.get(i).y)+")");
                }
                System.out.print("]");
                System.out.println("");*/
        }
        //if(!Double.isNaN(e.start.x)) {
            //if(edgeCount < (totalEdgeCount/2 + 2)) {//Added by sks0099 on 20250115
                /*StdDraw.setPenColor(Color.BLACK);
                StdDraw.line(e.start.x, e.start.y, e.end.x, e.end.y);
                if(showPartitionEdgeLabel) {
                    StdDraw.setFont(newFont);
                    StdDraw.setFont(newFont.deriveFont(Font.BOLD));
                    StdDraw.setPenColor(Color.BLUE);
                    labelText = "(" + new DecimalFormat("#.###").format(e.start.x) + "," + new DecimalFormat("#.###").format(e.start.y) + ")";
                    StdDraw.text((e.start.x - 0.02), (e.start.y + 0.02), labelText);
                    labelText = "(" + new DecimalFormat("#.###").format(e.end.x) + "," + new DecimalFormat("#.###").format(e.end.y) + ")";
                    StdDraw.text((e.end.x - 0.02), (e.end.y + 0.02), labelText);
                    labelText = "Edge " + edgeCount;
                    StdDraw.text(((e.start.x+e.end.x)/2 - 0.02), ((e.start.y+e.end.y)/2 + 0.02), labelText);
                    StdDraw.setFont(currentFont);
                }*/
            //}//Added by sks0099 on 20250115
        //    edgeCount += 1;//Added by sks0099 on 20250115
        //}
        System.out.println((stop-start)+" ms");
        printPartitionInfo();
        printPartitionNeighborInfo();
        /*VoronoiPlot mainPanel = new VoronoiPlot(fogNodePts);
        mainPanel.setPreferredSize(new Dimension(800, 600));
        JFrame frame = new JFrame("VoronoiPlot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);*/

        // draw results
        //StdDraw stdDraw = new StdDraw();
        StdDraw.setXscale(-1.0, 1.0);
        StdDraw.setYscale(-1.0, 1.0);
        //StdDraw.setCanvasSize(1536, 1024);
        StdDraw.setCanvasSize(900, 900);
        StdDraw.setPenRadius(.008);
        StdDraw.setPenColor(Color.RED);
        String labelText = "";
        Font currentFont = StdDraw.getFont();
        Font newFont = currentFont.deriveFont(currentFont.getSize() * 0.7F);
        //b.setFont(b.getFont().deriveFont(18.0f));
        int pointCnt = 1;
        for (Point p: fogNodePts) {
            //System.out.println(p.x + ", "+ p.y);
            StdDraw.point(p.x, p.y);
            if(showPartitionVertexLabel) {
                //labelText = "(" + new DecimalFormat("#.###").format(p.x) + "," + new DecimalFormat("#.###").format(p.y) + ")";
                labelText = ""+pointCnt;
                pointCnt += 1;
                StdDraw.setFont(newFont);
                StdDraw.setFont(newFont.deriveFont(Font.BOLD));
                StdDraw.text((p.x + 0.02), (p.y + 0.02), labelText);
                StdDraw.setFont(currentFont);
            }
        }
        StdDraw.setPenRadius(.002);
        //StdDraw.setPenColor(Color.BLACK);
        edgeCount = 1; //Added by sks0099 on 20250115
        int totalEdgeCount = diagram.edges.size();//Added by sks0099 on 20250115
        System.out.println("Total Edge count = "+totalEdgeCount);
        for (Edge e: diagram.edges) {
            System.out.println("edgeCount = "+edgeCount);
            System.out.println(e.start.x+", "+e.start.y+", "+e.end.x+", "+e.end.y);
            if(!Double.isNaN(e.start.x)) {
                //if(edgeCount < (totalEdgeCount/2 + 2)) {//Added by sks0099 on 20250115
                StdDraw.setPenColor(Color.BLACK);
                StdDraw.line(e.start.x, e.start.y, e.end.x, e.end.y);
                if(showPartitionEdgeLabel) {
                    StdDraw.setFont(newFont);
                    StdDraw.setFont(newFont.deriveFont(Font.BOLD));
                    StdDraw.setPenColor(Color.BLUE);
                    //labelText = "(" + new DecimalFormat("#.###").format(e.start.x) + "," + new DecimalFormat("#.###").format(e.start.y) + ")";
                    //StdDraw.text((e.start.x - 0.02), (e.start.y + 0.02), labelText);
                    //labelText = "(" + new DecimalFormat("#.###").format(e.end.x) + "," + new DecimalFormat("#.###").format(e.end.y) + ")";
                    //StdDraw.text((e.end.x - 0.02), (e.end.y + 0.02), labelText);
                    //labelText = "Edge " + edgeCount;
                    labelText = "" + edgeCount;
                    StdDraw.text(((e.start.x+e.end.x)/2 - 0.02), ((e.start.y+e.end.y)/2 + 0.02), labelText);
                    StdDraw.setFont(currentFont);
                }
                //}//Added by sks0099 on 20250115
                edgeCount += 1;//Added by sks0099 on 20250115
            }
        }

        StdDraw.setPenColor(Color.black);
        int vertexCnt = 1;
        for (Point vp: partitionVertices) {
            //System.out.println(mp.x + ", "+ mp.y);
            //StdDraw.point(vp.x, vp.y);
            StdDraw.setFont(newFont);
            if(showPartitionVertexLabel) {
                StdDraw.setFont(newFont.deriveFont(Font.BOLD));
                //labelText = "(" + new DecimalFormat("#.###").format(vp.x) + "," + new DecimalFormat("#.###").format(vp.y) + ")";
                labelText = ""+vertexCnt;// + new DecimalFormat("#.###").format(vp.x) + "," + new DecimalFormat("#.###").format(vp.y) + ")";
                StdDraw.text((vp.x + 0.02), (vp.y - 0.02), labelText);
                StdDraw.setFont(currentFont);
            }
            vertexCnt += 1;
        }

        StdDraw.setPenColor(Color.cyan);
        StdDraw.setPenRadius(0.005);
        for (Point mp: mobileDevicePts) {
            //System.out.println(mp.x + ", "+ mp.y);
            StdDraw.point(mp.x, mp.y);
        }
        createAndShowGui(fogNodePts, mobileDevicePts, diagram.edges);
        //VoronoiPlot mainPanel = new VoronoiPlot(mp);

    }
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