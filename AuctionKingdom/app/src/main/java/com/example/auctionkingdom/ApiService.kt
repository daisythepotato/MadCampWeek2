package com.example.auctionkingdom

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/saveUser")
    fun saveUser(@Body user: User): Call<Void>
}
