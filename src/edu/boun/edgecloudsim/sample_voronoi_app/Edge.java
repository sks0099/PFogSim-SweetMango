package edu.boun.edgecloudsim.sample_voronoi_app;

// an edge on the Voronoi diagram
public class Edge {

    Point start;
    Point end;
    Point site_left;
    Point site_right;
    Point direction; // edge is really a vector normal to left and right points

    Edge neighbor; // the same edge, but pointing in the opposite direction

    double slope;
    double yint; // yint is y-intercept of the line

    public Edge (Point first, Point left, Point right) {
        start = first;
        site_left = left;
        site_right = right;
        direction = new Point(right.y - left.y, - (right.x - left.x));
        end = null;
        slope = (right.x - left.x)/(left.y - right.y); //Commented by sks0099 on 20250118. Removed comment later.
        //slope = (right.y - left.y)/(-left.x + right.x); //Added by sks0099 on 20250118
        Point mid = new Point ((right.x + left.x)/2, (left.y+right.y)/2);
        yint = mid.y - slope*mid.x; //Commented by sks0099 on 20250118. Removed comment later.
    }

    public String toString() {
        return start + ", " + end;
    }
}
