package com.quickpoint.snookerboard.data.di

import android.content.Context
import com.quickpoint.snookerboard.data.DataStore
import com.quickpoint.snookerboard.data.database.SnookerDatabase
import com.quickpoint.snookerboard.domain.models.DomainBallManager
import com.quickpoint.snookerboard.domain.repository.DataStoreRepository
import com.quickpoint.snookerboard.domain.utils.MatchSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContext(@ApplicationContext context: Context) = context

    @Provides
    @Singleton
    fun provideDatabase(context: Context) = SnookerDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context) = DataStore(context)

    @Provides
    @Singleton
    fun provideSettings(dataStoreRepository: DataStoreRepository) = MatchSettings(dataStoreRepository)

    @Provides
    fun provideDomainBallManager(matchSettings: MatchSettings) = DomainBallManager(matchSettings)
}