package com.dew.edward.dewchat.di

import com.dew.edward.dewchat.ui.LoginActivity
import dagger.Component
import javax.inject.Singleton


/**
 *   Created by Edward on 8/31/2018.
 */

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(target: LoginActivity)
}