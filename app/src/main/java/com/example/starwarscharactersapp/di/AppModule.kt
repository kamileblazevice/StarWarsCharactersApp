package com.example.starwarscharactersapp.di

import android.content.Context
import androidx.room.Room
import com.example.starwarscharactersapp.BuildConfig
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.starwarscharactersapp.data.helper.NetworkMonitor
import com.example.starwarscharactersapp.data.local.StarWarsDao
import com.example.starwarscharactersapp.data.local.StarWarsDatabase
import com.example.starwarscharactersapp.data.network.DatabankApiService
import com.example.starwarscharactersapp.data.network.SwapiApiService
import com.example.starwarscharactersapp.data.repository.StarWarsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideSwapiApiService(): SwapiApiService =
        Retrofit.Builder()
            .baseUrl(BuildConfig.SWAPI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SwapiApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabankApiService(): DatabankApiService =
        Retrofit.Builder()
            .baseUrl(BuildConfig.DATABANK_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DatabankApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StarWarsDatabase =
        Room.databaseBuilder(
            context,
            StarWarsDatabase::class.java,
            StarWarsDatabase.DATABASE_NAME,
        ).fallbackToDestructiveMigration(dropAllTables = true).build()

    @Provides
    @Singleton
    fun provideDao(database: StarWarsDatabase): StarWarsDao =
        database.dao

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor =
        NetworkMonitor(context)

    @Provides
    @Singleton
    fun provideRepository(
        api: SwapiApiService,
        databankApi: DatabankApiService,
        dao: StarWarsDao,
    ): StarWarsRepository =
        StarWarsRepository(
            api,
            databankApi,
            dao,
        )

}
