package com.genie.commons.geo;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by d032459 on 14/12/22.
 */
public class GeoUtils {
    /**
     * Finds the centroid of a polygon with integer verticies.
     *
     * @param polygon
     *            The polygon to findInterSection the centroid of.
     * @return The centroid of the polygon.
     */
    public final static  GeoPos centroid(List<GeoPos> polygon) {


        if (polygon == null)
            return null;

        int N = polygon.size();
        if(N == 1)
            return polygon.get(0);


        double cx = 0, cy = 0;
        double A = polygonArea(polygon);
        int i, j;

        double factor = 0;
        for (i = 0; i < N; i++) {
            j = (i + 1) % N;
            factor = (polygon.get(i).lng * polygon.get(j).lat - polygon.get(j).lng * polygon.get(i).lat);
            cx += (polygon.get(i).lng + polygon.get(j).lng) * factor;
            cy += (polygon.get(i).lat + polygon.get(j).lat) * factor;
        }
        factor = 1.0 / (6.0 * A);
        cx *= factor;
        cy *= factor;
        return new GeoPos(Math.abs(cx), Math.abs(cy));

    }

    public static double polygonArea(List<GeoPos> polygon) {
        final int N = polygon.size();
        int i, j;
        double area = 0;

        for (i = 0; i < N; i++) {
            j = (i + 1) % N;
            area += polygon.get(i).lng * polygon.get(j).lat;
            area -= polygon.get(i).lat * polygon.get(j).lng;
        }

        area /= 2.0;
        return (Math.abs(area));
    }



    /*
	 * where: in kilometers (default)
	 */

    public final static double distance(double lon1, double lat1, double lon2,
                                        double lat2) {

        double theta = lon1 - lon2;

        double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.cos(Math.toRadians(theta));

        dist = Math.acos(dist);

        dist = Math.toDegrees(dist);

        dist = dist * 60 * 1.1515;



        dist = dist * 1.609344;



        return (dist);

    }

    public final static double distance(GeoPos p1, GeoPos p2) {
        return distance(p1.lng, p1.lat, p2.lng, p2.lat);
    }


    /**
     * calc the angle between the two lines consist of a, b, c 3 points in Degree
     * @param a
     * @param b
     * @param c
     * @return
     */
    public static double angleBetween(GeoPos a, GeoPos b, GeoPos c) {
        double angle1 = bearingDegree(b.lng, b.lat, a.lng, a.lat);
        double angle2 = bearingDegree(b.lng, b.lat, c.lng, c.lat);
        double angle = Math.abs(angle1 - angle2);

        return angle > 360 ? 360 - angle : angle;
    }

    public final static double bearingDegree(double lng1, double lat1,
                                             double lng2, double lat2) {
        double longitude1 = lng1;
        double longitude2 = lng2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(longitude2 - longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2)
                - Math.sin(latitude1) * Math.cos(latitude2)
                * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        // double r = Math.atan2(Math.cos(deg2rad(lat1))*Math.sin(deg2rad(lat2))
        // -
        // Math.sin(deg2rad(lat1))*Math.cos(deg2rad(lat2))*Math.cos(deg2rad(lng2
        // - lng1)),
        // Math.sin(deg2rad(lng2 - lng1))*Math.cos(deg2rad(lat2)));
        // return r;

    }

    // Returns 1 if the lines intersect, otherwise 0. In addition, if the lines
    // intersect the intersection point may be stored in the floats i_x and i_y.
    public static GeoPos intersection(GeoPos p0, GeoPos p1, GeoPos p2, GeoPos p3)
    {
        double s1_x, s1_y, s2_x, s2_y;
        s1_x = p1.lng - p0.lng;
        s1_y = p1.lat - p0.lat;
        s2_x = p3.lng - p2.lng;
        s2_y = p3.lat - p2.lat;

        double s, t;
        s = (-s1_y * (p0.lng - p2.lng) + s1_x * (p0.lat - p2.lat)) / (-s2_x * s1_y + s1_x * s2_y);
        t = ( s2_x * (p0.lat - p2.lat) - s2_y * (p0.lng - p2.lng)) / (-s2_x * s1_y + s1_x * s2_y);

        if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
        {
            // Collision detected
            GeoPos pos = new GeoPos(p0.lng + (t * s1_x), p0.lat + (t * s1_y));

            return pos;
        }

        return null; // No collision
    }
    public static double distanceToLine(double ax, double ay, double bx, double by, double px, double py) {
        GeoPos mid = GeoUtils.nearestPointOnLine(ax, ay, bx, by, px, py, false);
        return GeoUtils.distance(px, py, mid.lng, mid.lat);
    }

