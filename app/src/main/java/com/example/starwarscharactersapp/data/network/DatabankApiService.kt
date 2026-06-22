package com.example.starwarscharactersapp.data.network

import com.example.starwarscharactersapp.data.model.DatabankCharacterDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DatabankApiService {
    @GET("characters/name/{name}")
    suspend fun getCharacterByName(@Path("name") name: String): Response<List<DatabankCharacterDto>>
}
