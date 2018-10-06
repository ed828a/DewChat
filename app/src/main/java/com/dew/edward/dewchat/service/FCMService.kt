package com.dew.edward.dewchat.service

import android.content.Context
import android.util.Log
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.KEY_ACCESS_TOKEN
import com.dew.edward.dewchat.util.SHARED_PREF_NAME
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


/**
 *   Created by Edward on 9/27/2018.
 */
class FCMService : FirebaseMessagingService() {
    private val TAG = this.javaClass.simpleName

    var registrationToken: String? = null

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: $token")
        registrationToken = token

        token?.let{storeTokenToSharedPreferences(token)}

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {

        if (DbUtil.currentUser != null && token != null) {
            addTokenToFirestore(token)

        }
    }

    private fun storeTokenToSharedPreferences(token: String) {

        val sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, token)
                .apply()
        Log.d(TAG, "storeTokenToSharedPreferences toke=$token")
        getToken()
    }

    fun getToken(): String? {
         if (registrationToken != null) {
             Log.d(TAG, "getToken registrationToken=$registrationToken")
            return registrationToken
        }
        else {
            val sharedPreferences = applicationContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)

            val token = sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
            Log.d(TAG, "getToken token=$token")
            return token
        }
    }

    companion object {
        fun addTokenToFirestore(newToken: String?) {
            if (newToken == null) {
                throw NullPointerException("FCM token is null.")
            }

            DbUtil.getFCMRegistrationTokens { tokens ->
                if (tokens.contains(newToken))
                    return@getFCMRegistrationTokens
                else {
                    tokens.add(newToken)
                    DbUtil.setFCMRegistrationTokens(tokens)
                }
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage!!.from!!)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob()
//            } else {
//                // Handle message within 10 seconds
//                handleNow()
//            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
}