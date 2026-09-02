package com.prammmoe.pictrim.di

import android.content.Context
import com.prammmoe.pictrim.data.AndroidImageRepository
import com.prammmoe.pictrim.domain.repository.ImageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindImageRepository(repository: AndroidImageRepository): ImageRepository
}
