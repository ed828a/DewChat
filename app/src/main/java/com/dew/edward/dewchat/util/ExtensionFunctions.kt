package com.dew.edward.dewchat.util

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.dew.edward.dewchat.R


/**
 *   Created by Edward on 9/3/2018.
 */
fun String.toast(context: Context){
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}


fun RecyclerView.addDivider(){
    // add line-divider
    val horizontalDividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
    val horizontalDivider = ContextCompat.getDrawable(context, R.drawable.line_divider)
    horizontalDivider?.let {
        horizontalDividerItemDecoration.setDrawable(it)
        this.addItemDecoration(horizontalDividerItemDecoration)
    }
}