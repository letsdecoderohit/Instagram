package com.example.instagram.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.CommentsActivity
import com.example.instagram.Fragments.PostDetailsFragment
import com.example.instagram.Fragments.ProfileFragment
import com.example.instagram.MainActivity
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
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.posts_layout.view.*

class PostAdapter(private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(mContext).inflate(R.layout.posts_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = mPost[position]

        Picasso.get().load(post.postimage).into(holder.postImage)

        if(post.description.equals("")){
            holder.description.visibility = View.GONE
        }else{
            holder.description.visibility = View.VISIBLE
            holder.description.text = post.description
        }

        publisherInfo(holder.profileImage,holder.userName, holder.publisher, post.publisher)
        isLikes(post.postid,holder.likeButton)
        numberOfLikes(holder.likes,post.postid)
        getTotalComments(holder.comments,post.postid)
        checkSavedStatus(post.postid,holder.saveButton)


        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("postId",post.postid)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PostDetailsFragment())
                .commit()
        }

        holder.publisher.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("profileId",post.publisher)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }

        holder.profileImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("profileId",post.publisher)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("profileId",post.publisher)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ProfileFragment())
                .commit()
        }

        holder.likeButton.setOnClickListener {
            if(holder.likeButton.tag == "Like"){

                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.postid)
                    .child(firebaseUser!!.uid)
                    .setValue(true)

                addNotification(post.publisher, post.postid)
            }
            else{
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.postid)
                    .child(firebaseUser!!.uid)
                    .removeValue()

                val intent = Intent(mContext,MainActivity::class.java)
                mContext.startActivity(intent)
            }
        }

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id",post.postid)
            intent.putExtra("title","likes")
            mContext.startActivity(intent)
        }

        holder.commentButton.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId",post.postid)
            intentComment.putExtra("publisherId",post.publisher)
            mContext.startActivity(intentComment)
        }

        holder.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentsActivity::class.java)
            intentComment.putExtra("postId",post.postid)
            intentComment.putExtra("publisherId",post.publisher)
            mContext.startActivity(intentComment)
        }

        holder.saveButton.setOnClickListener {
            if(holder.saveButton.tag == "Save"){
                FirebaseDatabase.getInstance().reference
                    .child("Saves")
                    .child(firebaseUser!!.uid)
                    .child(post.postid)
                    .setValue(true)
            }else{
                FirebaseDatabase.getInstance().reference
                    .child("Saves")
                    .child(firebaseUser!!.uid)
                    .child(post.postid)
                    .removeValue()
            }

        }

    }

    private fun checkSavedStatus(postid: String,imageView: ImageView){
        val savesRef = FirebaseDatabase.getInstance().reference
            .child("Saves")
            .child(firebaseUser!!.uid)

        savesRef.addValueEventListener(object  : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.child(postid).exists()){
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag = "Saved"
                }else{
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag = "Save"
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun numberOfLikes(likes: TextView, postid: String) {

        val LikesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postid)

        LikesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                   likes.text = snapshot.childrenCount.toString() + " Likes"
                }
                else{

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getTotalComments(comments: TextView, postid: String) {

        val commentsRef = FirebaseDatabase.getInstance().reference
            .child("Comments")
            .child(postid)

        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    comments.text = " view all " + snapshot.childrenCount.toString() + " comments"
                }
                else{

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun isLikes(postid: String, likeButton: ImageView) {

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val LikesRef = FirebaseDatabase.getInstance().reference
            .child("Likes")
            .child(postid)

        LikesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(firebaseUser!!.uid).exists()){
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                }
                else{
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView){
        var profileImage: CircleImageView = itemView.user_profile_image_post
        var postImage: ImageView = itemView.post_image_home
        var likeButton: ImageView = itemView.post_image_like_btn
        var commentButton: ImageView = itemView.post_image_comment_btn
        var saveButton: ImageView = itemView.post_image_save_btn
        var userName: TextView = itemView.post_user_name
        var likes: TextView = itemView.likes
        var publisher: TextView = itemView.publisher
        var description: TextView = itemView.description
        var comments: TextView = itemView.comments
    }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherid: String) {

        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherid)
        usersRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(profileImage)
                    userName.setText(user!!.username)
                    publisher.setText(user!!.fullname)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private fun addNotification(userId: String, postid: String){
        val notiRef = FirebaseDatabase.getInstance()
            .reference.child("Notifications")
            .child(userId)

        val notiMap = HashMap<String,Any>()
        notiMap["userid"] = firebaseUser!!.uid
        notiMap["text"] = "liked your post:"
        notiMap["postid"] = postid
        notiMap["ispost"] = true

        notiRef.push().setValue(notiMap)
    }
}