package com.dew.edward.dewchat.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.dew.edward.dewchat.R
import com.dew.edward.dewchat.adapter.FindFriendAdapter
import com.dew.edward.dewchat.model.FindFriend
import com.dew.edward.dewchat.util.DbUtil
import com.dew.edward.dewchat.util.addDivider
import com.dew.edward.dewchat.util.toast
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.activity_find_friend.*

class FindFriendActivity : AppCompatActivity() {

    private lateinit var adapter: FindFriendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friend)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        initializeSearchResultList()
        searchFriendButton.setOnClickListener {
            val queryInput = searchInputText.text.toString().trim()
            adapter.stopListening()
            if (queryInput.isNotEmpty()) {
                val query = DbUtil.usersRef.whereEqualTo("userFullName", queryInput)
                val options = FirestoreRecyclerOptions.Builder<FindFriend>()
                        .setQuery(query, FindFriend::class.java)
                        .build()
                adapter = FindFriendAdapter(options)
                searchResultList.adapter = adapter
            } else {
                "Please write the person's name that you want to search...".toast(this)
                val query = DbUtil.usersRef.limit(100)
                val options = FirestoreRecyclerOptions.Builder<FindFriend>()
                        .setQuery(query, FindFriend::class.java)
                        .build()
                adapter = FindFriendAdapter(options)
                searchResultList.adapter = adapter
            }
            adapter.startListening()
        }
    }

    private fun initializeSearchResultList() {
        val query = DbUtil.usersRef.limit(100)
        val options = FirestoreRecyclerOptions.Builder<FindFriend>()
                .setQuery(query, FindFriend::class.java)
                .build()
        adapter = FindFriendAdapter(options)
        searchResultList.adapter = adapter
        searchResultList.setHasFixedSize(true)
        searchResultList.addDivider()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }
}
