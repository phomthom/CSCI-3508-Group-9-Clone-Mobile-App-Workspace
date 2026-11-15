package com.studentexchange.mobile.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** Read-only development adapter contracts. No authentication or write methods. */
public interface MarketplaceApi {
 @GET("api/listings") Call<List<Listing>> listings(@Query("search") String search, @Query("category") String category);
 @GET("api/listings/{id}") Call<Listing> detail(@Path("id") String id);
 @GET("api/categories") Call<List<String>> categories();
 @GET("api/conditions") Call<List<String>> conditions();

 class Listing {
  public Long id;
  public String title, category, condition, location, isbn, datetime, status, user;
  public String description, first_name, last_name, image_url;
  public List<String> images_url;
  public Double price;
 }
}
