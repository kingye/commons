package com.genie.commons.route;

import com.genie.commons.collection.StringBloomFilter;
import com.genie.commons.geo.GeoPos;
import com.genie.commons.network.InterSection;
import com.genie.commons.network.RoadNetwork;
import com.genie.commons.network.Section;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.util.*;
import java.util.logging.Logger;


public class DijkstraRouteFinder implements RouteFinder {
    private final RoadNetwork network;
    private final Map<InterSection, Vertex> vertexMap;
    private final NavigableSet<Vertex> q = new TreeSet<Vertex>();
    StringBloomFilter failed = new StringBloomFilter();
    private final static Logger logger = Logger.getLogger(DijkstraRouteFinder.class.getName());
    private final static class Vertex implements Comparable<Vertex> {
        public Section inSection;
        public final InterSection host;
        public Vertex previous;
        public double weight;
        public Vertex[] neighbours;
        public Section[] sections;

        public Vertex(InterSection is) {
            this.host = is;
            weight = 0.0;
        }

        public final int compareTo(Vertex other) {
            return Double.compare(weight, other.weight);
        }
    }

    public DijkstraRouteFinder(RoadNetwork n) {
        network = n;
        Collection<InterSection> interSections = network.getInterSections();
        vertexMap = new UnifiedMap<InterSection, Vertex>(interSections.size());//new HashMap<InterSection, Vertex>();
        // set-up vertices
        for (InterSection is : interSections) {
            Vertex v = new Vertex(is);

            v.previous = null;
            v.weight = Double.MAX_VALUE;
            v.inSection = Section.DISCONNECTED_SECTION;
            vertexMap.put(is, v);
        }
        Collection<Vertex> vertexes = vertexMap.values();
        for (Vertex v : vertexes) {
            ArrayList<Section> outs = v.host.getOutSections();
            v.neighbours = new Vertex[outs.size()];
            v.sections = new Section[outs.size()];
            for (int i = 0; i < outs.size(); i++) {
                Section section = outs.get(i);
                InterSection is = section.getTo();
                Vertex nv = vertexMap.get(is);
                v.neighbours[i] = nv;
                v.sections[i] = section;
            }
        }
    }

    private final void calc(Route r, InterSection from, InterSection to, int type) throws UnreachableException {
        //long time0 = System.currentTimeMillis();
        q.clear();

        final Vertex source = vertexMap.get(from);
        final Vertex dest = vertexMap.get(to);
        // set-up vertices
        Collection<Vertex> vertexes = vertexMap.values();
        for (Vertex v : vertexes) {
            v.previous = v == source ? source : null;
            v.weight = v == source ? 0.0 : Double.MAX_VALUE;
            v.inSection = v == source ? Section.NULL_SECTION : Section.DISCONNECTED_SECTION;
            if (v == source)  // don't need to put every vertex into set. the set growth during explore {JINY}
                q.add(v);

        }

        dijkstra(type);
        //Route r = new Route(origin, destination);
        Vertex v = dest;

        while (v != source) {
            if (v == null)
                throw new UnreachableException();
            if (v.inSection == null)
                return;
            r.push(v.inSection);
            v = v.previous;
        }
        //r.push(source.inSection);
        //long time1 = System.currentTimeMillis();
        //System.out.println(time1 - time0);
        return;

    }

