package com.genie.commons.geo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

/**
 * The only algorithm implemented is Graham's,
 * where the highest point is selected (called the pivot), the other points are
 * sorted according to their relative azimuths from the pivot, and then a path
 * is created around the other points. Any right turn encountered traversing the
 * points means that point should be skipped when creating the convex hull.
 */
public class ConvexHull implements HullFinder {
    public final  static  HullFinder instance = new ConvexHull();

    public List<GeoPos> hull(List<GeoPos> pts) {
        GeoPos[] points = new GeoPos[pts.size()];
        pts.sort(new LatLowestOrder());
        pts.toArray(points);
        Stack<GeoPos> hull = new Stack<GeoPos>();
        int N = points.length;

        Arrays.sort(points, 1, N, new PolarOrder(points[0]));


        hull.push(points[0]); // p[0] is first extreme point
        int k1;
        for (k1 = 1; k1 < N; k1++)
            if (!points[0].equals(points[k1]))
                break;
        if (k1 == N)
            return hull; // all points equal

        int k2;
        for (k2 = k1 + 1; k2 < N; k2++)
            if (ccw(points[0], points[k1], points[k2]) != 0)
                break;
        hull.push(points[k2 - 1]); // points[k2-1] is second extreme point

        for (int i = k2; i < N; i++)
        {
            GeoPos top = hull.pop();
            while (ccw(hull.peek(), top, points[i]) <= 0)
            {
                top = hull.pop();
            }
            hull.push(top);
            hull.push(points[i]);
        }


        //  double b1 = GeoCalc.rad2deg(GeoCalc.bearingRadian(p.lng, p.lat, a.lng, a.lat)) % 360;
        // double b2 = GeoCalc.rad2deg(GeoCalc.bearingRadian(p.lng, p.lat, b.lng, b.lat)) % 360;
//		   if(b1 < 180 ) {
//			   if(b2 < b1 || b2 > 180 + b1) { // left turn
//				   hull.push(b);
//				   p = a;
//				   a = b;
//			   }
//		   } else {
//			   if(b2 < b1 && b2 > b1 - 180) {
//				   hull.push(b);
//				   p = a;
//				   a = b;
//			   }
//		   }

        return hull;
    }
    public static class PolarOrder implements Comparator<GeoPos> {

        private GeoPos origin;
        public PolarOrder(GeoPos p) {
            origin = p;
        }

        public int compare(GeoPos q1, GeoPos q2)
        {
            double dx1 = q1.lng - origin.lng;
            double dy1 = q1.lat - origin.lat;
            double dx2 = q2.lng - origin.lng;
            double dy2 = q2.lat - origin.lat;

            if (dy1 >= 0 && dy2 < 0)
                return -1; // q1 above; q2 below
            else if (dy2 >= 0 && dy1 < 0)
                return +1; // q1 below; q2 above
            else if (dy1 == 0 && dy2 == 0)
            { // 3-collinear and horizontal
                if (dx1 >= 0 && dx2 < 0)
                    return -1;
                else if (dx2 >= 0 && dx1 < 0)
                    return +1;
                else
                    return 0;
            } else {
                double r = ccw(origin, q1, q2);
                if(r < 0) return 1;
                else if(r > 0) return -1;
                else return 0;
            }
            // both above or below
        }

    }
    public static double ccw(GeoPos p1, GeoPos p2, GeoPos p3) {
        return (p2.lng - p1.lng)*(p3.lat - p1.lat) - (p2.lat - p1.lat)*(p3.lng - p1.lng);
    }
    private static class LatLowestOrder implements Comparator<GeoPos> {
        public int compare(GeoPos p1, GeoPos p2) {
            if (p1.lat < p2.lat)
                return -1;
            if (p1.lat > p2.lat)
                return +1;
            if (p1.lng < p2.lng)
                return -1;
            if (p1.lng > p2.lng)
                return +1;
            return 0;
        }
    }
}
