package com.genie.commons.network;

import net.openhft.koloboke.collect.map.hash.HashObjObjMaps;

import java.util.ArrayList;
import java.util.Map;

public class Way extends ArrayList<Node>{
/**
	 * 
	 */
	private static final long serialVersionUID = -5003280259253431185L;
private String id;
	private final Map<String, String> tags = HashObjObjMaps.newMutableMap();
	private boolean oneWay;
	
	public Way(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}
	public String getTag(String key) {
		return tags.get(key);
	}
	public void putTag(String key, String type) {
		this.tags.put(key, type);
	}
	public boolean isOneWay() {
		return "yes".equals(tags.get("oneway"));
	}

	public Map<String, String> getTags() {
		return tags;
	}
	
}
