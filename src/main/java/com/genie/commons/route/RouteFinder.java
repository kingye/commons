package com.genie.commons.route;


import com.genie.commons.geo.GeoPos;

public interface RouteFinder {
	public final static int DISTANCE_OPTIMAL = 0;
	public final static int TIME_OPTIMAL = 1;
	//public Route calc(InterSection from, InterSection to, int type)throws UnreachableException;
	public Route route(GeoPos from, GeoPos to, int type, int expandTimes) throws UnreachableException;
}
