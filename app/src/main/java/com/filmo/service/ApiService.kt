package com.filmo.service

import okhttp3.ResponseBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @GET("health")
    suspend fun ping(): ResponseBody

    @GET("sample/items")
    suspend fun fetchItems(): ResponseBody

    @FormUrlEncoded
    @POST("sample/items")
    suspend fun submitItem(
        @Field("title") title: String,
        @Field("description") description: String
    ): ResponseBody

    @POST("api/auth/login")
    suspend fun login(
        @Body body: RequestBody
    ): ResponseBody

    @GET("auth-test")
    suspend fun authTest(
        @Header("Authorization") authorization: String
    ): ResponseBody
}
