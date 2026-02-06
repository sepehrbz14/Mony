package com.tolou.mony.data

import android.content.Context

class SessionStorage(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun fetchToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clear() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "mony_session"
        private const val KEY_TOKEN = "auth_token"
    }
}
