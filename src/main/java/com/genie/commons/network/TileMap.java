package com.genie.commons.network;

import com.genie.commons.geo.GeoPos;
import com.genie.commons.geo.GeoUtils;

import net.openhft.koloboke.collect.map.hash.HashObjObjMaps;


import java.util.*;

/**
 * Created by d032459 on 15/1/7.
 */
public class TileMap<T> {
    public final static class TileId {
        final int x;
        final int y;
        final double z;
        public TileId(int x, int y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public TileId(double lon, double lat, double z) {
            this.x = GeoUtils.long2tilex(lon, z);
            this.y = GeoUtils.lat2tiley(lat, z);
            this.z = z;

        }
        public TileId(GeoPos p, double z) {
            this.x = GeoUtils.long2tilex(p.lng, z);
            this.y = GeoUtils.lat2tiley(p.lat, z);
            this.z = z;

        }


        @Override
        public String toString() {
            return "TileId{" +
                    "x=" + x +
                    ", y=" + y +
                    ", z=" + z +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TileId tileId = (TileId) o;

            if (x != tileId.x) return false;
            if (y != tileId.y) return false;
            if (Double.compare(tileId.z, z) != 0) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = x;
            result = 31 * result + y;
            temp = Double.doubleToLongBits(z);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }
    private final static class DistanceComparator implements Comparator<GeoPos> {
        final GeoPos center;
        public DistanceComparator(GeoPos center) {
            this.center = center;
        }

        @Override
        public int compare(GeoPos o1, GeoPos o2) {
            double d1 = GeoUtils.distance(o1.lng, o1.lat, center.lng, center.lat);
            double d2 = GeoUtils.distance(o2.lng, o2.lat, center.lng, center.lat);
            return Double.compare(d1, d2);
        }
    }

    private final Map<TileId, Map<GeoPos, T>> map;
    private final double z;

    public TileMap(double z) {
        //Use GS-collections for better performance of hashmap -- 2015-01-22
        this.map = HashObjObjMaps.newMutableMap();//new HashMap<TileId, Map<GeoPos, T>>();
        this.z = z;
    }

    public void addValue(GeoPos p, T v) {
        TileId id = new TileId(p, z);
        Map<GeoPos, T> values = getValues(id);
        if(values == null) {
            //Use GS for better performance of hashmap -- 2015-01-22
            values = HashObjObjMaps.newMutableMap();//new HashMap<GeoPos, T>();
            map.put(id, values);
        }
        values.put(p, v);

    }


    public Map<GeoPos, T> getValues(TileId id) {
        return map.get(id);
    }

    public Map<GeoPos, T> getValues(GeoPos p) {

            Map<GeoPos, T> v = getValues(p, 0);

                return v;

    }

    public Map<GeoPos, T> getValues(GeoPos p, int level) {
        int d = level; //number of total levels


        int dim = 2 * d + 1;
        int s = dim * dim;

        TileId center = new TileId(p, z);
        TileId[] ids = new TileId[s];
        for(int x = 0; x < dim; x++) {
            for(int y = 0; y < dim; y++) {
                int m = x - d ;
                int n = y - d;
                ids[x*dim + y] = new TileId(center.x + m, center.y + n, z);
            }

        }

        HashMap<GeoPos, T> r = new HashMap<GeoPos, T>();
        for(int i = 0; i < s; i++) {
            Map<GeoPos, T> v = this.getValues(ids[i]);
            if(v != null)
                r.putAll(v);
        }
        return r;
    }

    public T getValue(GeoPos p, int level) {
        Map<GeoPos, T> values = this.getValues(p, level);
        return values.get(p);
    }
    public T getValue(GeoPos p) {
        return getValue(p, 0);
    }

    public T findValue(GeoPos p, int level) {

        Map<GeoPos, T> values = this.getValues(p, level);
        double dist = Double.MAX_VALUE;
        GeoPos nearstPos = null;
        Collection<GeoPos> keys = values.keySet();
        for(GeoPos pos : keys) {
            double d = GeoUtils.distance(p.lng, p.lat, pos.lng, pos.lat);
            if(d < dist) {
                dist = d;
                nearstPos = pos;
            }
        }
        if(nearstPos != null)
            return values.get(nearstPos);
        else
            return null;
    }

    public T findValue(GeoPos p) {
        return findValue(p, 0);
    }
    public Collection<T> findValues(GeoPos pt, int level) {
        Map<GeoPos, T > values =  getValues(pt, level);
        TreeSet<GeoPos> sorted = new TreeSet<GeoPos>(new DistanceComparator(pt));
        sorted.addAll(values.keySet());
        ArrayList<T> r = new ArrayList<T>(values.size());
        for(GeoPos p : sorted) {
            r.add(values.get(p));
        }
        return r;
    }
}
