package com.genie.commons.network;

import com.genie.commons.geo.GeoPos;
import com.genie.commons.geo.GeoUtils;

import java.util.ArrayList;


public class Path extends ArrayList<GeoPos>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4502588264898355984L;
	public Path() {
	}
	public void add(double lng, double lat) {
		add(new GeoPos(lng, lat));
	}
	
	/**
	 * 
	 * @param pos
	 * @return to segment index
	 */
	public int findNearBySegment(GeoPos pos) {
		
		int N = this.size();
		if( N <= 0) return 0;
		GeoPos from = this.get(0);
		double minDis = Double.MAX_VALUE;
		int minIx = 0;
		for(int i = 0; i < N; i++) {
			GeoPos to = this.get(i);
			double d = GeoUtils.distanceToLine(from.lng, from.lat, to.lng, to.lat, pos.lng, pos.lat);
			if(d < minDis) {
				minDis = d;
				minIx = i;
			}
			from = to;	
		}
		
		return minIx;
	}
	
	public GeoPos pointOnPathSegment(int segmentix, GeoPos pos) {
		if(segmentix == 0) 
			return this.get(0);
		GeoPos from = this.get(segmentix - 1);
		GeoPos to = this.get(segmentix);
		return GeoUtils.nearestPointOnLine(from.lng, from.lat, to.lng, to.lat, pos.lng, pos.lat, false);
	}
}
