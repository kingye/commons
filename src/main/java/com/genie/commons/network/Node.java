package com.genie.commons.network;


public class Node {
	private String id;
	private double lat;
	private double lng;
	public int refcount;
	public Node(String id, double lng, double lat) {
		this.lat = lat;
		this.lng = lng;
		this.id = id;
		refcount = 0;
	}
	public double getLat() {
		return lat;
	}
	public double getLng() {
		return lng;
	}
	public String getId() {
		return id;
	}

}
