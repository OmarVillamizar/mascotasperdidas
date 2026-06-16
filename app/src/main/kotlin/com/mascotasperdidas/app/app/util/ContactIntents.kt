package com.mascotasperdidas.app.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens a WhatsApp chat with [phone] via the wa.me deep link.
 * wa.me expects the number with country code and no '+' or separators.
 * Falls back to the browser if WhatsApp is not installed.
 */
fun launchWhatsApp(context: Context, phone: String) {
    val number = phone.filter { it.isDigit() }
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

/** Opens the phone dialer pre-filled with [phone] (no CALL_PHONE permission needed). */
fun launchDialer(context: Context, phone: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}
