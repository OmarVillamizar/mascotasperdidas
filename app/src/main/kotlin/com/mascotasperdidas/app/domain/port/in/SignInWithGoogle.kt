package com.mascotasperdidas.app.domain.port.`in`

fun interface SignInWithGoogle {
    suspend operator fun invoke(idToken: String)
}
