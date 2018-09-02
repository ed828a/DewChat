package com.dew.edward.dewchat.util

import android.content.Context
import android.widget.Toast


/**
 *   Created by Edward on 9/1/2018.
 */

fun String.toast(context: Context){
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}