package com.genie.commons.network;

import com.genie.commons.geo.GeoPos;
import com.genie.commons.geo.GeoUtils;
import com.genie.commons.route.DijkstraRouteFinder;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.util.*;


public class RoadNetwork {
	private Map<String, InterSection> interSections;

	private Map<String, Section> sections;


	private TileMap<Section> tileSectionMap;

	private final static double Z = 15.0;

	private GeoPos min;
	private GeoPos max;


	public RoadNetwork() {
		sections = new UnifiedMap<String, Section>();//new HashMap<String, Section>();
		interSections = new UnifiedMap<String, InterSection>();// new HashMap<String, InterSection>();
		tileSectionMap = new TileMap<Section>(Z);


	}

	public GeoPos getMin() {
		return min;
	}

	public GeoPos getMax() {
		return max;
	}

	public void setBounds(GeoPos min, GeoPos max) {
		this.min = min;
		this.max = max;
	}

	public boolean contains(GeoPos p) {
		return p.lng >= min.lng && p.lat >= min.lat
				&& p.lng <= max.lng && p.lat <= max.lng;

	}

	public void addSection(Section s) {
		if(sections.get(s.getId()) != null)
			System.out.println(s.getId());
		sections.put(s.getId(), s);
		for(GeoPos p : s.getPath()) {
			tileSectionMap.addValue(p, s);
		}
	}

	public void assignSectionToTiles(GeoPos p, Section s) {

		tileSectionMap.addValue(p, s);

	}

	public void addInterSection(InterSection is) {
		if(interSections.get(is.getId()) != null)
			System.out.println(is.getId());

		interSections.put(is.getId(), is);



	}




	public InterSection getInterSection(String p) {
		return interSections.get(p);
	}
	public Section getSection(String p) {
		return sections.get(p);
	}
	public Collection<Section> getSections() {
	
		return sections.values();
	}
	
	public Collection<InterSection> getInterSections() {
		return interSections.values();
	}
	

	public Section findSection(GeoPos p) {
		for(int i = 0; true; i++) {
			Section s = tileSectionMap.findValue(p, i);
			if (s != null)
				return s;
		}
	}
	public Section findSection(double lng, double lat) {
		return findSection(new GeoPos(lng, lat));
	}
	public Collection<Section> findSections(GeoPos pt, int level) {
		return tileSectionMap.findValues(pt, level);

	}

}
