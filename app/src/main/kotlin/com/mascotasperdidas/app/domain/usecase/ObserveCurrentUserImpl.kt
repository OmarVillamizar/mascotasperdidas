package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.model.User
import com.mascotasperdidas.app.domain.port.`in`.ObserveCurrentUser
import com.mascotasperdidas.app.domain.port.out.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserImpl @Inject constructor(
    private val userRepository: UserRepository,
) : ObserveCurrentUser {
    override fun invoke(): Flow<User?> = userRepository.observeCurrentUser()
}
