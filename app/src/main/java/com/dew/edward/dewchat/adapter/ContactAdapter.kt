package com.dew.edward.dewchat.adapter

import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.ViewGroup
import com.dew.edward.dewchat.model.Friend
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.VALUE_BECOME_FRIEND
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.squareup.picasso.Picasso


/**
 *   Created by Edward on 9/13/2018.
 */
class ContactAdapter(options: FirestoreRecyclerOptions<Friend>, val complete: () -> Unit) : FriendAdapter(options) {
    private val tag = this::class.java.simpleName

    override fun onBindViewHolder(holder: FriendAdapter.FriendViewHolder, position: Int, model: Friend) {

        val friendUserId = snapshots.getSnapshot(position).id  // userId used as friendDocumentId
        var localModel: Friend? = null
        DbUtil.usersRef
                .document(DbUtil.currentUserId!!)
                .collection("Friends")
                .document(friendUserId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    Log.d(tag, "friendUserId = $friendUserId")
                    if (documentSnapshot.exists()) {
                        localModel = documentSnapshot.toObject(Friend::class.java)
                        holder.userDateText.text = "Since: ${localModel?.date}"
                    } else {
                        holder.userDateText.text = "Since: "
                    }
                }

        DbUtil.usersRef.document(friendUserId)
                .get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    val friendName = documentSnapshot.get("userFullName").toString()
                    holder.userFullNameText.text = friendName
                    val friendProfileImage = documentSnapshot.get("profileImageUrl").toString()
                    if (friendProfileImage.isNotEmpty()) {
                        Picasso.get().load(friendProfileImage).into(holder.profileImage)
                    }

                    holder.mView.setOnClickListener { view ->
                        Log.d(tag, "model.friendship = ${localModel?.friendship}")
                        if (friendUserId != DbUtil.currentUserId) {
                            if (localModel != null &&
                                    localModel!!.friendship == VALUE_BECOME_FRIEND) {
                                super.sendUserToChatActivity(view.context, friendName, friendUserId)
                                complete()
                            } else {
                                super.sendUserToPeerProfileActivity(holder.mView.context, friendUserId)
                            }
                        }
                    }
                }
    }

}