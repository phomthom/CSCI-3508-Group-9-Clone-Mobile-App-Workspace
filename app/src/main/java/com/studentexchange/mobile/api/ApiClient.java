package com.studentexchange.mobile.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
 public static final String BASE_URL = "http://10.0.2.2:3100/";
 private ApiClient() {}
 public static MarketplaceApi create() {
  OkHttpClient client = new OkHttpClient.Builder()
   .connectTimeout(5, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS)
   .callTimeout(20, TimeUnit.SECONDS)
   .followRedirects(false).followSslRedirects(false)
   .addInterceptor(chain -> {
    HttpUrl url = chain.request().url();
    String path = url.encodedPath();
    boolean allowed = path.equals("/api/listings") || path.matches("/api/listings/[1-9][0-9]*")
     || path.equals("/api/categories") || path.equals("/api/conditions");
    if (!url.scheme().equals("http") || !url.host().equals("10.0.2.2") || url.port() != 3100
      || !chain.request().method().equals("GET") || !allowed)
     throw new IOException("Only the local mock marketplace is allowed.");
    return chain.proceed(chain.request());
   }).build();
  return new Retrofit.Builder().baseUrl(BASE_URL).client(client)
   .addConverterFactory(GsonConverterFactory.create()).build().create(MarketplaceApi.class);
 }
}
