package com.mascotasperdidas.app.data.image

object Base64ImageCodec {

    const val MAX_DOC_BASE64_BYTES = 921_600 // 900 × 1024 — conservative Firestore 1 MiB guard

    /**
     * Encodes [jpeg] bytes as a base64 string (no line wrapping, no padding).
     * Uses [java.util.Base64] (JDK 17 / API 26+) — zero Android imports, pure JVM.
     */
    fun encode(jpeg: ByteArray): String =
        java.util.Base64.getEncoder().withoutPadding().encodeToString(jpeg)

    /**
     * Decodes a base64 string back to raw bytes.
     * Uses [java.util.Base64.getDecoder] which handles both padded and unpadded input.
     */
    fun decode(base64: String): ByteArray =
        java.util.Base64.getDecoder().decode(base64)

    /**
     * Heuristic discriminator: returns `true` when [value] looks like a base64 payload
     * (non-blank AND does not start with "http").
     * Legacy Firestore docs use http(s) URLs; new docs use raw base64 strings.
     */
    fun isBase64(value: String): Boolean =
        value.isNotBlank() && !value.startsWith("http", ignoreCase = true)

    /**
     * Returns `true` when the base64 string length exceeds the Firestore document budget.
     * A base64 string's ASCII length equals its byte size, so length > threshold is the guard.
     */
    fun exceedsBudget(base64: String): Boolean =
        base64.length > MAX_DOC_BASE64_BYTES
}
