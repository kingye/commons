package com.genie.commons.network;

import java.util.ArrayList;
import java.util.HashMap;

public class Way extends ArrayList<Node>{
/**
	 * 
	 */
	private static final long serialVersionUID = -5003280259253431185L;
private String id;
	private final HashMap<String, String> tags = new HashMap<String, String>();
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

	public HashMap<String, String> getTags() {
		return tags;
	}
	
}
