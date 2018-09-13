package com.dew.edward.dewchat.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.model.Message
import com.dew.edward.dewchat.util.AppUtil
import com.dew.edward.dewchat.util.DbUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.message_layout.view.*


/**
 *   Created by Edward on 9/11/2018.
 */
class MessageAdapter(options: FirestoreRecyclerOptions<Message>) : FirestoreRecyclerAdapter<Message, MessageAdapter.MessageViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_layout, parent, false)

        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, model: Message) {
        val messageSenderId = DbUtil.currentUserId!!
        val fromUserId = model.from

        if (fromUserId != messageSenderId) { // message doesn't belong to the current user.
            DbUtil.usersRef.document(fromUserId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        val fromUserProfileImage = documentSnapshot.getString("profileImageUrl")
                        if (fromUserProfileImage != null) {
                            Picasso.get().load(fromUserProfileImage).into(holder.receiverProfileImage)
                        }
                    }
        }

        val timestamp = "${model.date} ${model.time}"
        holder.timestampText.text = timestamp

        if (model.type == "text"){
            if (fromUserId == messageSenderId){ // currentUser's message
                holder.senderMessageText.text = model.message
                holder.senderMessageText.visibility = View.VISIBLE

                holder.receiverProfileImage.visibility = View.GONE
                holder.receiverMessageImage.visibility = View.GONE
                holder.receiverMessageText.visibility = View.GONE
                holder.senderMessageImage.visibility = View.GONE
            } else { // received message
                holder.receiverMessageText.text = model.message
                holder.receiverMessageText.visibility = View.VISIBLE
                holder.receiverProfileImage.visibility = View.VISIBLE

                holder.receiverMessageImage.visibility = View.GONE
                holder.senderMessageText.visibility = View.GONE
                holder.senderMessageImage.visibility = View.GONE
            }
        } else {
            if (fromUserId == messageSenderId){
                Picasso.get().load(model.message).into(holder.senderMessageImage)
                holder.senderMessageImage.visibility = View.VISIBLE

                holder.receiverProfileImage.visibility = View.GONE
                holder.receiverMessageImage.visibility = View.GONE
                holder.senderMessageText.visibility = View.GONE
                holder.receiverMessageText.visibility = View.GONE
            } else {
                Picasso.get().load(model.message).into(holder.receiverMessageImage)
                holder.receiverMessageImage.visibility = View.VISIBLE
                holder.receiverProfileImage.visibility = View.VISIBLE

                holder.senderMessageImage.visibility = View.GONE
                holder.senderMessageText.visibility = View.GONE
                holder.receiverMessageText.visibility = View.GONE
            }
        }
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiverProfileImage = itemView.messageReceiverProfileImage
        val receiverMessageText = itemView.messageReceiverMessageText
        val senderMessageText = itemView.messageSenderMessageText
        val timestampText = itemView.MessageTimestampText
        val receiverMessageImage = itemView.messageReceiverImage
        val senderMessageImage = itemView.messageSenderImage
    }
}