package com.db.smsotpautofillkotlin.interfaces

/**
 * Created by DB on 03-01-2018.
 */
interface SmsListener {
    fun messageReceived(messageText: String)
}