package com.dew.edward.dewchat.model


/**
 *   Created by Edward on 9/4/2018.
 */
data class UserData(val username: String = "",
                    val userFullName: String = "",
                    val gender: String = "",
                    val profileImageUrl: String = "",
                    val country: String = "",
                    val DOB: String = "none",
                    val relationship: String = "none",
                    val status: String = "none",
                    val registrationTokens: MutableList<String> = mutableListOf()
)