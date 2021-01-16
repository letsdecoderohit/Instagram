package com.example.instagram.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Fragments.PostDetailsFragment
import com.example.instagram.Fragments.ProfileFragment
import com.example.instagram.Model.Notification
import com.example.instagram.Model.Post
import com.example.instagram.Model.User
import com.example.instagram.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.notification_item_layout.view.*
import kotlinx.android.synthetic.main.posts_layout.view.*

class NotificationAdapter(private val mContext: Context,private val mNotification: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>()
{


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notification_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return  mNotification.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotification[position]

        if(notification.text.equals("started following you")){
            holder.text.text = "started following you"
        }else if(notification.text.equals("liked your post")){
            holder.text.text = "liked your post"
        }else if(notification.text.contains("commented:")){
            holder.text.text = notification.text.replace("commented:","commented: ")
        }else{
            holder.text.text = notification.text
        }

        userInfo(holder.profileImage,holder.userName,notification.userid)

        if(notification.ispost){
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage,notification.postid)
        }
        else{
            holder.postImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener{
            if(notification.ispost){
                holder.postImage.setOnClickListener {
                    val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
                    editor.putString("postId",notification.postid)
                    editor.apply()
                    (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, PostDetailsFragment())
                        .commit()
            }
        }else{
                val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
                editor.putString("profileId",notification.userid)
                editor.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment())
                    .commit()
            }
        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postImage: ImageView = itemView.notification_post_image
        var profileImage: CircleImageView = itemView.notification_profile_image
        var userName: TextView = itemView.username_notification
        var text: TextView = itemView.comment_notification

    }

    private fun userInfo(imageView: ImageView,userName: TextView, publisherId: String) {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherId)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot)
            {
                if (p0.exists())
                {

                    val user = p0.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(imageView)
                    userName.text = user!!.username

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun getPostImage(imageView: ImageView,postID: String) {
        val postRef =
            FirebaseDatabase.getInstance().getReference().child("Posts").child(postID)

        postRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val post = p0.getValue<Post>(Post::class.java)

                    Picasso.get().load(post!!.postimage).placeholder(R.drawable.profile)
                        .into(imageView)
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}