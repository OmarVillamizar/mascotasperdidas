package com.mascotasperdidas.app.app.di

import com.mascotasperdidas.app.data.fake.FakeAuthRepository
import com.mascotasperdidas.app.data.fake.FakePetReportRepository
import com.mascotasperdidas.app.data.fake.FakeUserRepository
import com.mascotasperdidas.app.domain.port.out.AuthRepository
import com.mascotasperdidas.app.domain.port.out.PetReportRepository
import com.mascotasperdidas.app.domain.port.out.UserRepository
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
    abstract fun bindAuthRepository(impl: FakeAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: FakeUserRepository): UserRepository

    @Binds
    @Singleton
    abstract fun bindPetReportRepository(impl: FakePetReportRepository): PetReportRepository
}
