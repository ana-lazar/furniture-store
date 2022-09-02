package com.ma.ikea.data.remote

import com.google.gson.GsonBuilder
import com.ma.ikea.data.Product
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

object ProductApi {
    interface Service {
        @GET("/products")
        suspend fun find(): List<Product>

        @GET("/product/{id}")
        suspend fun read(@Path("id") productId: String): Product

        @Headers("Content-Type: application/json")
        @POST("/product")
        suspend fun create(@Body product: Product): Product

        @Headers("Content-Type: application/json")
        @PUT("/product/{id}")
        suspend fun update(@Path("id") productId: String, @Body product: Product): Product

        @DELETE("/product/{id}")
        suspend fun delete(@Path("id") productId: String): Response<Unit>
    }

    private const val URL = "http://192.168.0.73:3000/"

    private val client: OkHttpClient = OkHttpClient.Builder().build()

    val service: Service = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .client(client)
        .build()
        .create(Service::class.java)
}