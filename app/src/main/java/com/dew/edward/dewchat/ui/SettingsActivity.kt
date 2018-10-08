package com.dew.edward.dewchat.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.dew.edward.dewchat.MainActivity
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.model.UserData
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.RC_IMAGE_PICK
import com.dew.edward.dewchat.util.toast
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    private val tag = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        DbUtil.usersRef.document(DbUtil.currentUserId!!)
                .addSnapshotListener(this) { documentSnapshot, exception ->
                    if (exception != null) {
                        Log.d(tag, "Adding listener failed: ${exception.message}")
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(UserData::class.java)
                        user?.let {
                            Log.d(tag, "user = $it")
                            if (it.profileImageUrl.isNotEmpty()) {
                                Picasso.get().load(it.profileImageUrl).into(settingsProfileImage)
                            }
                            settingsCountry.setText(it.country)
                            settingsDOB.setText(it.DOB)
                            settingsGender.setText(it.gender)
                            settingsProfileFullName.setText(it.userFullName)
                            settingsRelationshipStatus.setText(it.relationship)
                            settingsStatus.setText(it.status)
                            settingsUsername.setText(it.username)
                        }
                    }
                }

        updateAccountSettingsButton.setOnClickListener {
            updateAccountInfo()
        }

        settingsProfileImage.setOnClickListener {
            val intent = Intent()
            with(intent) {
                action = Intent.ACTION_GET_CONTENT
                type = "image/*"
            }
            startActivityForResult(intent, RC_IMAGE_PICK)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                settingsProgressBar.visibility = View.VISIBLE

                val resultUri = result.uri
                val filePath = DbUtil.profileImageStorageRef.child("${DbUtil.currentUserId}.jpg")
                val uploadTask = filePath.putFile(resultUri)
                uploadTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                throw task.exception as Throwable
                            } else {
                                "Profile Image successfully stored.".toast(this)
                                return@continueWithTask filePath.downloadUrl
                            }
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val downloadUrl = task.result.toString()
                                DbUtil.usersRef.document(DbUtil.currentUserId!!)
//                                        .set(mapOf("profileImageUrl" to downloadUrl), SetOptions.merge())
                                        .update(mapOf("profileImageUrl" to downloadUrl))
                                        .addOnCompleteListener { nestedTask ->
                                            settingsProgressBar.visibility = View.GONE
                                            if (nestedTask.isSuccessful) {
                                                "Profile image stored in firestore successfully...".toast(this)
                                                Picasso.get().load(downloadUrl).into(settingsProfileImage)
//                                                startActivity(Intent(this, SettingsActivity::class.java))
                                            } else {
                                                "Storing Image failed, try again...".toast(this)
                                                Log.d(tag, "Error: ${task.exception.toString()}")
                                            }
                                        }
                            } else {
                                settingsProgressBar.visibility = View.GONE
                                "Storing Image failed, try again...".toast(this)
                                Log.d(tag, "Error: ${task.exception.toString()}")
                            }
                        }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                "Picking picture failed: ${result.error}".toast(this)
                Log.d(tag, "Picking Image failed: ${result.error}")
            }
        }
    }

    private fun updateAccountInfo() {
        when {
            settingsUsername.text.isEmpty() -> "Please write your username...".toast(this)
            settingsProfileFullName.text.isEmpty() -> "Please write your full name...".toast(this)
            settingsCountry.text.isEmpty() -> "Please write your country...".toast(this)
            settingsGender.text.isEmpty() -> "Please write your gender...".toast(this)
            settingsDOB.text.isEmpty() -> "Please write your date of birth...".toast(this)
            settingsRelationshipStatus.text.isEmpty() -> "Please write your relationship...".toast(this)
            settingsStatus.text.isEmpty() -> "Please write your status...".toast(this)
            else -> {
                settingsProgressBar.visibility = View.VISIBLE

                val userMap = mapOf<String, Any>(
                        "username" to settingsUsername.text.toString(),
                        "userFullName" to settingsProfileFullName.text.toString(),
                        "country" to settingsCountry.text.toString(),
                        "DOB" to settingsDOB.text.toString(),
                        "gender" to settingsGender.text.toString(),
                        "status" to settingsStatus.text.toString(),
                        "relationship" to settingsRelationshipStatus.text.toString()
                )

                DbUtil.usersRef.document(DbUtil.currentUserId!!)
//                        .set(userMap, SetOptions.merge())
                        .update(userMap)
                        .addOnCompleteListener { task ->
                            settingsProgressBar.visibility = View.GONE
                            if (task.isSuccessful) {
                                "Account settings updated successfully....".toast(this)
                                sendUserToMainActivity()
                            } else {
                                "Updating failed, please try again...".toast(this)
                                Log.d(tag, "Error: ${task.exception.toString()}")
                            }
                        }
            }
        }
    }

    private fun sendUserToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
