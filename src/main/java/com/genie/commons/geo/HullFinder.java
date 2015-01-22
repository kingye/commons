package com.genie.commons.geo;

import java.util.List;

/**
 * Created by d032459 on 14/12/22.
 */
public interface HullFinder {
    public List<GeoPos> hull(List<GeoPos> pts);
}
