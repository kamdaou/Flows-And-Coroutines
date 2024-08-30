package com.example.coroutines_flows_jc.data.data_source.remote

import com.example.coroutines_flows_jc.data.data_source.remote.dto.SwapiDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StarWarsApi {

    @GET("people/")
    suspend fun getPeople(
        @Query("page") page: Int
    ) : Response<SwapiDto>
}