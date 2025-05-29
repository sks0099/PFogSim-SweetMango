package edu.boun.edgecloudsim.sample_voronoi_app;

import java.util.Comparator;
//import edu.boun.edgecloudsim.sample_voronoi_app.Point;
public class PointComparator implements Comparator<Point> {
    //@Override
    public int compare(Point p1, Point p2) {
        int xComparison = Double.compare(p1.x, p2.x);
        if (xComparison != 0) {
            return xComparison;
        } else {
            return Double.compare(p1.y, p2.y);
        }
    }
}
