package com.dew.edward.dewchat.di

import android.content.Context
import com.dew.edward.dewchat.repository.FireRepository
import com.dew.edward.dewchat.viewmodel.DewViewModelFactory
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 *   Created by Edward on 8/31/2018.
 */

@Module
class AppModule (private val app: DewChatApp){

    @Singleton
    @Provides
    fun provideContext(): Context = app


    @Singleton
    @Provides
    fun provideFireRepository(): FireRepository {
        return FireRepository()
    }

    @Singleton
    @Provides
    fun provideViewModelFactory(repository: FireRepository): DewViewModelFactory{
            return DewViewModelFactory(repository)
    }
}