package com.genie.commons.route;

import com.genie.commons.geo.GeoPos;
import com.genie.commons.network.InterSection;
import com.genie.commons.network.RoadNetwork;
import com.genie.commons.network.Section;

import java.util.*;

/**
 * Created by d032459 on 15/1/8.
 */
public class BidirectionalDijkstraRouteFinder implements RouteFinder  {
    private final RoadNetwork network;
    private final HashMap<InterSection, Vertex> vertexMap;
    private final NavigableSet<Vertex> qOrigin = new TreeSet<Vertex>(new VertexOriginComparator());
    private final NavigableSet<Vertex> qDestination = new TreeSet<Vertex>(new VertexDestinationComparator());

    public static class Vertex  {
        public Section inSection;
        public Section outSection;
        public InterSection host;
        public Vertex previous;
        public Vertex next;
        public double weightOrigin;
        public double weightDesitination;

        public Vertex(InterSection is) {
            this.host = is;
            weightOrigin = 0.0;
            weightDesitination = 0.0;
            previous = null;
            weightDesitination = Double.MAX_VALUE;
            inSection = Section.DISCONNECTED_SECTION;
            outSection = Section.DISCONNECTED_SECTION;
        }



    }

    public static class FoundPath {
        public final Route route;
        public final double weight;
        public FoundPath(GeoPos from, GeoPos to, Vertex source, Vertex mid, Vertex dest) {
            route = new Route(from, to);
            weight = mid.weightOrigin + mid.weightDesitination;
            Vertex v = mid;

            while(v != source) {
                if(v.inSection == null)
                    return;
                route.addFirst(v.inSection);
                v = v.previous;
            }
           // route.addFirst(source.inSection);
            v = mid;
            while (v != dest) {
                if(v.outSection == null)
                    return;
                route.addLast(v.outSection);
                v = v.next;
            }
            //route.addLast(dest.outSection);
        }
    }

    public static class VertexOriginComparator implements Comparator<Vertex> {
        @Override
        public int compare(Vertex o1, Vertex o2) {
            return Double.compare(o1.weightOrigin, o2.weightOrigin);
        }
    }
    public static class VertexDestinationComparator implements Comparator<Vertex> {
        @Override
        public int compare(Vertex o1, Vertex o2) {
            return Double.compare(o1.weightDesitination, o2.weightDesitination);
        }
    }
    public BidirectionalDijkstraRouteFinder(RoadNetwork n) {
        network = n;
        vertexMap = new HashMap<InterSection, Vertex>();
        // set-up vertices
        for (InterSection is : network.getInterSections()) {
            Vertex v = new Vertex(is);
            vertexMap.put(is, v);
        }
    }

    @Override
    public Route route(GeoPos from, GeoPos to, int type, int expandTimes) throws UnreachableException {
        Section f = network.findSection(from);
        Section t0 = network.findSection(to);

        if (f == null || t0 == null)
            return null;

            Route r = calc(from, to, f.getFrom(), t0.getTo(), RouteFinder.TIME_OPTIMAL);
            if(r != null)
                return r;




        final int times = expandTimes;

        for(int i = 0; i < times; i++) {
            Collection<Section> tl = network.findSections(to, i);
            if (f != null && !tl.isEmpty()) {
                for(Section t : tl) {



                    Route route = calc(from, to, f.getFrom(), t.getTo(), RouteFinder.TIME_OPTIMAL);
                    if(route != null)
                        return route;

                }
            }

        }
        throw new UnreachableException();
    }

    private Route calc(GeoPos fPos, GeoPos tPos, InterSection from, InterSection to, int type) {
        qDestination.clear();
        qOrigin.clear();

        final Vertex source = vertexMap.get(from);
        final Vertex dest = vertexMap.get(to);
        // set-up vertices
        for (Vertex v : vertexMap.values()) {
            v.previous = v == source ? source : null;
            v.next = v == dest ? dest: null;
            v.weightOrigin = v == source ? 0.0 : Double.MAX_VALUE;
            v.weightDesitination = v == dest ? 0.0 : Double.MAX_VALUE;
            v.inSection = (v == source) ? Section.NULL_SECTION : Section.DISCONNECTED_SECTION;
            v.outSection = (v== dest) ? Section.NULL_SECTION : Section.DISCONNECTED_SECTION;
            if(v == source)  // don't need to put every vertex into set. the set growth during explore {JINY}
                qOrigin.add(v);
            if(v == dest) {
                qDestination.add(v);
            }

        }

        return dijkstra(fPos, tPos, source, dest, type);

    }
    private Route dijkstra(final GeoPos from, final GeoPos to, final Vertex source, final Vertex dest,  final int type) {
        Vertex u0, v0, u1, v1;
        Vertex mid = null;
        double midWeight = Double.MAX_VALUE;
        while (!qOrigin.isEmpty() && !qDestination.isEmpty()) {
            //System.out.println(qDestination.size());
            u0 = qOrigin.pollFirst();
            u1 = qDestination.pollFirst();


//            if(u0.weightOrigin < Double.MAX_VALUE) { // we can ignore u (and any other remaining vertices) since they are unreachable
                if (u0.weightDesitination != Double.MAX_VALUE) {
                    double tmp = u0.weightDesitination + u0.weightOrigin;
                    if (tmp < midWeight) {
                        mid = u0;

                        midWeight = tmp;
                    }
                } else {

                    //look at distances to each neighbour
                    for (Section a : u0.host.getOutSections()) {
                        InterSection is = a.getTo(); //the neighbour in this iteration
                        v0 = vertexMap.get(is);
                        double w = 0;
                        if (type == DISTANCE_OPTIMAL)
                            w = a.getDistance();
                        else if (type == TIME_OPTIMAL)
                            w = a.getAvgPassTime();
                        final double alternateDist = u0.weightOrigin + w;
                        if (alternateDist < v0.weightOrigin) { // shorter path to neighbour found
                            qOrigin.remove(v0);
                            v0.weightOrigin = alternateDist;
                            v0.inSection = a;
                            v0.previous = u0;
                            qOrigin.add(v0);
                        }
                    }
                }

//            }
//            if (u1.weightDesitination <  Double.MAX_VALUE) { // we can ignore u (and any other remaining vertices) since they are unreachable
                if (u1.weightOrigin != Double.MAX_VALUE) {
                    double tmp =u1.weightDesitination + u1.weightOrigin;
                    if (tmp < midWeight) {
                        mid = u1;

                        midWeight = tmp;
                    }
                } else {
                    //look at distances to each neighbour
                    for (Section a : u1.host.getInSections()) {
                        InterSection is = a.getFrom(); //the neighbour in this iteration
                        v1 = vertexMap.get(is);
                        double w = 0;
                        if (type == DISTANCE_OPTIMAL)
                            w = a.getDistance();
                        else if (type == TIME_OPTIMAL)
                            w = a.getAvgPassTime();
                        final double alternateDist = u1.weightDesitination + w;
                        if (alternateDist < v1.weightDesitination) { // shorter path to neighbour found
                            qDestination.remove(v1);
                            v1.weightDesitination = alternateDist;
                            v1.outSection = a;
                            v1.next = u1;
                            qDestination.add(v1);
                        }
                    }
                }
//            }
        }
        FoundPath p = new FoundPath(from, to, source, mid, dest);
        return p.route;
    }
}
