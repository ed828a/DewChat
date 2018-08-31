package com.dew.edward.dewchat.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.dew.edward.dewchat.repository.FireRepository


/**
 *   Created by Edward on 8/31/2018.
 */
class DewViewModelFactory(private val repository: FireRepository): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> LoginViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel Class.")
        }
    }
}