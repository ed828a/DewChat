package com.dew.edward.dewchat.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.adapter.MessageAdapter
import com.dew.edward.dewchat.model.Message
import com.dew.edward.dewchat.util.*
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_custom_appbar.*

class ChatActivity : AppCompatActivity() {

    private val tag = this::class.java.simpleName
    private lateinit var messageReceiverId: String
    private lateinit var messageReceiverName: String
    private val messageSenderId = DbUtil.currentUserId!!
    private lateinit var adapter: MessageAdapter
    private var sender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        setSupportActionBar(chatCustomToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        messageReceiverId = intent.getStringExtra(KEY_EXTRA_LISTED_USER_ID) ?: ""
        messageReceiverName = intent.getStringExtra(KEY_EXTRA_USER_NAME) ?: ""
        if (messageReceiverId.isEmpty() || messageReceiverName.isEmpty()){
            Log.d(tag, "Error: empty receiver")
        } else {
            displayReceiverInfo(messageReceiverId, messageReceiverName)
            initializeMessageList(messageReceiverId, messageSenderId)
            DbUtil.messagesRef.document(messageSenderId).collection(messageReceiverId)
                    .addSnapshotListener(this){querySnapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                        if (exception != null){
                            Log.d(tag, "Adding listener failed: ${exception.message}")
                            return@addSnapshotListener
                        }
                        if (querySnapshot != null && querySnapshot.documentChanges.size > 0){
                            Log.d(tag, "adapter.itemCount = ${adapter.itemCount}")
                        }
                    }
        }
        DbUtil.currentUserId?.let {currentUserId ->
            DbUtil.usersRef.document(currentUserId).get().addOnSuccessListener {
                sender = it.getString("userFullName")
            }
        }

        chatMessageSendButton.setOnClickListener {
            sendTextMessage(messageReceiverId, messageSenderId)
        }

        chatMessageImageSelectButton.setOnClickListener {
            sendImageMessage()
        }
    }

    private fun sendImageMessage() {
        val intent = Intent()
        with(intent) {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        startActivityForResult(intent, RC_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var imageUri: Uri? = null
        if (requestCode == RC_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null){
            imageUri = data.data
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(16, 9)
                    .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && data != null){
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK){
                imageUri = result.uri
                val fileName = AppUtil.currentTimeInMillis.toString()
                val filePath = DbUtil.messageImageStorageRef
                        .child("$fileName.jpg")
                filePath.putFile(imageUri)
                        .continueWithTask { task ->
                            return@continueWithTask if (task.isSuccessful){
                                "Message image stored successfully".toast(this)
                                filePath.downloadUrl
                            } else {
                                throw task.exception as Throwable
                            }
                        }
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                val downloadUrl = task.result.toString()
                                val message = Message(
                                        message = downloadUrl,
                                        type = "image",
                                        from = messageSenderId,
                                        date = AppUtil.currentDate,
                                        time = AppUtil.currentTime,
                                        index = AppUtil.currentTimeInMillis,
                                        senderName = sender ?: ""
                                )
                                DbUtil.currentUserId?.let {
                                    DbUtil.messagesRef
                                            .document(messageSenderId)
                                            .collection(messageReceiverId)
                                            .add(message)
                                            .addOnSuccessListener {documentReference ->
                                                val documentId = documentReference.id
                                                DbUtil.messagesRef
                                                        .document(messageReceiverId)
                                                        .collection(messageSenderId)
                                                        .document(documentId)
                                                        .set(message)
                                                        .addOnSuccessListener {
                                                            "message sent...".toast(this)
                                                            chatMessageList.scrollToPosition(adapter.itemCount - 1)
                                                        }
                                                        .addOnFailureListener {exception ->
                                                            "message sending failed, please try again.".toast(this)
                                                            Log.d(tag, "Error: ${exception.message}")
                                                        }
                                            }
                                            .addOnFailureListener {exception ->
                                                "message sending failed, please try again.".toast(this)
                                                Log.d(tag, "Error: ${exception.message}")
                                            }
                                }
                            } else {
                                Log.d(tag, "Error: ${task.exception.toString()}")
                                "Server is busy, try again.".toast(this)
                            }
                        }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun sendTextMessage(messageReceiverId: String, messageSenderId: String) {
        val messageText = chatMessageTextInput.text.toString().trim()
        if (messageText.isEmpty()){
            "Please input chat text...".toast(this)
            return
        } 

        val message = Message(
                message = messageText,
                type = "text",
                from = messageSenderId,
                date = AppUtil.currentDate,
                time = AppUtil.currentTime,
                index = AppUtil.currentTimeInMillis,
                senderName = sender ?: ""
                )
        DbUtil.messagesRef.document(messageSenderId)
                .collection(messageReceiverId)
                .add(message)
                .addOnSuccessListener { documentReference ->
                    val documentId = documentReference.id
                    DbUtil.messagesRef
                            .document(messageReceiverId)
                            .collection(messageSenderId)
                            .document(documentId)
                            .set(message)
                            .addOnSuccessListener {
                                "message sent...".toast(this)
                                chatMessageTextInput.text.clear()
                                chatMessageList.scrollToPosition(adapter.itemCount - 1)
                            }
                            .addOnFailureListener { exception ->
                                "message sending failed, please try again.".toast(this)
                                Log.d(tag, "Error: ${exception.message}")
                            }
                }
                .addOnFailureListener {exception ->
                    "message sending failed, please try again.".toast(this)
                    Log.d(tag, "Error: ${exception.message}")
                }
    }



    private fun initializeMessageList(messageReceiverId: String, messageSenderId: String) {
        chatMessageList.layoutManager = LinearLayoutManager(this)
        chatMessageList.setHasFixedSize(true)
        val query = DbUtil.messagesRef
                .document(messageSenderId)
                .collection(messageReceiverId)
                .orderBy("index")

        val options = FirestoreRecyclerOptions.Builder<Message>()
                .setLifecycleOwner(this)
                .setQuery(query,Message::class.java)
                .build()
        adapter = MessageAdapter(options)
        chatMessageList.adapter = adapter
    }

    private fun displayReceiverInfo(messageReceiverId: String, messageReceiverName: String) {
        customProfileName.text = messageReceiverName
        DbUtil.usersRef.document(messageReceiverId)
                .get()
                .addOnSuccessListener {documentSnapshot ->
                    val profileImage = documentSnapshot.getString("profileImageUrl")
                    profileImage?.let {
                        Picasso.get().load(profileImage).into(customProfileImage)
                    }
                }
    }
}
