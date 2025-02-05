package edu.boun.edgecloudsim.sample_voronoi_app;

import java.util.Objects;

// a point in 2D, sorted by y-coordinate
public class Point implements Comparable <Point>{

    double x;
    double y;
    double epsilon = 1.0e-4;

    public Point (double x0, double y0) {
        x = x0;
        y = y0;
    }

    public int compareTo (Point other) {
        if (this.y == other.y) {
            if (this.x == other.x) return 0;
            else if (this.x > other.x) return 1;
            else return -1;
        }
        else if (this.y > other.y) {
            return 1;
        }
        else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        if(Math.abs(x - point.x) < epsilon && Math.abs(y - point.y) < epsilon){
            return true;
        }else{
            return false;
        }
        //return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public String toString() {
        return "(" + x + ", " + y + ")";
    }

}