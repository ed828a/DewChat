package com.dew.edward.dewchat.viewmodel

import android.arch.lifecycle.ViewModel
import com.dew.edward.dewchat.repository.FireRepository


/**
 *   Created by Edward on 8/31/2018.
 */
class LoginViewModel(val fireRepository: FireRepository): ViewModel(){

    fun signOut(){
        fireRepository.mAuth.signOut()
    }
}