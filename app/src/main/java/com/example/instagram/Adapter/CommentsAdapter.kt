package com.example.instagram.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Model.Comment
import com.example.instagram.Model.User
import com.example.instagram.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.comments_item_layout.view.*
import kotlinx.android.synthetic.main.posts_layout.view.*

class CommentsAdapter (private val mContext: Context, private val mComment: MutableList<Comment>?)
    :RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comments_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return mComment!!.size
    }

    override fun onBindViewHolder(holder: CommentsAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val comment = mComment!![position]

        holder.commentTV.text = comment.comment
        getUserInfo(holder.imageProfile,holder.usernameTV,comment.publisher)
    }



    inner  class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView){

        var imageProfile: CircleImageView = itemView.user_profile_image_comment
        var usernameTV: TextView = itemView.user_name_comment
        var commentTV: TextView = itemView.comment_comment
    }

    private fun getUserInfo(imageProfile: CircleImageView, usernameTV: TextView, publisher: String) {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisher)
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.exists()){
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get().load(user!!.image).placeholder(R.drawable.profile).into(imageProfile)
                    usernameTV.text = user!!.username

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}