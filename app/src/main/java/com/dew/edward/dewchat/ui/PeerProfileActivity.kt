package com.dew.edward.dewchat.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.model.UserData
import com.dew.edward.dewchat.util.*
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_peer_profile.*

class PeerProfileActivity : AppCompatActivity() {

    private val tag = this::class.java.simpleName
    private lateinit var receiverUserId: String
    private lateinit var senderUserId: String
    private lateinit var receiverfriendsRef: CollectionReference
    private lateinit var senderFriendsRef: CollectionReference
    private var currentFriendshipState = "not_friend"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peer_profile)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        receiverUserId = intent.getStringExtra("FriendListedUserId")
        senderUserId = DbUtil.currentUserId ?: ""
        initializeButtons(senderUserId, receiverUserId)

        receiverfriendsRef = DbUtil.usersRef.document(receiverUserId).collection("Friends")
        senderFriendsRef = DbUtil.usersRef.document(senderUserId).collection("Friends")

        DbUtil.usersRef.document(receiverUserId)
                .addSnapshotListener(this){documentSnapshot: DocumentSnapshot?, exception: FirebaseFirestoreException? ->
                    if (exception != null){
                        Log.d(tag, "Adding listener failed: ${exception.message}")
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()){
                        val userData = documentSnapshot.toObject(UserData::class.java)
                        userData?.let {
                            if (it.profileImageUrl.isNotEmpty()){
                                Picasso.get().load(it.profileImageUrl).into(peerProfileImagePicture)
                            }
                            peerProfileCountry.text = it.country
                            peerProfileDOB.text = it.DOB
                            peerProfileGender.text = it.gender
                            peerProfileRelationship.text = it.relationship
                            peerProfileStatus.text = it.status
                            peerProfileUserFullName.text = it.userFullName
                        }
                    }
                }

        maintenanceOfButtons()
    }

    private fun maintenanceOfButtons() {
        senderFriendsRef.document(receiverUserId)
                .addSnapshotListener(this){documentSnapshot, exception ->
                    if (exception != null){
                        Log.d(tag, "Adding listener failed: ${exception.message}")
                        return@addSnapshotListener
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()){
                        val friendshipStatus = documentSnapshot.getString(KEY_FRIENDSHIP) ?: "error"
                        when (friendshipStatus){
                            VALUE_BECOME_FRIEND -> {
                                currentFriendshipState = VALUE_BECOME_FRIEND
                                peerSendFriendRequestButton.text = getString(R.string.unfriend_this_person)
                                peerDeclineFriendRequestButton.visibility = View.INVISIBLE
                                peerDeclineFriendRequestButton.isEnabled = false
                            }

                            VALUE_FRIEND_REQUEST_SENT -> {
                                currentFriendshipState = VALUE_FRIEND_REQUEST_SENT
                                peerSendFriendRequestButton.text = getString(R.string.cancel_friend_request)
                                peerDeclineFriendRequestButton.visibility = View.INVISIBLE
                                peerDeclineFriendRequestButton.isEnabled = false
                            }

                            VALUE_FRIEND_REQUEST_RECEIVED -> {
                                currentFriendshipState = VALUE_FRIEND_REQUEST_RECEIVED
                                peerSendFriendRequestButton.text = getString(R.string.accept_friend_request)
                                peerDeclineFriendRequestButton.visibility = View.VISIBLE
                                peerDeclineFriendRequestButton.isEnabled = true
                            }

                            "error" -> {
                                "This person's friendship is wrong. please take a look at the code.".toast(this)
                                Log.d(tag, "Error: $receiverUserId's friendship state is error.")
                                currentFriendshipState = VALUE_NOT_FRIEND
                                peerSendFriendRequestButton.text = getString(R.string.send_friend_request)
                                peerDeclineFriendRequestButton.visibility = View.INVISIBLE
                                peerDeclineFriendRequestButton.isEnabled = false
                            }
                        }
                    } else {
                        currentFriendshipState = VALUE_NOT_FRIEND
                        peerSendFriendRequestButton.text = getString(R.string.send_friend_request)
                        peerDeclineFriendRequestButton.visibility = View.INVISIBLE
                        peerDeclineFriendRequestButton.isEnabled = false
                    }
                }
    }

    private fun initializeButtons(senderUserId: String, receiverUserId: String) {
        with(peerDeclineFriendRequestButton) {
            setOnClickListener {
                cancelFriendRequest(receiverUserId, senderUserId)
            }
            visibility = View.INVISIBLE
            isEnabled = false
        }

        if (senderUserId == receiverUserId){
            peerSendFriendRequestButton.visibility = View.GONE
        } else {
            peerSendFriendRequestButton.setOnClickListener {
                Log.d(tag, "SendFriendRequestButton Clicked: $currentFriendshipState")
                when(currentFriendshipState){
                    VALUE_NOT_FRIEND -> sendFriendRequestToPerson(receiverUserId, senderUserId)
                    VALUE_FRIEND_REQUEST_SENT -> cancelFriendRequest(receiverUserId, senderUserId)
                    VALUE_FRIEND_REQUEST_RECEIVED -> acceptFriendRequest(receiverUserId, senderUserId)
                    VALUE_BECOME_FRIEND -> unfriendExistingFriend(receiverUserId, senderUserId)
                    else -> {}
                }
            }
        }
    }


    private fun unfriendExistingFriend(receiverUserId: String, senderUserId: String) {
        cancelFriendRequest(receiverUserId, senderUserId)
    }

    private fun acceptFriendRequest(receiverUserId: String, senderUserId: String) {
        Log.d(tag, "acceptFriendRequest() called: ")
        senderFriendsRef.document(receiverUserId)
                .update(mapOf(
                        KEY_FRIENDSHIP to VALUE_BECOME_FRIEND,
                        KEY_DATE to AppUtil.currentDate))
                .addOnSuccessListener {
                    receiverfriendsRef.document(senderUserId)
                            .update(mapOf(KEY_FRIENDSHIP to VALUE_BECOME_FRIEND,
                                    KEY_DATE to AppUtil.currentDate))
                            .addOnSuccessListener { _ ->
                                currentFriendshipState = VALUE_BECOME_FRIEND
                                peerSendFriendRequestButton.text = getString(R.string.unfriend_this_person)
                                with(peerDeclineFriendRequestButton){
                                    visibility = View.INVISIBLE
                                    isEnabled = false
                                }
                            }
                }
    }

    private fun cancelFriendRequest(receiverUserId: String, senderUserId: String) {
        Log.d(tag, "cancelFriendRequest() called")
        senderFriendsRef.document(receiverUserId)
                .delete()
                .addOnSuccessListener {
                    receiverfriendsRef.document(senderUserId)
                            .delete()
                            .addOnSuccessListener {
                                currentFriendshipState = VALUE_NOT_FRIEND
                                with( peerSendFriendRequestButton){
                                    text = getString(R.string.send_friend_request)
                                    isEnabled = true
                                }

                                with(peerDeclineFriendRequestButton){
                                    visibility = View.INVISIBLE
                                    isEnabled = false
                                }
                            }
                }
    }

    private fun sendFriendRequestToPerson(receiverUserId: String, senderUserId: String) {
        Log.d(tag, "sendFriendRequestToPerson() called: ")
        senderFriendsRef.document(receiverUserId)
                .set(mapOf(KEY_FRIENDSHIP to VALUE_FRIEND_REQUEST_SENT,
                        KEY_DATE to AppUtil.currentDate))
                .addOnSuccessListener {
                    receiverfriendsRef.document(senderUserId)
                            .set(mapOf(KEY_FRIENDSHIP to VALUE_FRIEND_REQUEST_RECEIVED,
                                    KEY_DATE to AppUtil.currentDate))
                            .addOnSuccessListener {
                                with(peerSendFriendRequestButton) {
                                    isEnabled = true
                                    text = getString(R.string.cancel_friend_request)
                                }
                                currentFriendshipState = VALUE_FRIEND_REQUEST_SENT
                                with(peerDeclineFriendRequestButton){
                                    visibility = View.INVISIBLE
                                    isEnabled = false
                                }
                            }
                }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home){
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