    /**
     * Implementation of dijkstra's algorithm using a binary heap.
     */
    private final void dijkstra(final int type) {
        Vertex u, v;
        Section a;
        double w;
        double alternateDist;
        int i;
        u = q.pollFirst(); // vertex with shortest distance (first iteration will return source)
        if(u == null) return;
        do {
            //System.out.println(q.size());

//		         if (u.weight == Double.MAX_VALUE) break; // we can ignore u (and any other remaining vertices) since they are unreachable

            //look at distances to each neighbour
            for (i = 0; i < u.neighbours.length; i++) {
                //InterSection is = a.getTo(); //the neighbour in this iteration
                //v = vertexMap.get(is);
                a = u.sections[i];
                v = u.neighbours[i];
                w = 0;
                if (type == DISTANCE_OPTIMAL)
                    w = a.getDistance();
                else if (type == TIME_OPTIMAL)
                    w = a.getAvgPassTime();
                alternateDist = u.weight + w;
                if (alternateDist < v.weight) { // shorter path to neighbour found
                    q.remove(v);
                    v.weight = alternateDist;
                    v.inSection = a;
                    v.previous = u;
                    q.add(v);
                }
            }

            u = q.pollFirst();

        } while(u != null);
    }


    /*
    DP-15090193
    DP-21078063
    DP-500348
    Map area is too small
    why DP-21078063 - DP-15112427  [121.51792,31.28823] - [121.48445,31.40097]
    why DP-21078063 - DP-17890369  [121.51792,31.28823] - [121.37252,31.34473]
    why DP-21078063 - DP-13805097  [121.51792,31.28823] - [121.31462,31.39418]

    :DP-21078063 - DP-5584073
    P-21078063 - DP-2363170
     */
    public Route route(GeoPos from, GeoPos to, int type,
                       int expandTimes) throws UnreachableException {
        failed.clear();
        if(!network.contains(from) || !network.contains(to))
            throw new UnreachableException("Positions " + from + " or " + to + " are not in network.");

        Section f = network.findSection(from);
        Section t0 = network.findSection(to);

        if (f == null || t0 == null)
            return null;
        f = f.getNavigatable();
        t0 = t0.getNavigatable();
        final Route route = new Route(from, to);
        try {

            calc(route, f.getFrom(), t0.getTo(), type);

            return route;
        } catch (UnreachableException e) {
            //System.out.println(t0);
            //System.out.println("From Sec.:" + f.getId() + "," + "To Sec.:" + t0.getId());
        }

        //HashSet<String> failed = new HashSet<String>();

        failed.add(t0.wayId);
        final int times = expandTimes;
        if(times < 1)
            throw new UnreachableException();


            Collection<Section> tl = network.findSections(to, times);
       // System.out.println("Expand 1");
            if (f != null && !tl.isEmpty()) {
                for (Section t : tl) {
                    try {
                        if(failed.contains(t.wayId)) continue;
                        route.clear();
                        calc(route, f.getFrom(), t.getTo(), type);
                        logger.finer("!: From Sec.:" + f.getId() + "," + "To Sec.:" + t.getId() + "[" + t.wayId + "] ~~ " + t0.getId() + "[" + t0.wayId + "]");
                        t0.setNavigatable(t);
                        return route;
                    } catch (UnreachableException e) {
                        failed.add(t.wayId);
                        logger.finer("X: From Sec.:" + f.getId() + "," + "To Sec.:" + t.getId() + "[" + t.wayId + "] ~~ " + t0.getId() + "[" + t0.wayId + "]");
                    }

                }
            }
            failed.clear();
            failed.add(f.wayId);
            Collection<Section> fl = network.findSections(from, times);
       // System.out.println("Expand 2");
            if (t0 != null && !fl.isEmpty()) {
                for (Section f1 : fl) {
                    try {
                        if(failed.contains(f1.wayId)) continue;

                        route.clear();
                        calc(route, f1.getFrom(), t0.getTo(), type);
                        logger.finer("!: " + f.getId() + " ~~ From Sec.:" + f1.getId() + "," + "To Sec.:" + t0.getId());
                        f.setNavigatable(f1);
                        return route;
                    } catch (UnreachableException e) {
                        failed.add(f1.wayId);
                        logger.finer("X: " + f.getId() + " ~~ From Sec.:" + f1.getId() + "," + "To Sec.:" + t0.getId());
                    }

                }
            }


        throw new UnreachableException("From " + from + " to " + to + " is not navigable.");

    }


}
