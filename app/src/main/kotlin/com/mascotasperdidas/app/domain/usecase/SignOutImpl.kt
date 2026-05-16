package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.port.`in`.SignOut
import com.mascotasperdidas.app.domain.port.out.AuthRepository
import javax.inject.Inject

class SignOutImpl @Inject constructor(
    private val authRepository: AuthRepository,
) : SignOut {
    override suspend fun invoke() = authRepository.signOut()
}
