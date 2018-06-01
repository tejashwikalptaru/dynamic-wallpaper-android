/*
 * Copyright (c) 2018, Tejashwi Kalp Taru
 */

package tejashwi.com.unsplasher.rest.model;

import java.util.HashMap;
import java.util.Map;

public class ProfileImage {
    private String small;
    private String medium;
    private String large;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
