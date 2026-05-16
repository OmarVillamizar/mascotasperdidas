package com.mascotasperdidas.app.domain.usecase

import com.mascotasperdidas.app.domain.port.`in`.UpdateUserProfile
import com.mascotasperdidas.app.domain.port.out.UserRepository
import javax.inject.Inject

class UpdateUserProfileImpl @Inject constructor(
    private val userRepository: UserRepository,
) : UpdateUserProfile {
    override suspend fun invoke(name: String, phone: String) =
        userRepository.updateProfile(name, phone)
}
