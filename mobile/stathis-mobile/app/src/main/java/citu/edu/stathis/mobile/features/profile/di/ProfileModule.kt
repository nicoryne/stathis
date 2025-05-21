package citu.edu.stathis.mobile.features.profile.di

import citu.edu.stathis.mobile.features.profile.data.repository.UserProfileRepository
import citu.edu.stathis.mobile.features.profile.domain.repository.IUserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepository: UserProfileRepository
    ): IUserProfileRepository

}