package com.mascotasperdidas.app.domain.port.`in`

import com.mascotasperdidas.app.domain.model.User
import kotlinx.coroutines.flow.Flow

fun interface ObserveCurrentUser {
    operator fun invoke(): Flow<User?>
}
