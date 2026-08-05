package com.example.tasknight.di

import com.example.tasknight.domain.repository.AuthRepository
import com.example.tasknight.domain.usecases.SignInAsGuestUseCase
import com.example.tasknight.domain.usecases.SignInWithEmailUseCase
import com.example.tasknight.domain.usecases.SignUpWithEmailUseCase
import com.example.tasknight.domain.usecases.UpgradeGuestToUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideSignInWithEmailUseCase(repository: AuthRepository): SignInWithEmailUseCase {
        return SignInWithEmailUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSignUpWithEmailUseCase(repository: AuthRepository): SignUpWithEmailUseCase {
        return SignUpWithEmailUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSignInAsGuestUseCase(repository: AuthRepository): SignInAsGuestUseCase {
        return SignInAsGuestUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpgradeGuestToUserUseCase(repository: AuthRepository): UpgradeGuestToUserUseCase {
        return UpgradeGuestToUserUseCase(repository)
    }
}
