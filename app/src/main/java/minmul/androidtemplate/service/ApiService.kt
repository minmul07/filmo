package minmul.androidtemplate.service

import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
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
}
