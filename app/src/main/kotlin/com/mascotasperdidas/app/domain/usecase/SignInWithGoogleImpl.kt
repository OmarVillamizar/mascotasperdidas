package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.port.`in`.SignInWithGoogle
import com.mascotasperdidas.app.domain.port.out.AuthRepository
import javax.inject.Inject

class SignInWithGoogleImpl @Inject constructor(
    private val authRepository: AuthRepository,
) : SignInWithGoogle {
    override suspend fun invoke(idToken: String) = authRepository.signInWithGoogleIdToken(idToken)
}
