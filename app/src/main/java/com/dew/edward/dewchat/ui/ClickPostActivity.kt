package com.dew.edward.dewchat.ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.EditText
import com.dew.edward.dewchat.MainActivity
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.DbUtil.Companion.currentUserId
import com.dew.edward.dewchat.util.DbUtil.Companion.postsRef
import com.dew.edward.dewchat.util.toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_click_post.*
import kotlinx.android.synthetic.main.post_layout.view.*

class ClickPostActivity : AppCompatActivity() {
    private val tag = this::class.java.simpleName

    private lateinit var postRef: DocumentReference
    private var postImageUrl: String? = null
    private var postText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_click_post)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val postId: String = intent.extras.getString("PostId")
        Log.d(tag, "postId = $postId")

        postRef = DbUtil.postsRef.document(postId)
        postRef.addSnapshotListener(this){documentSnapshot, exception ->
            if (exception != null){
                Log.d(tag, "Error: Adding Listener failed: ${exception.message}")
                return@addSnapshotListener
            }
            if (documentSnapshot != null){
                postText = documentSnapshot.getString("postText")
                postImageUrl = documentSnapshot.getString("postImage")
                val postOwnerId = documentSnapshot.getString("userId")
                val currentUserId = DbUtil.currentUserId

                textViewPostText.text = postText
                if (postImageUrl != null && postImageUrl!!.isNotEmpty()){
                    Picasso.get().load(postImageUrl).into(imageViewPost)
                } else {
                    imageViewPost.visibility = View.GONE
                }

                if (currentUserId == postOwnerId){  // only post owner can delete and edit his post.
                    buttonDeletePost.visibility = View.VISIBLE
                    buttonEditPost.visibility = View.VISIBLE
                } else {
                    buttonDeletePost.visibility = View.INVISIBLE
                    buttonEditPost.visibility = View.INVISIBLE
                }
            }
        }

        buttonDeletePost.setOnClickListener {
            deleteCurrentPost()
        }

        buttonEditPost.setOnClickListener {
            editCurrentPost()
        }
    }

    private fun deleteCurrentPost() {
        if (postImageUrl != null) {
            val imageStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(postImageUrl!!)
            imageStorageRef.delete()
                    .addOnSuccessListener {
                        Log.d(tag, "image in Storage has been deleted!")
                        postRef.delete()
                                .addOnSuccessListener {
                                    "Post has been deleted".toast(this@ClickPostActivity)
                                    sendUserToMainActivity()
                                }
                                .addOnFailureListener {
                                    "Deleting the post failed".toast(this@ClickPostActivity)
                                }
                    }
                    .addOnFailureListener {
                        "Deleting image in Storage failed.".toast(this@ClickPostActivity)
                    }
        }
    }

    private fun sendUserToMainActivity() {
        val intent = Intent(this@ClickPostActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun editCurrentPost() {
        val alertDialogBuilder = AlertDialog.Builder(this@ClickPostActivity)

        val inputField = EditText(this@ClickPostActivity)
        with(inputField){
            if (postText != null){
                setText(postText)
                setTextColor(Color.WHITE)
            }
        }

        with(alertDialogBuilder){
            setTitle("Edit Post")
            setView(inputField)
            setPositiveButton("Update"){dialogInterface: DialogInterface?, i: Int ->
                val inputText = inputField.text.toString()
                postRef.set(mapOf("postText" to inputText), SetOptions.merge())
                        .addOnSuccessListener {
                            "Post updated successfully".toast(this@ClickPostActivity)

                        }
            }
            setNegativeButton("Cancel"){dialogInterface, i ->
                dialogInterface.cancel()
            }
        }

        val dialog = alertDialogBuilder.create()
        dialog.show()
        dialog.window.setBackgroundDrawableResource(android.R.color.holo_green_dark)
    }
}
