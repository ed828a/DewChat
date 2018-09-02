package com.dew.edward.dewchat.util

import android.app.ProgressDialog


/**
 *   Created by Edward on 9/1/2018.
 */
class AppUtil {

    companion object {

        fun showProgressDialog(loadingBar: ProgressDialog, title: String, message: String){
            with(loadingBar){
                setTitle("Google Sign In")
                setMessage(message)
                setCanceledOnTouchOutside(true)
                show()
            }
        }

        fun dismissProgressDialog(loadingBar: ProgressDialog){
            if (loadingBar.isShowing)
                loadingBar.dismiss()
        }

    }

}