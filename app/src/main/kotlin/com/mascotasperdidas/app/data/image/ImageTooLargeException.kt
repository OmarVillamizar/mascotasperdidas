package com.mascotasperdidas.app.data.image

/**
 * Thrown when a compressed+encoded image exceeds the Firestore document size budget.
 * The [message] is the user-facing Spanish error string.
 * No Android imports — pure Kotlin.
 */
class ImageTooLargeException(message: String) : Exception(message)
