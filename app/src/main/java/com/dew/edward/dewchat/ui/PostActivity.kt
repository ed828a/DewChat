package com.dew.edward.dewchat.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.dew.edward.dewchat.MainActivity
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.R.id.selectPostImage
import com.dew.edward.dewchat.model.PostData
import com.dew.edward.dewchat.util.AppUtil
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.RC_IMAGE_PICK
import com.dew.edward.dewchat.util.toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_post.*

class PostActivity : AppCompatActivity() {
    private val tag = this::class.java.simpleName

    private var imageUri: Uri? = null
    //    private var postText: String = ""
    private var postRandomName: String = ""
    private var saveCurrentDate: String = ""
    private var saveCurrentTime: String = ""
    private var downloadUrl: String = ""

    private var countPosts = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.title = "Post"
        }

        selectPostImage.setOnClickListener {
            openGallery()
        }

        publishPostButton.setOnClickListener {
            publishPost()
        }
    }

    private fun publishPost() {
        val postText = postDescription.text.toString().trim()
        val image = imageUri
        if (image == null && postText.isEmpty()) {
            "Please select a post image or write something ...".toast(this)
        } else {
            postProgressBar.visibility = View.VISIBLE
            saveCurrentDate = AppUtil.currentDate
            saveCurrentTime = AppUtil.currentTime
            postRandomName = saveCurrentDate + AppUtil.currentAccurateTime

            if (image != null) {
                storingPostToFirebaseStorage(image, postText)
            } else {
                savePostInfoToFirestore("", postText)
            }
        }

    }

    private fun storingPostToFirebaseStorage(imageUri: Uri, postText: String) {

        val filePath = DbUtil.postImageStorageRef
                .child("${imageUri.lastPathSegment}$postRandomName.jpg")

        filePath.putFile(imageUri)
                .continueWithTask { task ->
                    if (task.isSuccessful) {
                        "Post Image stored successfully.".toast(this)
                        return@continueWithTask filePath.downloadUrl
                    } else {
                        postProgressBar.visibility = View.GONE
                        throw task.exception as Throwable
                    }
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        downloadUrl = task.result.toString()
                        savePostInfoToFirestore(downloadUrl, postText)
                    } else {
                        Log.d(tag, "Error: ${task.exception.toString()}")
                        "Server is busy, try again.".toast(this)
                        postProgressBar.visibility = View.GONE
                    }
                }

    }

    private fun savePostInfoToFirestore(postImageUrl: String, postText: String) {
        val currentUserId = DbUtil.currentUserId
        currentUserId?.let {
            DbUtil.usersRef.document(it).addSnapshotListener(this) { documentSnapshot,
                                                                     exception ->
                if (exception != null) {
                    Log.d(tag, "Exception: ${exception.message}")
                    "Server is busy, try again".toast(this)
                    postProgressBar.visibility = View.GONE
                    return@addSnapshotListener
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val userFullName = documentSnapshot.get("userFullName").toString()
                    val userProfileImage = documentSnapshot.get("profileImageUrl").toString()
                    // index by milliseconds that have elapsed since January 1st, 1970 at midnight in the GMT time zone.
                    val postIndex = AppUtil.currentTimeInMillis
                    val postMap = PostData(
                            DbUtil.currentUserId!!,
                            userFullName,
                            userProfileImage,
                            postText,
                            postImageUrl,
                            countPosts,
                            saveCurrentDate,
                            saveCurrentTime,
                            postIndex
                    )

                    DbUtil.postsRef.document("$currentUserId$postRandomName")
                            .set(postMap, SetOptions.merge())
                            .addOnCompleteListener { task ->
                                postProgressBar.visibility = View.GONE
                                if (task.isSuccessful) {
                                    sendUserToMainActivity();
                                    "New post is published successfully.".toast(this)
                                } else {
                                    Log.d(tag, "Error: ${task.exception.toString()}")
                                    "Server is busy, try again.".toast(this)
                                }
                            }
                }
            }
        }

    }

    private fun sendUserToMainActivity() {
        startActivity(Intent(this@PostActivity, MainActivity::class.java))
        if (!this@PostActivity.isFinishing) {
            finish()
        }
    }

    private fun openGallery() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(Intent.createChooser(intent, "Select Image"), RC_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(18, 9)
                    .start(this)

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null){
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK){
                imageUri = result.uri
                selectPostImage.setImageURI(imageUri)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
