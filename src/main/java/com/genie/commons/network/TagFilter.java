package com.genie.commons.network;

import java.util.Map;

/**
 * Created by d032459 on 15/1/5.
 */
public interface TagFilter {
    public boolean contains(Map<String, String> types);
}
