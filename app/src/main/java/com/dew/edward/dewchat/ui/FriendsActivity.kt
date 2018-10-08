package com.dew.edward.dewchat.ui

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DividerItemDecoration
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.adapter.FriendAdapter
import com.dew.edward.dewchat.model.Friend
import com.dew.edward.dewchat.util.DbUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.activity_friends.*

class FriendsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initializeFriendList(this)
    }

    private fun initializeFriendList(context: Context) {
        val query = DbUtil.usersRef.document(DbUtil.currentUserId!!).collection("Friends")
        val options = FirestoreRecyclerOptions.Builder<Friend>()
                .setQuery(query, Friend::class.java)
                .setLifecycleOwner(this)
                .build()
        friendsFriendList.adapter = FriendAdapter(options)
        // add line-divider
        val horizontalDividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val horizontalDivider = ContextCompat.getDrawable(context, R.drawable.line_divider)
        horizontalDividerItemDecoration.setDrawable(horizontalDivider!!)
        friendsFriendList.addItemDecoration(horizontalDividerItemDecoration)
    }
}
