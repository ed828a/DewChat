package com.dew.edward.dewchat.model


/**
 *   Created by Edward on 9/1/2018.
 */
data class User(val username: String = "",
                val userFullName: String = "",
                val gender: String = "",
                val profileImageUrl: String = "",
                val country: String = "",
                val DOB: String = "none",
                val relationship: String = "none",
                val status: String = "none"
                )