package com.dew.edward.dewchat.ui

import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.adapter.CommentAdapter
import com.dew.edward.dewchat.model.CommentData
import com.dew.edward.dewchat.util.AppUtil
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.toast
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.DocumentReference
import kotlinx.android.synthetic.main.activity_comment.*

class CommentActivity : AppCompatActivity() {
    private val tag = this::class.java.simpleName
    lateinit var adapter: CommentAdapter
    lateinit var usersReference: DocumentReference
    var postId: String? = null
    var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        postId = intent.getStringExtra("PostId")
        if (postId == null){
            "Error: postId = null".toast(this)
            finish()
        }
        initializeAdapter(this@CommentActivity)

        commentsPostButton.setOnClickListener {
            if (DbUtil.currentUserId ==null){
                Log.d(tag, "Error: currentUserId = null")
                return@setOnClickListener
            }

            DbUtil.usersRef.document(DbUtil.currentUserId!!)
                    .get()
                    .addOnSuccessListener {documentSnapshot ->
                        val userName = documentSnapshot.getString("userFullName") ?: ""
                        postComment(userName)
                        commentsInput.text.clear()
                        AppUtil.hideKeyboard(this@CommentActivity)
                    }
        }
    }

    private fun postComment(userName: String) {
        val commentText = commentsInput.text.toString().trim()
        if (commentText.isEmpty()){
            "please write some comment ...".toast(this)
        } else {
            val comment = CommentData(commentText,
                    AppUtil.currentDate,
                    AppUtil.currentTime,
                    DbUtil.currentUserId!!,
                    userName,
                    AppUtil.currentTimeInMillis)

            DbUtil.postsRef.document(postId!!).collection("comments")
                    .add(comment)
                    .addOnSuccessListener {
                        "comment saved successfully...".toast(this)
                        Log.d(tag, "adapter.itemCount = ${adapter.itemCount}")
                        if (adapter.itemCount > 0) {
                            commentsList.smoothScrollToPosition(adapter.itemCount - 1)
                        }
                    }
                    .addOnFailureListener {
                        Log.d(tag, "Error: ${it.message}")
                    }
        }
    }

    private fun initializeAdapter(context: Context) {
        val linearLayoutManager = LinearLayoutManager(this@CommentActivity)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        commentsList.layoutManager = linearLayoutManager
        commentsList.setHasFixedSize(true)

        if (postId == null){
            "Error: PostId == null".toast(this@CommentActivity)
            return
        }
        val query = DbUtil.postsRef.document(postId!!).collection("comments")
                .orderBy("index")
        val options = FirestoreRecyclerOptions.Builder<CommentData>()
                .setLifecycleOwner(this@CommentActivity)
                .setQuery(query, CommentData::class.java)
                .build()

        adapter = CommentAdapter(options)
        commentsList.adapter = adapter

        val horizontalDividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val horizontalDivider = ContextCompat.getDrawable(context, R.drawable.line_divider)
        horizontalDividerItemDecoration?.setDrawable(horizontalDivider!!)
        commentsList.addItemDecoration(horizontalDividerItemDecoration)

    }
}
