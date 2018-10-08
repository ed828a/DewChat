package com.dew.edward.dewchat.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.R.drawable.username
import com.dew.edward.dewchat.model.Friend
import com.dew.edward.dewchat.ui.ChatActivity
import com.dew.edward.dewchat.ui.PeerProfileActivity
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.KEY_EXTRA_LISTED_USER_ID
import com.dew.edward.dewchat.util.KEY_EXTRA_USER_NAME
import com.dew.edward.dewchat.util.VALUE_BECOME_FRIEND
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_layout.view.*


/**
 *   Created by Edward on 9/10/2018.
 */
open class FriendAdapter(options: FirestoreRecyclerOptions<Friend>)
    : FirestoreRecyclerAdapter<Friend, FriendAdapter.FriendViewHolder>(options) {
    private val tag = this::class.java.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_layout, parent, false)

        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int, model: Friend) {
        holder.userDateText.text = "Since: ${model.date}"
        val friendUserId = snapshots.getSnapshot(position).id  // userId used as friendDocumentId

            DbUtil.usersRef.document(friendUserId)
                    .get()
                    .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                        val friendName = documentSnapshot.get("userFullName").toString()
                        holder.userFullNameText.text = friendName
                        val friendProfileImage = documentSnapshot.get("profileImageUrl").toString()
                        if (friendProfileImage.isNotEmpty()) {
                            Picasso.get().load(friendProfileImage).into(holder.profileImage)
                        }

                        holder.mView.setOnClickListener {view ->
                            if (model.friendship == VALUE_BECOME_FRIEND){
                            val options = arrayOf<CharSequence>("$friendName's profile", "Send Message")
                            val builder = AlertDialog.Builder(view.context)
                            with(builder) {
                                setTitle("Select options")
                                setItems(options) { _: DialogInterface?, i: Int ->
                                    when (i) {
                                        0 -> sendUserToPeerProfileActivity(view.context, friendUserId)
                                        1 -> sendUserToChatActivity(view.context, friendName, friendUserId)
                                    }
                                }
                            }
                            builder.create()
                                    .show()
                            } else {
                                sendUserToPeerProfileActivity(holder.mView.context, friendUserId)
                            }
                        }
                    }

    }

    fun sendUserToChatActivity(context: Context, username: String, friendUserId: String) {
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra(KEY_EXTRA_LISTED_USER_ID, friendUserId)
        intent.putExtra(KEY_EXTRA_USER_NAME, username)
        startActivity(context, intent, null)
    }

    fun sendUserToPeerProfileActivity(context: Context, friendUserId: String) {
        val intent = Intent(context, PeerProfileActivity::class.java)
        intent.putExtra("FriendListedUserId", friendUserId)
        startActivity(context, intent, null)
    }

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mView = itemView
        val profileImage = itemView.userLayoutProfileImage
        val userFullNameText = itemView.userLayoutProfileFullName
        val userDateText = itemView.userLayoutUserStatus
    }
}