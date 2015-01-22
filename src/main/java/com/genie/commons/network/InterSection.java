package com.genie.commons.network;

import com.genie.commons.geo.GeoPos;

import java.util.ArrayList;


public class InterSection {
	private final GeoPos position;
	private ArrayList<Section> outSections;
	private ArrayList<Section> inSections;
	private final String id;


	public InterSection(String id, double lng, double lat) {
		position = new GeoPos(lng, lat);
		outSections = new ArrayList<Section>();
		inSections = new ArrayList<Section>();
		this.id = id;

	}
	public GeoPos getPosition() {
		return position;
	}

	public ArrayList<Section> getOutSections() {
		return outSections;
	}
	
	public ArrayList<Section> getInSections() {
		return inSections;
	}
	
	public void addOutSection(Section outSection) {
		this.outSections.add(outSection);
	}
	
	public void addInSection(Section inSection) {
		this.inSections.add(inSection);
	}
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "InterSection{" +
				"position=" + position +
				", id='" + id + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		InterSection that = (InterSection) o;

		if (!id.equals(that.id)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}


}
