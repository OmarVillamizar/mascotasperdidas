package com.mascotasperdidas.app.data.image

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Base64ImageCodecTest {

    // ── Round-trip ──────────────────────────────────────────────────────────

    @Test
    fun encode_then_decode_roundTrip_returnsOriginalBytes() {
        val original = byteArrayOf(0x00, 0x01, 0x7F, 0x80.toByte(), 0xFF.toByte(), 0x42)
        val encoded = Base64ImageCodec.encode(original)
        val decoded = Base64ImageCodec.decode(encoded)
        assertArrayEquals("Round-trip must return original bytes", original, decoded)
    }

    // ── Encoding format ─────────────────────────────────────────────────────

    @Test
    fun encode_producesAsciiString_noNewlines() {
        val input = ByteArray(300) { it.toByte() }
        val result = Base64ImageCodec.encode(input)
        assertNotNull(result)
        assertFalse("Encoded string must not contain '\\n'", result.contains('\n'))
        assertFalse("Encoded string must not contain '\\r'", result.contains('\r'))
        // All chars must be valid base64 alphabet (A-Z, a-z, 0-9, +, /, =) but withoutPadding drops '='
        assertTrue(
            "Encoded string must only contain base64 alphabet chars",
            result.all { it.isLetterOrDigit() || it == '+' || it == '/' },
        )
    }

    // ── exceedsBudget boundaries ────────────────────────────────────────────

    @Test
    fun exceedsBudget_aboveThreshold_returnsTrue() {
        val longString = "A".repeat(Base64ImageCodec.MAX_DOC_BASE64_BYTES + 1)
        assertTrue(
            "String of length MAX+1 must exceed budget",
            Base64ImageCodec.exceedsBudget(longString),
        )
    }

    @Test
    fun exceedsBudget_atThreshold_returnsFalse() {
        val exactString = "A".repeat(Base64ImageCodec.MAX_DOC_BASE64_BYTES)
        assertFalse(
            "String of exactly MAX_DOC_BASE64_BYTES must NOT exceed budget (boundary is exclusive)",
            Base64ImageCodec.exceedsBudget(exactString),
        )
    }

    @Test
    fun exceedsBudget_belowThreshold_returnsFalse() {
        val shortString = "A".repeat(180_000)
        assertFalse(
            "String of 180_000 chars must NOT exceed budget",
            Base64ImageCodec.exceedsBudget(shortString),
        )
    }

    // ── isBase64 discrimination ─────────────────────────────────────────────

    @Test
    fun isBase64_httpsUrl_returnsFalse() {
        assertFalse(
            "HTTPS URL must not be treated as base64",
            Base64ImageCodec.isBase64("https://placedog.net/500"),
        )
    }

    @Test
    fun isBase64_httpLowercase_returnsFalse() {
        assertFalse(
            "http URL must not be treated as base64",
            Base64ImageCodec.isBase64("http://example.com"),
        )
    }

    @Test
    fun isBase64_httpUppercase_returnsFalse() {
        assertFalse(
            "HTTP URL (uppercase) must not be treated as base64",
            Base64ImageCodec.isBase64("HTTP://example.com"),
        )
    }

    @Test
    fun isBase64_validBase64String_returnsTrue() {
        val knownBase64 = Base64ImageCodec.encode(byteArrayOf(0x01, 0x02, 0x03, 0x04))
        assertTrue(
            "A known base64 string not starting with 'http' must return true",
            Base64ImageCodec.isBase64(knownBase64),
        )
    }

    @Test
    fun isBase64_blankString_returnsFalse() {
        assertFalse(
            "Empty string must not be treated as base64",
            Base64ImageCodec.isBase64(""),
        )
    }

    @Test
    fun isBase64_whitespaceOnly_returnsFalse() {
        assertFalse(
            "Whitespace-only string must not be treated as base64",
            Base64ImageCodec.isBase64("   "),
        )
    }

    // ── Constant sanity check ───────────────────────────────────────────────

    @Test
    fun maxDocBase64Bytes_is921600() {
        assertEquals(921_600, Base64ImageCodec.MAX_DOC_BASE64_BYTES)
    }
}
