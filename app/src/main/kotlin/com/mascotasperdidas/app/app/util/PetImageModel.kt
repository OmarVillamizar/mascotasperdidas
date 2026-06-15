package com.mascotasperdidas.app.app.util

/**
 * Returns the correct Coil [AsyncImage] model for a polymorphic [imageUrl] value:
 *  - null / blank   → null  (Coil shows placeholder / nothing)
 *  - starts "http"  → the original String (remote URL pass-through to Coil)
 *  - otherwise      → decoded [ByteArray] (Coil's ByteArrayFetcher handles it natively)
 *
 * Inline decode uses [java.util.Base64] (JDK 17 / min SDK 26 safe).
 * NO import from data/ — per CLAUDE.md §4.2 hexagonal boundary rule.
 */
fun petImageModel(value: String?): Any? = when {
    value.isNullOrBlank() -> null
    value.startsWith("http", ignoreCase = true) -> value
    else -> runCatching {
        java.util.Base64.getDecoder().decode(value)
    }.getOrNull()
}
