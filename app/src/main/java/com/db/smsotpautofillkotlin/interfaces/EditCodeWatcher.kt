package com.db.smsotpautofillkotlin.interfaces

/**
 * Created by DB on 03-01-2018.
 */
interface EditCodeWatcher {
    fun onCodeChanged(code: String)
}