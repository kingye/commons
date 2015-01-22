package route;

import com.genie.commons.geo.GeoPos;
import com.genie.commons.network.RoadNetwork;
import com.genie.commons.network.RoadNetworkBuilder;
import com.genie.commons.network.TagFilter;
import com.genie.commons.route.*;
import net.openhft.koloboke.collect.map.hash.HashObjObjMaps;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by d032459 on 15/1/8.
 */
public class TestRoute {
    public static void main(String[] args) {
        try {
            RoadNetworkBuilder.instance.build(args[0],  new TagFilter() {
                @Override
                public boolean contains(HashMap<String, String> types) {
                    for(String id : types.keySet()) {
                        if("highway".equals(id)) {
                            String v = types.get(id);
                            if ("motorway".equals(v)
                                    || "trunk".equals(v)
                                    || "primary".equals(v)
                                    || "primary_link".equals(v)
                                    || "secondary".equals(v)
                                    || "tertiary".equals(v)
                                    || "motorway_link".equals(v)
                                    || "residential".equals(v)
                                    || "living_street".equals(v)
                                    || "unclassified".equals(v)
                                    || "service".equals(v)
                                    || "footway".equals(v)
                                    || "pedestrian".equals(v)
                                    || "cycleway".equals(v))
                                return true;
                        }
                    }
                    return false;

                }
            });

            RoadNetwork network = RoadNetworkBuilder.instance.getNetwork();
            DijkstraRouteFinder f = new DijkstraRouteFinder(network);
            long t0 = System.currentTimeMillis();
            //GeoPos{lat=31.19392, lng=121.53801} to GeoPos{lat=31.59162, lng=121.53189}
            Route r = f.route(new GeoPos(121.51792,31.28823), new GeoPos(121.48306,31.22475), RouteFinder.TIME_OPTIMAL, 1);
            long t1 = System.currentTimeMillis();
            System.out.println(r);
            System.out.println(t1 - t0);
            //DijkstraRouteFinder f1 = new DijkstraRouteFinder(network);
            //t0 = System.currentTimeMillis();
            //r = f1.route(new GeoPos(121.43953, 31.23924), new GeoPos(121.43669, 31.25633), RouteFinder.TIME_OPTIMAL, 0);
            //t1 = System.currentTimeMillis();
            //System.out.println(r);
            //System.out.println(t1 - t0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (UnreachableException e) {
            e.printStackTrace();
        }

    }
}
