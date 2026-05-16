package com.filmo.service

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.filmo.BuildConfig
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
            mockAppRepository.get()
        } else {
            remoteAppRepository.get()
        }
    }
}
