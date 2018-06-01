/*
 * Copyright (c) 2018, Tejashwi Kalp Taru
 */

package tejashwi.com.unsplasher.rest.model;

import java.util.HashMap;
import java.util.Map;

public class Tag {
    private String title;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
