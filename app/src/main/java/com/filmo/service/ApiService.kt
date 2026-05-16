package com.filmo.service

import okhttp3.ResponseBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

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

    @POST("api/auth/signup")
    suspend fun signup(
        @Body body: RequestBody
    ): ResponseBody

    @POST("api/auth/login")
    suspend fun login(
        @Body body: RequestBody
    ): ResponseBody

    @GET("api/auth/nickname/random")
    suspend fun fetchRandomNickname(): ResponseBody

    @GET("api/users/me")
    suspend fun fetchMe(
        @Header("Authorization") authorization: String
    ): ResponseBody

    @GET("auth-test")
    suspend fun authTest(
        @Header("Authorization") authorization: String
    ): ResponseBody

    @GET("api/movies")
    suspend fun fetchMovies(
        @Query("keyword") keyword: String? = null,
        @Query("genre") genre: String? = null,
        @Query("year") year: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: List<String>? = null
    ): ResponseBody

    @GET("api/movies/{seq}")
    suspend fun fetchMovie(
        @Path("seq") seq: Long
    ): ResponseBody

    @GET("api/movies/image/{imagePath}")
    suspend fun fetchMovieImage(
        @Path("imagePath", encoded = true) imagePath: String
    ): ResponseBody

    @GET("api/theaters")
    suspend fun fetchTheaters(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: List<String>? = null
    ): ResponseBody

    @GET("api/theaters/{theaCd}")
    suspend fun fetchTheater(
        @Path("theaCd") theaCd: String
    ): ResponseBody

    @POST("api/theaters/{theaCd}/save")
    suspend fun saveTheater(
        @Path("theaCd") theaCd: String
    ): ResponseBody

    @DELETE("api/theaters/{theaCd}/save")
    suspend fun removeSavedTheater(
        @Path("theaCd") theaCd: String
    ): ResponseBody

    @GET("api/tickets")
    suspend fun fetchTickets(): ResponseBody
}
