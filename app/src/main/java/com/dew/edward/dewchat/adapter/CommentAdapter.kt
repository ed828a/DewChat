package com.dew.edward.dewchat.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.model.CommentData
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.comment_layout.view.*


/**
 *   Created by Edward on 9/9/2018.
 */
class CommentAdapter(options: FirestoreRecyclerOptions<CommentData>) : FirestoreRecyclerAdapter<CommentData, CommentAdapter.CommentViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.comment_layout, parent, false)

        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int, model: CommentData) {
        holder.commentContentText.text = model.comment
        holder.commentDateText.text = model.date
        holder.commentTimeText.text = model.time
        holder.userNameText.text = model.userName
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userNameText = itemView.comment_username
        val commentDateText = itemView.comment_date
        val commentContentText = itemView.comment_contents
        val commentTimeText = itemView.comment_time
    }
}