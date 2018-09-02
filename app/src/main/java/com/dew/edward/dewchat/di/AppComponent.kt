package com.dew.edward.dewchat.di

import com.dew.edward.dewchat.MainActivity
import com.dew.edward.dewchat.ui.LoginActivity
import com.dew.edward.dewchat.ui.RegisterActivity
import com.dew.edward.dewchat.ui.ResetPasswordActivity
import com.dew.edward.dewchat.ui.SetupActivity
import dagger.Component
import javax.inject.Singleton


/**
 *   Created by Edward on 8/31/2018.
 */

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(target: LoginActivity)
    fun inject(target: RegisterActivity)
    fun inject(target: ResetPasswordActivity)
    fun inject(target: SetupActivity)
    fun inject(target: MainActivity)

}