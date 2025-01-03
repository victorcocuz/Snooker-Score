package com.quickpoint.snookerboard.data.di

import android.content.Context
import com.quickpoint.snookerboard.data.DataStore
import com.quickpoint.snookerboard.data.database.SnookerDatabase
import com.quickpoint.snookerboard.domain.models.BallFactory
import com.quickpoint.snookerboard.domain.models.DomainBallManager
import com.quickpoint.snookerboard.domain.models.DomainBreakManager
import com.quickpoint.snookerboard.domain.models.DomainFrameManager
import com.quickpoint.snookerboard.domain.models.DomainScoreManager
import com.quickpoint.snookerboard.domain.models.PotFactory
import com.quickpoint.snookerboard.domain.repository.DataStoreRepository
import com.quickpoint.snookerboard.domain.usecases.AssignPotUseCase
import com.quickpoint.snookerboard.domain.usecases.HandlePotBallUseCase
import com.quickpoint.snookerboard.domain.usecases.HandleUndoPotBallUseCase
import com.quickpoint.snookerboard.domain.utils.MatchConfig
import com.quickpoint.snookerboard.domain.utils.MatchConfigImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
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
    fun provideSettings(dataStoreRepository: DataStoreRepository): MatchConfig = MatchConfigImpl(dataStoreRepository)

    @Provides
    @Singleton
    fun provideBallFactory(matchConfig: MatchConfig) = BallFactory(matchConfig)

    @Provides
    @Singleton
    fun providePotFactory(
        matchConfig: MatchConfig,
        ballFactory: BallFactory,
        dataStoreRepository: DataStoreRepository,
    ) = PotFactory(matchConfig, ballFactory, dataStoreRepository)

    @Provides
    @Singleton
    fun provideDomainBallManager(matchConfig: MatchConfig, ballFactory: BallFactory) = DomainBallManager(matchConfig, ballFactory)

    @Provides
    @Singleton
    fun provideDomainBreakManager(
        matchConfig: MatchConfig,
    ): DomainBreakManager {
        Timber.e("how many times")
        return DomainBreakManager(matchConfig)
    }

    @Provides
    @Singleton
    fun provideDomainScoreManager(matchConfig: MatchConfig) = DomainScoreManager(matchConfig)

    @Provides
    @Singleton
    fun provideDomainFrameManager(matchConfig: MatchConfig) = DomainFrameManager(matchConfig)

    @Provides
    @Singleton
    fun provideAssignPotUseCase(
        matchConfig: MatchConfig,
        frameManager: DomainFrameManager,
        handlePotBallUseCase: HandlePotBallUseCase,
        handleUndoPotBallUseCase: HandleUndoPotBallUseCase,
        breakManager: DomainBreakManager,
        ballManager: DomainBallManager,
        potFactory: PotFactory,
    ) = AssignPotUseCase(matchConfig, frameManager, handlePotBallUseCase, handleUndoPotBallUseCase, breakManager, ballManager, potFactory)
}