package com.example.network

import retrofit2.Call
import retrofit2.http.GET

interface StatusService {
    @GET("status")
    fun getStatus(): Call<Status>

}