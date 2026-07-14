package com.example.starwarscharactersapp.di

import android.content.Context
import com.example.starwarscharactersapp.BuildConfig
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.data.network.DatabankApiService
import com.example.starwarscharactersapp.data.network.SwapiApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private fun buildRetrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideSwapiApiService(): SwapiApiService =
        buildRetrofit(BuildConfig.SWAPI_BASE_URL).create(SwapiApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabankApiService(): DatabankApiService =
        buildRetrofit(BuildConfig.DATABANK_BASE_URL).create(DatabankApiService::class.java)

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor =
        NetworkMonitor(context)
}
