package com.genie.commons.geo;

import java.util.*;

/**
 * Created by d032459 on 14/12/22.
 */
public class ConcaveHull implements HullFinder {
    public final static HullFinder instance = new ConcaveHull();
    private static class Edge implements Comparable<Edge>{
        final GeoPos pointA;
        final GeoPos pointB;
        final double distance;
        public Edge(GeoPos a, GeoPos b) {
            pointA = a;
            pointB = b;
            distance = GeoUtils.distance(pointA.lng, pointA.lat, pointB.lng, pointB.lat);
        }
        public int compareTo(Edge o) {

            if(distance > o.distance)
                return 1;
            else if(distance < o.distance)
                return -1;
            else
                return 0;
        }
        public String toString() {
            return pointA.toString() + " - " + pointB.toString() + ": " + distance;
        }
    }


    public List<GeoPos> hull(List<GeoPos> pts) {
        List<GeoPos> convexHull = ConvexHull.instance.hull(pts);
        TreeSet<Edge> edges = new TreeSet<Edge>();
        Iterator<GeoPos> it = convexHull.iterator();
        ArrayList<GeoPos> hull = new ArrayList<GeoPos>();
        Set<GeoPos> boundary = new HashSet<GeoPos>(convexHull);
        HashMap<GeoPos, Edge> hullEdges = new HashMap<GeoPos, Edge>();
        GeoPos a = null;
        GeoPos b = null;
        GeoPos first = null;
        if(it.hasNext())
            a = it.next();
        first = a;
        while(it.hasNext()) {
            b = it.next();
            Edge e = new Edge(a, b);
            edges.add(e);
            //System.out.println(e);
            a = b;
        }
        edges.add(new Edge(a, first));




        while(!edges.isEmpty()) {
            Edge e = edges.last();
            edges.remove(e);
            double minAngle = Double.MAX_VALUE;
            GeoPos minP = null;
            boolean newEdge = false;
            double maxD = maxDistanceToEdge(e, pts);  // more bigger polygon
            if(e.distance > maxD) {
                for(GeoPos pa : pts) {
                    double angle1 = GeoUtils.angleBetween(pa, e.pointA, e.pointB);
                    double angle2 = GeoUtils.angleBetween(pa, e.pointB, e.pointA);
                    double maxAngle = angle1 > angle2 ? angle1 : angle2;
                    if(maxAngle < minAngle) {
                        minAngle = maxAngle;
                        minP = pa;
                    }
                }
                //double maxD = GeoCalc.distanceToLine(e.pointA.lng, e.pointA.lat, e.pointB.lng, e.pointB.lat, minP.lng, minP.lat); // more smaller polygon
                //if(e.distance > maxD) {
                if(minAngle < 90.0 && !boundary.contains(minP)) {
                    Edge e2 = new Edge(e.pointA, minP);
                    Edge e3 = new Edge(minP, e.pointB);
                    //System.out.println(e2);
                    //System.out.println(e3);
                    GeoPos intersect = getIntersection(e2, e3, edges);
                    if(intersect == null || (intersect !=null && (intersect.equals(e.pointA) || intersect.equals(e.pointB)))){
                        edges.add(e2);
                        edges.add(e3);
                        boundary.add(minP);
                        newEdge =true;
                    }
                }
            }
            if(!newEdge) {
                hullEdges.put(e.pointA, e);
                boundary.add(e.pointA);
                boundary.add(e.pointB);

            }
        }
        Iterator<Edge> ite = hullEdges.values().iterator();
        Edge edge = ite.next();
        hullEdges.remove(edge.pointA);
        while(!hullEdges.isEmpty()) {
            hull.add(edge.pointA);
            hull.add(edge.pointB);
            edge = hullEdges.get(edge.pointB);
            hullEdges.remove(edge.pointA);

        }
        //GeoPos[] points = new GeoPos[hull.size()];
        //hull.toArray(points);

        //Arrays.sort(points, 1, points.length, new PolarOrder(points[0]));

        //hull.sort(new ConvexHull.PolarOrder(hull.get(0)));

        //List<GeoPos> hull2 = Arrays.asList(points);
//        for(GeoPos p : hull)
//            System.out.println(p);
        return hull;

    }
    private GeoPos getIntersection(Edge e1, Edge e2, Collection<Edge> edges) {
        for(Edge e: edges) {
//			if(!e.pointA.equals(e1.pointA) && !e.pointA.equals(e1.pointB)
//					&& !e.pointB.equals(e1.pointA) && !e.pointB.equals(e1.pointB)) {
            GeoPos intersect = GeoUtils.intersection(e.pointA, e.pointB, e1.pointA, e1.pointB);
            if(intersect != null){
                return intersect;
            }
//			}
//			if(!e.pointA.equals(e2.pointA) && !e.pointA.equals(e2.pointB)
//					&& !e.pointB.equals(e2.pointA) && !e.pointB.equals(e2.pointB)) {

            intersect = GeoUtils.intersection(e.pointA, e.pointB, e2.pointA, e2.pointB);
            if(intersect != null){
                return intersect;
            }
//			}
        }
        return null;
    }
    private double maxDistanceToEdge(Edge e, List<GeoPos> pts) {

        double maxD = Double.MIN_VALUE;
        for(GeoPos p : pts) {
            double d = GeoUtils.distanceToLine(e.pointA.lng, e.pointA.lat, e.pointB.lng, e.pointB.lat, p.lng, p.lat);
            if(d > maxD)
                maxD = d;
        }
        return maxD;
    }

}
