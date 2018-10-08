package com.dew.edward.dewchat.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import java.text.SimpleDateFormat
import java.util.*


/**
 *   Created by Edward on 9/3/2018.
 */
class AppUtil {


    companion object {

        val currentDate: String
            get() = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    .format(Calendar.getInstance().time)

        val currentTime: String
            get() = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Calendar.getInstance().time)

        val currentAccurateTime: String
            get() = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
                    .format(Calendar.getInstance().time)

        val currentTimeInMillis: Long
            get() = System.currentTimeMillis()



        // need to adapt to progressbar
        fun showProgressBar(loadingBar: ProgressBar) {
            loadingBar.visibility = View.VISIBLE
        }

        fun dismissProgressBar(loadingBar: ProgressBar) {
            loadingBar.visibility = View.GONE
        }


        fun storeImage(data: Intent?, resultCode: Int, loadingBar: ProgressBar,
                       context: Context, onComplete: () -> Unit) {
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                AppUtil.showProgressBar(loadingBar)

                val imageFilePath: StorageReference = DbUtil.profileImageStorageRef
                        .child("${DbUtil.mAuth.currentUser?.uid!!}.jpg")
                // save profile image in FirebaseStorage

                val uploadTask: UploadTask = imageFilePath.putFile(result.uri)
                val urlTask: Task<Uri> = uploadTask
                        .continueWithTask { task ->
                            return@continueWithTask if (task.isSuccessful) {
                                "Profile image has stored successfully".toast(context)
                                imageFilePath.downloadUrl
                            } else {
                                AppUtil.dismissProgressBar(loadingBar)
                                throw task.exception as Throwable
                            }
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUrl = task.result.toString()
                                val userMap = mutableMapOf<String, Any>("profileImageUrl" to downloadUrl)
                                DbUtil.usersRef.document(DbUtil.mAuth.currentUser?.uid!!)
                                        .set(userMap, SetOptions.merge())
                                        .addOnCompleteListener { nestedTask ->
                                            AppUtil.dismissProgressBar(loadingBar)
                                            if (nestedTask.isSuccessful) {
                                                "Profile Image Url was stored successfully...".toast(context)
                                                Log.d("storeImage", "Profile Image Url was stored successfully. imageUrl=$downloadUrl")
                                                onComplete()
                                            } else {
                                                "Storing Profile Image Url failed: ${nestedTask.exception?.message}"
                                            }
                                        }
                            } else {
                                AppUtil.dismissProgressBar(loadingBar)
                                "Error: ${task.exception?.message}".toast(context)

                            }
                            AppUtil.dismissProgressBar(loadingBar)
                        }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                "Error: ${result.error.message}".toast(context)
            }
        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

    }

}
//
//fun main(args: Array<String>) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        val current = LocalDateTime.now()
//        println("current date and time is $current")
//
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
//        val formatted = current.format(formatter)
//
//        println("Current Date and Time is: $formatted")
//
//    } else {
//        val date = Date()
//        var strDateFormat = "hh:mm:ss a"
//        var dateFormat = SimpleDateFormat(strDateFormat)
//        var formattedDate = dateFormat.format(date)
//        println("Current time of the day using Date - 12 hour format: $formattedDate")
//        strDateFormat = "dd-MM-yyyy"
//        dateFormat = SimpleDateFormat(strDateFormat)
//        formattedDate = dateFormat.format(date)
//        println("Current time of the day using Date - 12 hour format: $formattedDate")
//
//        println("Current date = ${AppUtil.currentDate}")
//        println("current time = ${AppUtil.currentTime}")
//    }
//}