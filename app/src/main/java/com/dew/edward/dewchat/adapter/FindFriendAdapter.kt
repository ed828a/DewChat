package com.dew.edward.dewchat.adapter

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.model.FindFriend
import com.dew.edward.dewchat.ui.PeerProfileActivity
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_layout.view.*


/**
 *   Created by Edward on 9/10/2018.
 */
class FindFriendAdapter(options: FirestoreRecyclerOptions<FindFriend>) : FirestoreRecyclerAdapter<FindFriend, FindFriendAdapter.FindFriendViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindFriendViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_layout, parent, false)

        return FindFriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FindFriendViewHolder, position: Int, model: FindFriend) {
        if (model.profileImageUrl.isNotEmpty()){
            Picasso.get().load(model.profileImageUrl).into(holder.profileImage)
        }
        holder.profileFullName.text = model.userFullName
        holder.profileStatus.text = model.status
        holder.mView.setOnClickListener {
            val peerUserId = snapshots.getSnapshot(position).id
            sendUserToPeerProfileActivity(it.context, peerUserId)
        }
    }

    private fun sendUserToPeerProfileActivity(context: Context, peerUserId: String) {
        val intent = Intent(context, PeerProfileActivity::class.java)
        intent.putExtra("FriendListedUserId", peerUserId)
        startActivity(context, intent, null)
    }

    inner class FindFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mView = itemView
        val profileImage = itemView.userLayoutProfileImage
        val profileFullName = itemView.userLayoutProfileFullName
        val profileStatus = itemView.userLayoutUserStatus
    }
}