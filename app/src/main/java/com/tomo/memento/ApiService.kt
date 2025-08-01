package com.tomo.memento

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("hello")
    fun getHello(): Call<MessageResponse>
}