package com.dew.edward.dewchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.dew.edward.dewchat.adapter.ContactAdapter
import com.dew.edward.dewchat.adapter.PostAdapter
import com.dew.edward.dewchat.model.Friend
import com.dew.edward.dewchat.model.PostData
import com.dew.edward.dewchat.model.UserData
import com.dew.edward.dewchat.ui.*
import com.dew.edward.dewchat.util.DbUtil
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val tag = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        fab.setOnClickListener { _ ->
            sendUserToPostActivity()
        }
        fab.visibility = View.GONE

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        displayNavigation(this)
        displayAllPosts()
    }

    private fun displayNavigation(context: Context) {
        if (DbUtil.currentUserId != null) {
            DbUtil.usersRef.document(DbUtil.currentUserId!!)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        val profileImage = documentSnapshot.getString("profileImageUrl")
                        val fullName = documentSnapshot.getString("userFullName")
                        navUserNameText.text = fullName
                        if (profileImage != null && profileImage.isNotEmpty()) {
                            Picasso.get().load(profileImage).into(navUserProfileImage)
                        }
                    }

            navChannelList.setHasFixedSize(true)

            val query = DbUtil.usersRef
            val options = FirestoreRecyclerOptions.Builder<Friend>()
                    .setQuery(query, Friend::class.java)
                    .setLifecycleOwner(this)
                    .build()
            navChannelList.adapter = ContactAdapter(options) {
                if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
                    drawer_layout.closeDrawer(GravityCompat.START)
                }
            }

            // add line-divider
            val horizontalDividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            val horizontalDivider = ContextCompat.getDrawable(context, R.drawable.line_divider)
            horizontalDividerItemDecoration.setDrawable(horizontalDivider!!)
            navChannelList.addItemDecoration(horizontalDividerItemDecoration)
        }
    }

    private fun displayAllPosts() {
        mainPostList.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        mainPostList.layoutManager = linearLayoutManager

        val query =
            DbUtil.postsRef.orderBy("postIndex")

        val options = FirestoreRecyclerOptions.Builder<PostData>()
                .setQuery(query, PostData::class.java)
                .setLifecycleOwner(this)
                .build()
        val adapter = PostAdapter(options)

        mainPostList.adapter = adapter
        Log.d(tag, "adapter.itemCount = ${adapter.itemCount}")
    }


    override fun onStart() {
        super.onStart()
        val currentUser = DbUtil.mAuth.currentUser
        if (currentUser == null) {
            sendUserToLoginActivity()
        } else {
            checkUserExistence()
        }
    }

    private fun checkUserExistence() {
        val currentUserId = DbUtil.mAuth.currentUser?.uid
        if (currentUserId != null) {
            DbUtil.usersRef.document(currentUserId)
                    .addSnapshotListener { documentSnapshot,
                                           exception ->
                        if (exception != null) {
                            Log.d(tag, "Exception: ${exception.message}")
                            return@addSnapshotListener
                        }

                        if (documentSnapshot == null || !documentSnapshot.exists()) {
                            sendUserToSetupActivity()
                        }
                    }
        }
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.options_add_post -> {
                sendUserToPostActivity()
                return true
            }

            R.id.options_action_settings -> {
                sendUserToSettingsActivity()
                return true
            }

            R.id.options_user_profile -> {
                startActivity(Intent(this, SetupActivity::class.java))
                return true
            }

            R.id.options_account_details -> {
                startActivity(Intent(this@MainActivity, DetailsActivity::class.java))
                return true
            }

            R.id.options_settings_sign_out -> {
                DbUtil.signOut()
                sendUserToLoginActivity()
                return true
            }
            R.id.options_user_summary -> {
                startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                return true
            }

            R.id.options_find_friend -> {
                startActivity(Intent(this@MainActivity, FindFriendActivity::class.java))
                return true
            }

            R.id.options_friends -> {
                startActivity(Intent(this@MainActivity, FriendsActivity::class.java))
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun sendUserToSettingsActivity() {
        val intent = Intent(this@MainActivity, SettingsActivity::class.java)
        startActivity(intent)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun sendUserToLoginActivity() {
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        // this line cause the leakage
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        if (!this@MainActivity.isFinishing) {
            finish()
        }
    }

    private fun sendUserToSetupActivity() {
        val intent = Intent(this@MainActivity, SetupActivity::class.java)
        startActivity(intent)
//        if (!this@MainActivity.isFinishing) {
//            finish()
//        }
    }

    private fun sendUserToPostActivity() {
        startActivity(Intent(this@MainActivity, PostActivity::class.java))
    }


}
