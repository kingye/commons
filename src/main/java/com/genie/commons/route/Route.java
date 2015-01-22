package com.genie.commons.route;

import com.genie.commons.network.Section;
import com.genie.commons.geo.GeoPos;

import java.util.ArrayDeque;


// as stack
public class Route extends ArrayDeque<Section>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8129038678479564366L;
	private final GeoPos origin;
	private final GeoPos destination;

	public Route(GeoPos origin, GeoPos destination) {
		this.origin = origin;
		this.destination = destination;
	}

	public double getDistance() {
		double d = 0;
		for(Section s : this) {
			d = d + s.getDistance();
		}
		return d;
	}
	public double getAvgPassTime() {
		double d = 0;
		for(Section s : this) {
			d = d + s.getAvgPassTime();
		}
		return d;
	}
	public GeoPos getOrigin() {
		return origin;
	}

	public GeoPos getDestination() {
		return destination;
	}

	@Override
	public String toString() {
		String str = "Route{" +
				"origin=" + origin +
				", destination=" + destination +
				", time=" + getAvgPassTime() +
				", sections:[";

		for(Section s : this) {
			str = str + s.getId() + ",";
		}
		str = str + "]}";
		return str;
	}
}
