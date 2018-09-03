package com.dew.edward.dewchat.util

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import com.dew.edward.dewchat.ui.SetupActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage


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


        fun storeImage(data: Intent?, resultCode: Int, loadingBar: ProgressDialog,
                       context: Context, onComplete: () -> Unit){
            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                AppUtil.showProgressDialog(loadingBar, "Profile Image",
                        "Your profile image is updating...")

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
                                AppUtil.dismissProgressDialog(loadingBar)
                                throw task.exception as Throwable
                            }
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUrl = task.result.toString()
                                val userMap = mutableMapOf<String, Any>("profileImageUrl" to downloadUrl)
                                DbUtil.usersRef.document(DbUtil.mAuth.currentUser?.uid!!)
                                        .set(userMap, SetOptions.merge())
                                        .addOnCompleteListener { task ->
                                            AppUtil.dismissProgressDialog(loadingBar)
                                            if (task.isSuccessful) {
                                                "Profile Image Url was stored successfully...".toast(context)
                                                Log.d("storeImage", "Profile Image Url was stored successfully. imageUrl=$downloadUrl")
                                                onComplete()
                                            } else {
                                                "Storing Profile Image Url failed: ${task.exception?.message}"
                                            }
                                        }
                            } else {
                                AppUtil.dismissProgressDialog(loadingBar)
                                "Error: ${task.exception?.message}".toast(context)

                            }
                            AppUtil.dismissProgressDialog(loadingBar)
                        }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                "Error: ${result.error.message}".toast(context)
            }
        }
    }

}