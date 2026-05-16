package com.filmo.service

import okhttp3.ResponseBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("health")
    suspend fun ping(): ResponseBody

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
        @Header("Authorization") authorization: String? = null
    ): ResponseBody

    @GET("api/movies")
    suspend fun fetchMovies(
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sort") sort: List<String>? = listOf("seq,DESC")
    ): ResponseBody

    @GET("api/movies/{seq}")
    suspend fun fetchMovie(
        @Path("seq") seq: Long
    ): ResponseBody

    @GET("api/movies/image/{imagePath}")
    suspend fun fetchMovieImage(
        @Path("imagePath", encoded = true) imagePath: String
    ): ResponseBody

    @GET("api/tickets")
    suspend fun fetchTickets(): ResponseBody

    @GET("api/tickets/{ticketId}")
    suspend fun fetchTicket(
        @Path("ticketId") ticketId: Long
    ): ResponseBody

    @GET("api/tickets/public")
    suspend fun fetchPublicTickets(
        @Query("sort") sort: String = "latest"
    ): ResponseBody

    @POST("api/tickets")
    suspend fun createTicket(
        @Body body: RequestBody
    ): ResponseBody

    @PATCH("api/tickets/{ticketId}/share")
    suspend fun updateTicketShare(
        @Path("ticketId") ticketId: Long,
        @Query("showYn") showYn: Boolean
    ): ResponseBody

    @POST("api/likes/{ticketId}")
    suspend fun addLike(
        @Path("ticketId") ticketId: Long
    ): ResponseBody

    @DELETE("api/likes/{ticketId}")
    suspend fun removeLike(
        @Path("ticketId") ticketId: Long
    ): ResponseBody

    @PATCH("api/tickets/{ticketId}")
    suspend fun updateTicket(
        @Path("ticketId") ticketId: Long,
        @Body body: RequestBody
    ): ResponseBody

    @DELETE("api/tickets/{ticketId}")
    suspend fun deleteTicket(
        @Path("ticketId") ticketId: Long
    ): ResponseBody

    @GET("api/collections")
    suspend fun fetchCollections(): ResponseBody

    @DELETE("api/collections/{ticketId}")
    suspend fun removeCollection(
        @Path("ticketId") ticketId: Long
    ): ResponseBody
}
