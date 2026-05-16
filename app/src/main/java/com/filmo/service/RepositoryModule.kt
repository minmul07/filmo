package com.filmo.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.filmo.BuildConfig
import timber.log.Timber
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAppRepository(
        mockAppRepository: Provider<MockAppRepository>,
        remoteAppRepository: Provider<RemoteAppRepository>
    ): AppRepository {
        return if (BuildConfig.USE_MOCK_REPOSITORY) {
            Timber.d("RepositoryModule.provideAppRepository selected=mock")
            mockAppRepository.get()
        } else {
            Timber.d("RepositoryModule.provideAppRepository selected=remote")
            remoteAppRepository.get()
        }
    }

    @Provides
    @Singleton
    fun provideTheaterBookmarkStore(localDisk: LocalDisk): TheaterBookmarkStore {
        return localDisk
    }
}
