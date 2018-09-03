package com.dew.edward.dewchat.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.dew.edward.dewchat.MainActivity
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.AppUtil
import com.dew.edward.dewchat.util.RC_IMAGE_PICK
import com.dew.edward.dewchat.util.toast
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_setup.*

class SetupActivity : AppCompatActivity() {
    private val tag: String = this.javaClass.simpleName

    private lateinit var loadingBar: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        loadingBar = ProgressDialog(this)

        setupUserProfileImage.setOnClickListener {
            saveInputFields()
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, RC_IMAGE_PICK)
        }

        setupSaveButton.setOnClickListener {
            saveUserAccountInformation()
        }

        DbUtil.usersRef.document(DbUtil.mAuth.currentUser?.uid!!)
                .addSnapshotListener { documentSnapshot,
                                       firebaseFirestoreException ->
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        if (documentSnapshot.contains("profileImageUrl")) {
                            val imageUrl = documentSnapshot.get("profileImageUrl").toString()
                            Picasso.get().load(imageUrl).into(setupUserProfileImage)
                        }
                        if (documentSnapshot.contains("username")) {
                            val username = documentSnapshot.get("username").toString()
                            setupUserName.setText(username)
                        }
                        if (documentSnapshot.contains("userFullName")) {
                            val fullname = documentSnapshot.get("userFullName").toString()
                            setupUserFullName.setText(fullname)
                        }
                        if (documentSnapshot.contains("country")) {
                            val country = documentSnapshot.get("country").toString()
                            setupUserCountry.setText(country)
                        }
                    }
                }
    }

    private fun saveInputFields() {
        val inputFieldsMap = mutableMapOf<String, Any>()
        val username = setupUserName.text.toString().trim()
        if (username.isNotEmpty()) {
            inputFieldsMap["username"] = username
        }

        val userFullName = setupUserFullName.text.toString().trim()
        if (userFullName.isNotEmpty()) {
            inputFieldsMap["userFullName"] = userFullName
        }

        val country = setupUserCountry.text.toString().trim()
        if (country.isNotEmpty()) {
            inputFieldsMap["country"] = country
        }

        DbUtil.usersRef.document(DbUtil.mAuth.currentUser?.uid!!)
                .set(inputFieldsMap, SetOptions.merge())
    }

    private fun saveUserAccountInformation() {
        val username = setupUserName.text.toString().trim()
        if (username.isEmpty()) {
            "please input your name...".toast(this@SetupActivity)
            return
        }

        val userFullName = setupUserFullName.text.toString().trim()
        if (userFullName.isEmpty()) {
            "please input your full name...".toast(this@SetupActivity)
            return
        }

        val country = setupUserCountry.text.toString().trim()
        if (country.isEmpty()) {
            "please input your country...".toast(this@SetupActivity)
            return
        }

        AppUtil.showProgressDialog(loadingBar, "Saving information...",
                "your account is being created...")

        val userInfoMap = mutableMapOf<String, Any>(
                "username" to username,
                "userFullName" to userFullName,
                "country" to country,
                "gender" to "none",
                "DOB" to "none",
                "relationship" to "none",
                "status" to "none"
        )
        DbUtil.usersRef.document(DbUtil.mAuth.currentUser?.uid!!)
                .set(userInfoMap, SetOptions.merge())
                .addOnCompleteListener { task ->
                    AppUtil.dismissProgressDialog(loadingBar)
                    if (task.isSuccessful) {
                        sendUserToMainActivity()
                        "Your account has been created successfully.".toast(this)
                    } else {
                        "Error: ${task.exception?.message}".toast(this)
                    }
                }

    }

    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@SetupActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri: Uri = data.data
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
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
                                "Profile image has stored successfully".toast(this@SetupActivity)
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
                                                "Profile Image Url was stored successfully...".toast(this)
                                                Log.d(tag, "Profile Image Url was stored successfully. imageUrl=$downloadUrl")
                                                val tem =  SetupActivity::class.java
                                                startActivity(Intent(this, SetupActivity::class.java))
                                            } else {
                                                "Storing Profile Image Url failed: ${task.exception?.message}"
                                            }
                                        }
                            } else {
                                AppUtil.dismissProgressDialog(loadingBar)
                                "Error: ${task.exception?.message}".toast(this)

                            }
                            AppUtil.dismissProgressDialog(loadingBar)
                        }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                "Error: ${result.error.message}".toast(this)
            }
        }
    }
}
