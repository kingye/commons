package com.genie.commons.network;

import java.util.HashMap;

/**
 * Created by d032459 on 15/1/5.
 */
public interface TagFilter {
    public boolean contains(HashMap<String, String> types);
}
