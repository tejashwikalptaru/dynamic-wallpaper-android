/*
 * Copyright (c) 2018, Tejashwi Kalp Taru
 */

package tejashwi.com.unsplasher.rest;

public class APIUtils {

    public static Services getClient() {
        return RestClient.getClient().create(Services.class);
    }
}
