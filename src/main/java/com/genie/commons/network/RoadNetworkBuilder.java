package com.genie.commons.network;

import com.genie.commons.geo.GeoPos;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;


public class RoadNetworkBuilder {
	public final static RoadNetworkBuilder instance = new RoadNetworkBuilder();
	private RoadNetwork network;
	private TagFilter filter;
	private static class RoadNetorkTagFilter implements com.genie.commons.network.TagFilter{

		public boolean contains(Map<String, String> types) {
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
							|| "unclassified".equals(v))
						return true;
				}
			}
			return false;

		}
	}

	private RoadNetworkBuilder() {

	}
	public RoadNetwork getNetwork() {
		return network;
	}
	public final synchronized void build(String filePath ) throws IOException, XMLStreamException {
		build(filePath, null);
	}
	public final synchronized void build(String filePath, TagFilter filter) throws IOException, XMLStreamException {
		if(network != null) return;


		OSMParser parser = new OSMParser();
		if(filter == null)
			filter = new RoadNetorkTagFilter();
		parser.parse(filePath, filter);
		RoadNetwork network = new RoadNetwork();
		network.setBounds(parser.getMin(), parser.getMax());
		Collection<Way> wayList = parser.getWays();
			for (Way way : wayList) {
				Section section = new Section(way.getId(), way.getId());
				network.addSection(section);
				section.setType(way.getTag("highway"));
				int l = way.size();
				for (int i = 0; i < l; i++) {
					Node n = way.get(i);
					section.addPoint(n.getLng(), n.getLat());
					network.assignSectionToTiles(new GeoPos(n.getLng(), n.getLat()), section);
					if (i == 0) {
						InterSection is = network.getInterSection(n.getId());
						if(is == null) {
							is = new InterSection(n.getId(),
								n.getLng(), n.getLat());
							network.addInterSection(is);
						}

						section.setFrom(is);
						is.addOutSection(section);
					}

					if (i == l - 1) {
						InterSection is = network.getInterSection(n.getId());
						if(is == null) {
							is = new InterSection(n.getId(), n.getLng(), n.getLat());
							network.addInterSection( is);
						}
						section.setTo(is);
						is.addInSection(section);
					}

					if(n.refcount > 1 && i != 0 && i != l-1){
						InterSection is = network.getInterSection(n.getId());
						if(is == null){
							is = new InterSection(n.getId(), n.getLng(), n.getLat());
							network.addInterSection( is);
						}
							
						// split
							Section section2 = new Section(section.getId() + "-s", way.getId());
							network.addSection(section2);
							section2.setType(way.getTag("highway"));
							section2.addPoint(n.getLng(), n.getLat());
							network.assignSectionToTiles(new GeoPos(n.getLng(), n.getLat()), section2);
							section2.setFrom(is);
							is.addOutSection(section2);
							
							
							//close the previous section
							section.setTo(is);
							is.addInSection(section);
							section.calcDistance();
							if(!way.isOneWay())
								network.addSection(section.reverseClone());
							
							// continue with section section
							section = section2;
							continue;
						
					}
				}



				section.calcDistance();
				if(!way.isOneWay())
					network.addSection(section.reverseClone());
			}
			
			//System.out.println(network.getInterSections().size());
			System.out.println(network.getSections().size());
			this.network = network;


	}
}
