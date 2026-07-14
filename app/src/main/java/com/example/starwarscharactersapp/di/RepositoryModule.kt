package com.example.starwarscharactersapp.di

import com.example.starwarscharactersapp.data.repository.StarWarsRepositoryImpl
import com.example.starwarscharactersapp.domain.StarWarsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStarWarsRepository(impl: StarWarsRepositoryImpl): StarWarsRepository
}
