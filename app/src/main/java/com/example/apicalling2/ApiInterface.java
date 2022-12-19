package com.example.apicalling2;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {


    @GET("random")
    Call<Dog> dogGo();
}
