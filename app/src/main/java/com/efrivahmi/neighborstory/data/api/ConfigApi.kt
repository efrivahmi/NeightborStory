package com.efrivahmi.neighborstory.data.api

import com.efrivahmi.neighborstory.data.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ConfigApi {
    @FormUrlEncoded
    @POST("register")
    fun neighborRegister(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<Register>

    @FormUrlEncoded
    @POST("login")
    fun neighborLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<Login>

    @GET("stories")
    suspend fun storyNeighbor(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Story

    @GET("stories")
    fun getStoryWithMaps(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("location") loc: Int = 1
    ): Call<Story>

    @Multipart
    @POST("stories")
    fun uploadDataNeighbor(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null
    ): Call<AddStory>
}
