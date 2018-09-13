package com.dew.edward.dewchat.model

import com.dew.edward.dewchat.util.VALUE_NOT_FRIEND


/**
 *   Created by Edward on 9/10/2018.
 */
data class Friend(
        val friendship: String = VALUE_NOT_FRIEND,
        val date: String = "")