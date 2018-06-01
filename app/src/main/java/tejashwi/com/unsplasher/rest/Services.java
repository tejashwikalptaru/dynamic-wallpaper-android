/*
 * Copyright (c) 2018, Tejashwi Kalp Taru
 */

package tejashwi.com.unsplasher.rest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;
import tejashwi.com.unsplasher.rest.model.RandomImagesObject;
import tejashwi.com.unsplasher.rest.model.SearchResult;

public interface Services {
    @GET(Constants.RANDOM)
    Call <List<RandomImagesObject>> getRandom(@Query("page") int page);

    @GET(Constants.SEARCH)
    Call <SearchResult> searchImage(@Query("query") String query, @Query("page") int page);

    @GET
    Call<ResponseBody> downloadImage(@Url String url);
}
