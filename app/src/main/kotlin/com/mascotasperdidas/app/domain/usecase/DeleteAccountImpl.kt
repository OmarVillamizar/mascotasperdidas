package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.port.`in`.DeleteAccount
import com.mascotasperdidas.app.domain.port.out.AuthRepository
import com.mascotasperdidas.app.domain.port.out.UserRepository
import javax.inject.Inject

class DeleteAccountImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : DeleteAccount {
    override suspend fun invoke() {
        userRepository.deleteCurrentUserDocument()
        authRepository.deleteCurrentUser()
    }
}
