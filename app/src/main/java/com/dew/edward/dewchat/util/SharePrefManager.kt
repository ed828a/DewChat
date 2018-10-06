package com.dew.edward.dewchat.util

import android.content.Context
import android.util.Log


/**
 *   Created by Edward on 9/27/2018.
 */
class SharePrefManager(val context: Context) {
    private val TAG = this.javaClass.simpleName

    @Volatile
    private var mInstance: SharePrefManager? = null

    fun getInstance(context: Context): SharePrefManager =
            mInstance ?: synchronized(SharePrefManager::class.java) {
                mInstance ?: SharePrefManager(context).also {
                    mInstance = it
                }
            }

    fun storeToken(token: String) {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, token)
                .apply()

    }

    fun getToken(): String? {
        val sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
        Log.d(TAG, "getToken, token=$token")
        return token
    }
}