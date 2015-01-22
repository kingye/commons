package com.genie.commons.geo;





/**
 * Created by d032459 on 14/12/22.
 */
public class GeoPos {
    public final double lat;
    public final double lng;

    public GeoPos(double lng, double lat) {
        this.lat = lat;
        this.lng = lng;
    }
    public GeoPos(String osm) {
        String[] strs = osm.split("/");
        lat = Double.valueOf(strs[0]);
        lng = Double.valueOf(strs[1]);
    }
   public GeoPos(Number lng, Number lat) {
       this(lng.doubleValue(), lat.doubleValue());
   }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoPos geoPos = (GeoPos) o;

        if (Double.compare(geoPos.lat, lat) != 0) return false;
        if (Double.compare(geoPos.lng, lng) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "GeoPos{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
