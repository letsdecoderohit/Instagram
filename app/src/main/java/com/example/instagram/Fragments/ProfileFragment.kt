package com.example.instagram.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.AccountSettingsActivity
import com.example.instagram.Adapter.MyImagesAdapter
import com.example.instagram.Model.Post
import com.example.instagram.Model.User
import com.example.instagram.R
import com.example.instagram.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {

    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postList: List<Post>? = null
    var myImagesAdapter: MyImagesAdapter? = null

    var postListSaved: List<Post>? = null
    var mySavesImg: List<String>? = null
    var myImagesAdapterSavedImg: MyImagesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onResume() {
        super.onResume()


        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null)
        {
            this.profileId = pref.getString("profileId", "none").toString()
        }


        if (profileId == firebaseUser.uid)
        {
            edit_account_settings_btn.text = "Edit Profile"
        }
        else if (profileId != firebaseUser.uid)
        {
            checkFollowAndFollowingButtonStatus()
        }

        //recyclerview for Uploaded Images
        var recyclerViewUploadedImages: RecyclerView? = null
        recyclerViewUploadedImages = view?.findViewById(R.id.recycler_view_upload_pic)
        recyclerViewUploadedImages?.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context,3)
        recyclerViewUploadedImages?.layoutManager = linearLayoutManager

        postList = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it,postList as ArrayList<Post>) }
        recyclerViewUploadedImages?.adapter = myImagesAdapter


        //recyclerview for Saved Images
        var recyclerViewSavedImages: RecyclerView? = null
        recyclerViewSavedImages = view?.findViewById(R.id.recycler_view_saved_pic)
        recyclerViewSavedImages?.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(context,3)
        recyclerViewSavedImages?.layoutManager = linearLayoutManager2

        postListSaved = ArrayList()
        myImagesAdapterSavedImg = context?.let { MyImagesAdapter(it,postListSaved as ArrayList<Post>) }
        recyclerViewSavedImages?.adapter = myImagesAdapterSavedImg

        recyclerViewSavedImages!!.visibility = View.GONE
        recyclerViewUploadedImages!!.visibility = View.VISIBLE

        images_grid_view_btn.setOnClickListener {
            recyclerViewSavedImages!!.visibility = View.GONE
            recyclerViewUploadedImages!!.visibility = View.VISIBLE
        }

        images_save_btn.setOnClickListener {
            recyclerViewSavedImages!!.visibility = View.VISIBLE
            recyclerViewUploadedImages!!.visibility = View.GONE
        }

        total_followers.setOnClickListener {
            val intent = Intent(context,ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title","followers")
            startActivity(intent)
        }
        total_following.setOnClickListener {
            val intent = Intent(context,ShowUsersActivity::class.java)
            intent.putExtra("id",profileId)
            intent.putExtra("title","following")
            startActivity(intent)
        }


        edit_account_settings_btn.setOnClickListener {
            val getButtonText = edit_account_settings_btn.text.toString()

            when
            {
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))

                getButtonText == "Follow" -> {

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .setValue(true)
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .setValue(true)
                    }

                    addNotification()

                }

                getButtonText == "Following" -> {

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .removeValue()
                    }

                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .removeValue()
                    }

                }
            }

        }

        getFollowers()
        getFollowings()
        userInfo()
        myPhotos()
        getTotalNumberOfPost()
        mySaves()
    }



    private fun checkFollowAndFollowingButtonStatus() {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        if (followingRef != null)
        {
            followingRef.addValueEventListener(object : ValueEventListener
            {
                override fun onDataChange(p0: DataSnapshot)
                {
                    if (p0.child(profileId).exists())
                    {
                        edit_account_settings_btn?.text = "Following"
                    }
                    else
                    {
                        edit_account_settings_btn?.text = "Follow"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }

    private fun getFollowers() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    total_followers?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun getFollowings() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(profileId)
            .child("Following")


        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {
                    total_following?.text = p0.childrenCount.toString()
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    private fun myPhotos(){
        val postRef = FirebaseDatabase.getInstance().getReference().child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    (postList as ArrayList<Post>).clear()

                    for(snapshot in p0.children){
                        val post = snapshot.getValue(Post::class.java)!!
                        if(post.publisher.equals(profileId)){
                            (postList as ArrayList<Post>).add(post)
                        }
                        Collections.reverse(postList)
                        myImagesAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileId)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {

                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(pro_image_profile_frag)
                    profile_fragment_username?.text = user!!.username
                    full_name_profile_frag?.text = user!!.fullname
                    bio_profile_frag?.text = user!!.bio
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    override fun onStop() {
        super.onStop()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId", firebaseUser.uid)
        pref?.apply()
    }

    private fun getTotalNumberOfPost(){
        val postRef = FirebaseDatabase.getInstance().getReference().child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    var postCounter = 0

                    for (datasnapshot in snapshot.children) {

                        val post = datasnapshot.getValue(Post::class.java)
                        if (post?.publisher == profileId){
                            postCounter++
                        }
                    }
                    total_posts.text = "" + postCounter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun mySaves() {
     mySavesImg = ArrayList()
        val savedRef = FirebaseDatabase.getInstance()
            .reference.child("Saves")
            .child(firebaseUser.uid)

        savedRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    for (snapshot in dataSnapshot.children){
                        (mySavesImg as ArrayList<String>).add(snapshot.key!!)
                    }
                    readSavedImgData()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun readSavedImgData() {
        val postRef = FirebaseDatabase.getInstance().getReference().child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    (postListSaved as ArrayList<Post>).clear()

                    for(snapshot in dataSnapshot.children){
                        val post = snapshot.getValue(Post::class.java)

                        for(key in mySavesImg!!){
                            if(post!!.postid == key){
                                (postListSaved as ArrayList<Post>).add(post!!)
                            }
                        }
                    }
                    myImagesAdapterSavedImg!!.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun addNotification(){
        val notiRef = FirebaseDatabase.getInstance()
            .reference.child("Notifications")
            .child(profileId)

        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "started following you "
        notiMap["postid"] = ""
        notiMap["ispost"] = false

        notiRef.push().setValue(notiMap)
    }

}