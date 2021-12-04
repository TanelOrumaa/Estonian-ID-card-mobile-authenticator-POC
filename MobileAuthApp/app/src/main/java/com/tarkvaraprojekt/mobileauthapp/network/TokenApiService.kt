package com.tarkvaraprojekt.mobileauthapp.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Class for making HTTP requests
 * Based on https://developer.android.com/courses/pathways/android-basics-kotlin-unit-4-pathway-2
 */
const val BASE_URL =
    "https://6bb0-85-253-195-252.ngrok.io"

private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
private val retrofit = Retrofit.Builder().addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL).build()

interface TokenApiService {
    @Headers("Content-Type: application/json")
    @POST("/auth/authentication")
    suspend fun postToken(@Body data: String): Response<TokenItem>
}

object TokenApi {
    val retrofitService : TokenApiService by lazy {
        retrofit.create(TokenApiService::class.java)
    }
}