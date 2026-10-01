package com.example.askfaculty.core.firebase

import com.example.askfaculty.data.repository.AuthRepositoryImpl
import com.example.askfaculty.data.repository.CommunityRepositoryImpl
import com.example.askfaculty.data.repository.DoubtRepositoryImpl
import com.example.askfaculty.data.repository.NotificationRepositoryImpl
import com.example.askfaculty.data.repository.ProfileRepositoryImpl
import com.example.askfaculty.domain.repository.AuthRepository
import com.example.askfaculty.domain.repository.CommunityRepository
import com.example.askfaculty.domain.repository.DoubtRepository
import com.example.askfaculty.domain.repository.NotificationRepository
import com.example.askfaculty.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindDoubtRepository(impl: DoubtRepositoryImpl): DoubtRepository

    @Binds @Singleton
    abstract fun bindCommunityRepository(impl: CommunityRepositoryImpl): CommunityRepository

    @Binds @Singleton
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
