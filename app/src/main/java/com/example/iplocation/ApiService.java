package com.example.iplocation;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("json/{ip}")
    Call<IpApiResponse> getIpInfo(@Path("ip") String ip);
}
