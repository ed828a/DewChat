package com.dew.edward.dewchat.di

import android.app.Application
import com.squareup.leakcanary.LeakCanary


/**
 *   Created by Edward on 8/31/2018.
 */
class DewChatApp: Application() {

    companion object {
        // control email verification, controlled by owner only
        var isNeedEmailVerification: Boolean = true
    }



    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)){
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        // Normal app init code here ...


    }
}