package com.dew.edward.dewchat.model


/**
 *   Created by Edward on 9/11/2018.
 */
data class Message(
        val message: String = "",
        val type: String = "",
        val from: String = "",
        val date: String = "",
        val time: String = "",
        val index: Long = 0
)