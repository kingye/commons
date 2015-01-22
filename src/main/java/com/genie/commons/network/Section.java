package com.genie.commons.network;

import com.genie.commons.geo.GeoPos;
import com.genie.commons.geo.GeoUtils;


public class Section {

	private InterSection from;
	private InterSection to;
	private Path path;
	private String type;
	private final String id;
	private double dist;
	private double avgSpeed;
	public final static Section DISCONNECTED_SECTION; 
	public final static Section NULL_SECTION;
	public final String wayId;
	private Section navigatable;
	static {
		DISCONNECTED_SECTION = new Section("DISCONNECTED_SECTION", "DISCONNECTED_SECTION");
		DISCONNECTED_SECTION.dist = Double.MAX_VALUE;
		NULL_SECTION = new Section("NULL_SECTION", "NULL_SECTION");
		NULL_SECTION.dist = 0;
		
	}

	public Section(String id, String wayId) {
		path = new Path();
		this.id = id;
		this.wayId = wayId;
		dist = 0;
	}
	
	public Section reverseClone() {
		Section s = new Section(this.id + "-r", this.wayId);
		s.to = this.from;
		s.from = to;
		s.type = this.type;
		//System.out.println(this.to);
		s.to.addInSection(s);
		s.from.addOutSection(s);
		s.avgSpeed = this.avgSpeed;

		int size = path.size();
		for(int i = size - 1; i >= 0; i--) {
			s.path.add(path.get(i));
		}
		s.calcDistance();
		return s;
	}

	public synchronized Section getNavigatable() {
		if(navigatable == null)
			return this;
		else
			return navigatable;
	}

	public synchronized void setNavigatable(Section navigatable) {
		this.navigatable = navigatable;
	}

	public InterSection getFrom() {
		return from;
	}
	
	public InterSection getTo() {
		return to;
	}

	public void addPoint(double lng, double lat) {
		path.add(lng, lat);
	}
	public void addPoint(GeoPos p) {
		path.add(p);
	}

	public void setFrom(InterSection from) {
		this.from = from;
	}

	public void setTo(InterSection to) {
		this.to = to;
	}

	public Path getPath() {
		return path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		if ("motorway".equals(type)) {
			avgSpeed = 120.0;
		} else if("trunk".equals(type)) {
			avgSpeed = 100.0;
		} else if("primary".equals(type)) {
			avgSpeed = 60.0;
		} else if("secondary".equals(type)) {
			avgSpeed = 50.0;
		} else if("tertiary".equals(type)) {
			avgSpeed = 30.0;
		} else if("primary_link".equals(type)) {
			avgSpeed = 60.0;
		} else if("motorway_link".equals(type)) {
			avgSpeed = 70.0;
		} else if("service".equals(type)) {
			avgSpeed = 5.0;
		} else if("footway".equals(type)) {
			avgSpeed = 5.0;
		} else if("pedestrian".equals(type)) {
			avgSpeed = 5.0;
		} else if("cycleway".equals(type)) {
			avgSpeed = 5.0;
		} else
			avgSpeed = 30.0;
	}

	public String getId() {
		return id;
	}
	public double getDistance() {
		return this.dist;
	}
	public double getAvgPassTime() { // in sec.
		return (this.dist / avgSpeed) * 3600;
	}
	public void calcDistance() {
		path.trimToSize();
		GeoPos from = null;
		GeoPos to = null;
		for(GeoPos p : this.path) {
			if(from == null) {
				from = p;
				continue;
			}
			if(to == null)
				to = p;
			dist = dist + GeoUtils.distance(from.lng, from.lat, to.lng, to.lat);
		}
	}

	public double getAvgSpeed() {
		return avgSpeed;
	}

	public void setAvgSpeed(double avgSpeed) {
		this.avgSpeed = avgSpeed;
	}
	
	public String toString() {
		
			return this.from.toString() + " - " + this.to.toString();
	
	}



	
}
