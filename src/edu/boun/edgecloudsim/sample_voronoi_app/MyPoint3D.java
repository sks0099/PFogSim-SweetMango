package edu.boun.edgecloudsim.sample_voronoi_app;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// A custom Point3D class with overridden equals() and hashCode()
public class MyPoint3D {
    private double x, y, z;

    public MyPoint3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyPoint3D myPoint3D = (MyPoint3D) o;
        return Double.compare(myPoint3D.x, x) == 0 &&
                Double.compare(myPoint3D.y, y) == 0 &&
                Double.compare(myPoint3D.z, z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
