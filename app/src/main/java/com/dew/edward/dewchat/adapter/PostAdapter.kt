package com.dew.edward.dewchat.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.R.id.likeButton
import com.dew.edward.dewchat.model.PostData
import com.dew.edward.dewchat.ui.ClickPostActivity
import com.dew.edward.dewchat.ui.CommentActivity
import com.dew.edward.dewchat.util.DbUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_layout.view.*


/**
 *   Created by Edward on 9/5/2018.
 */
class PostAdapter(options: FirestoreRecyclerOptions<PostData>) :
        FirestoreRecyclerAdapter<PostData, PostAdapter.PostViewHolder>(options) {
    private val tag = this.javaClass.simpleName

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_layout, parent, false)



        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int, model: PostData) {
        val postId = snapshots.getSnapshot(position).id
        Log.d(tag, "postId = $postId")

        holder.date.text = model.date
        holder.time.text = model.time
        holder.postText.text = model.postText
        holder.userFullName.text = model.userFullName
        Picasso.get().load(model.userProfileImage).into(holder.profileImage)
        if (model.postImage.isNotEmpty()) {
            Picasso.get().load(model.postImage).into(holder.postImage)
//        }
        } else {
            holder.postImage.visibility = View.GONE
////            holder.postImage.setImageResource(android.R.color.transparent)
//            holder.postImage.setImageBitmap(null)
//            holder.postImage.destroyDrawingCache()
        }
        holder.setLikeButtonStatus(postId)
        holder.setNumberOfLikes(postId)

        holder.mView.setOnClickListener { view: View ->
            sendUserToClickPostActivity(view.context, postId)
        }

        holder.likeButton.setOnClickListener {
            Log.d(tag, "likeButton OnClick: postId = $postId")
            DbUtil.postsRef.document(postId).collection("Likes")
                    .document(DbUtil.currentUserId!!)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
//                            holder.likeButton.setImageResource(R.drawable.dislike)
                            DbUtil.postsRef.document(postId)
                                    .collection("Likes")
                                    .document(DbUtil.currentUserId!!)
                                    .delete()
                        } else {
//                            holder.likeButton.setImageResource(R.drawable.like)
                            DbUtil.postsRef.document(postId)
                                    .collection("Likes")
                                    .document(DbUtil.currentUserId!!)
                                    .set(mapOf("like" to true))
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(tag, "Error: ${exception.message}")
                    }
        }

        holder.commentButton.setOnClickListener { view: View ->
            sendUserToCommentsActivity(view.context, postId)
        }
    }

    private fun sendUserToCommentsActivity(context: Context, postId: String) {
        val intent = Intent(context, CommentActivity::class.java)
        intent.putExtra("PostId", postId)
        startActivity(context, intent, null)

    }

    private fun sendUserToClickPostActivity(context: Context, postId: String) {
        val intent = Intent(context, ClickPostActivity::class.java)
        intent.putExtra("PostId", postId)
        startActivity(context, intent, null)
    }

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mView = itemView
        val profileImage = itemView.postProfileImage
        val userFullName = itemView.postUserName
        val date = itemView.postDate
        val time = itemView.postTime
        val postText = itemView.postTextView
        val postImage = itemView.postImageView
        val likeButton = itemView.likeButton
        val numberOfLikes = itemView.displayNoOfLikes
        val commentButton = itemView.commentButton

        fun setLikeButtonStatus(postId: String) {
            DbUtil.postsRef.document(postId).collection("Likes")
                    .document(DbUtil.currentUserId!!)
                    .addSnapshotListener(itemView.context as Activity) { documentSnapshot,
                                                                         exception ->
                        if (exception != null) {
                            Log.d(tag, "Listen failed: ${exception.message}")
                            return@addSnapshotListener
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            likeButton.setImageResource(R.drawable.like)
                        } else {
                            likeButton.setImageResource(R.drawable.dislike)
                        }
                    }
        }

        fun setNumberOfLikes(postId: String) {
            DbUtil.postsRef.document(postId).collection("Likes")
                    .addSnapshotListener { querySnapshot, exception ->
                        if (exception != null) {
                            Log.d(tag, "Listen failed: ${exception.message}")
                            return@addSnapshotListener
                        }

                        if (querySnapshot != null) {
                            numberOfLikes.text = querySnapshot.size().toString()
                        }
                    }
        }
    }
}