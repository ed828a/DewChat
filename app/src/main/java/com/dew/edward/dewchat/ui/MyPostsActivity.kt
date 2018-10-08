package com.dew.edward.dewchat.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.adapter.PostAdapter
import com.dew.edward.dewchat.model.PostData
import com.dew.edward.dewchat.util.DbUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_my_posts.*

class MyPostsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_posts)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        myPostList.layoutManager = linearLayoutManager
        myPostList.setHasFixedSize(true)

        val query = DbUtil.postsRef
                .whereEqualTo("userId", DbUtil.currentUserId)
                .orderBy("postIndex", Query.Direction.DESCENDING)
        val options =
                FirestoreRecyclerOptions.Builder<PostData>()
                        .setQuery(query, PostData::class.java)
                        .setLifecycleOwner(this)
                        .build()
        myPostList.adapter = PostAdapter(options)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = if (item?.itemId == android.R.id.home) {
        onBackPressed()
        true
    } else {
        super.onOptionsItemSelected(item)
    }

}