    public static GeoPos nearestPointOnLine(double ax, double ay, double bx, double by, double px, double py,
                                            boolean clampToSegment) {
        // Thanks StackOverflow!
        // http://stackoverflow.com/questions/1459368/snap-point-to-a-line-java




        double apx = px - ax;
        double apy = py - ay;
        double abx = bx - ax;
        double aby = by - ay;

        double ab2 = abx * abx + aby * aby;
        double ap_ab = apx * abx + apy * aby;
        double t = ap_ab / ab2;
        if (clampToSegment) {
            if (t < 0) {
                t = 0;
            } else if (t > 1) {
                t = 1;
            }
        }
        GeoPos dest = new GeoPos(ax + abx * t, ay + aby * t);
        return dest;
    }

    public final static GeoPos move(double lng1, double lat1, double bearing,
                                    double distance) {
        double dist = distance / 6371.0;
        double brng = Math.toRadians(bearing);
        lat1 = Math.toRadians(lat1);
        double lon1 = Math.toRadians(lng1);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist)
                + Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
        double a = Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1),
                Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
        // System.out.println("a = " + a);
        double lon2 = lon1 + a;

        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
        return new GeoPos(Math.toDegrees(lon2), Math.toDegrees(lat2));
        //
        // double R = 6371; // km of earth radius;
        // double lat2 =
        // Math.asin(Math.sin(deg2rad(lat1))*Math.cos(deg2rad(distance/R))+
        // Math.cos(deg2rad(lat1))*Math.sin(deg2rad(distance/R))*Math.cos(bearing));
        // double lng2 = deg2rad(lng1) +
        // Math.atan2(Math.cos(deg2rad(distance/R)) -
        // Math.sin(deg2rad(lat1))*Math.sin(deg2rad(lat2)),
        // Math.sin(bearing)*Math.sin(deg2rad(distance/R))*Math.cos(deg2rad(lat1)));
        // return new GeoPos(rad2deg(lat2), rad2deg(lng2));
    }

    /**
     * the algorithm is to draw a horizontal line through your test point. Count
     * how many lines of of the polygon you intersect to reach your point.
     *
     * If the answer is odd, you're inside. If the answer is even, you're
     * outside.
     * http://stackoverflow.com/questions/924171/geo-fencing-point-inside
     * -outside-polygon/924179#924179
     * http://en.wikipedia.org/wiki/Point_in_polygon
     *
     * @param poly
     * @return
     */
    public final static boolean inPolygon(List<GeoPos> poly, GeoPos point) {
        int i, j;
        boolean c = false;
        int s0 = poly.size() - 1;
        int s1 = poly.size();
        for (i = 0, j = s0; i < s1; j = i++) {

            if ((((poly.get(i).lat <= point.lat) && (point.lat < poly.get(j).lat)) || ((poly
                    .get(j).lat <= point.lat) && (point.lat < poly.get(i).lat)))
                    && (point.lng < (poly.get(j).lng - poly.get(i).lng)
                    * (point.lat - poly.get(i).lat)
                    / (poly.get(j).lat - poly.get(i).lat)
                    + poly.get(i).lng))

                c = !c;

        }

        return c;
    }

    public final static int long2tilex(double lon, double z)
    {
        return (int)(Math.floor((lon + 180.0) / 360.0 * Math.pow(2.0, z)));
    }

    public final static int lat2tiley(double lat, double z)
    {
        return (int)(Math.floor((1.0 - Math.log(Math.tan(lat * Math.PI / 180.0) + 1.0 / Math.cos(lat * Math.PI / 180.0)) / Math.PI) / 2.0 * Math.pow(2.0, z)));
    }

    public final static double tilex2long(int x, double z)
    {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public final static double tiley2lat(int y, double z)
    {
        double n = Math.PI - 2.0 * Math.PI * y / Math.pow(2.0, z);
        return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n)));
    }
}
