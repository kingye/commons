package com.genie.commons.network;

import com.genie.commons.geo.GeoPos;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by d032459 on 15/1/5.
 */
public class OSMParser {
    private final HashMap<String, Node> nodeList = new HashMap<String, Node>();
    private final HashMap<String, Way> wayList = new HashMap<String, Way>();

    private GeoPos min;
    private GeoPos max;

    public GeoPos getMin() {
        return min;
    }


    public GeoPos getMax() {
        return max;
    }


    public Collection<Way> getWays() {
        return wayList.values();

    }

    public void parse(String file, TagFilter filter) throws IOException, XMLStreamException {
        File f = new File(file);
        FileInputStream fi;

        fi = new FileInputStream(f);
        parse(fi, filter);
        fi.close();
    }

    public void parse(InputStream in, TagFilter filter ) throws IOException, XMLStreamException {

        nodeList.clear();
        wayList.clear();
        Node currNode = null;
        Way currWay = null;

        XMLInputFactory factory = XMLInputFactory.newInstance();



        XMLStreamReader reader = factory.createXMLStreamReader(in);



        while (reader.hasNext()) {

            int event = reader.next();

            switch (event) {

                case XMLStreamConstants.START_ELEMENT:
                    if("bounds".equals(reader.getLocalName())) {
                        min = new GeoPos(Double.parseDouble(reader.getAttributeValue(null, "minlon")),
                                Double.parseDouble(reader.getAttributeValue(null, "minlat")));
                        max = new GeoPos(Double.parseDouble(reader.getAttributeValue(null, "maxlon")),
                                Double.parseDouble(reader.getAttributeValue(null, "maxlat")));
                    }
                    if ("node".equals(reader.getLocalName())) {
                        currWay = null;
                        currNode = new Node(
                                reader.getAttributeValue(null, "id"),
                                Double.parseDouble(reader.getAttributeValue(
                                        null, "lon")),
                                Double.parseDouble(reader.getAttributeValue(
                                        null, "lat")));

                    }

                    if ("way".equals(reader.getLocalName())) {
                        String wid = reader.getAttributeValue(null, "id");
                        //if(wid.equals("202418990")) {
                          //  System.out.println(wid);
                        //}
                        currWay = new Way(wid);
                    }
                    if ("nd".equals(reader.getLocalName())) {
                        if (currWay == null)
                            break;
                        String id = reader.getAttributeValue(null, "ref");
                        Node n = nodeList.get(id);
                        if (n != null) {
                            currWay.add(n);
                            n.refcount++;
                        }

                    }
                    if ("tag".equals(reader.getLocalName())) {
                        String k = reader.getAttributeValue(null, "k");
                        String v = reader.getAttributeValue(null, "v");
                        if (currWay != null)
                           currWay.putTag(k, v);
                    }

                    break;

                case XMLStreamConstants.END_ELEMENT:

                    if ("node".equals(reader.getLocalName())) {

                        nodeList.put(currNode.getId(), currNode);
                    }
                    if ("way".equals(reader.getLocalName())) {
                        if (currWay != null) {
                            if (filter.contains(currWay.getTags()))
                                wayList.put(currWay.getId(), currWay);
                        }
                    }
                    break;

            }

        }
        System.out.println(nodeList.keySet().size());
        System.out.println(wayList.keySet().size());
    }

}
