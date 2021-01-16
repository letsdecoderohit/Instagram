package com.example.instagram.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.instagram.Fragments.PostDetailsFragment
import com.example.instagram.Model.Post
import com.example.instagram.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.images_item_layout.view.*

class MyImagesAdapter(private var mContext: Context, mPost: List<Post>)
    :RecyclerView.Adapter<MyImagesAdapter.ViewHolder?>() {


    private var mPost: List<Post>? = null

    init {
        this.mPost = mPost
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyImagesAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.images_item_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
     return mPost!!.size
    }

    override fun onBindViewHolder(holder: MyImagesAdapter.ViewHolder, position: Int) {
        val post: Post = mPost!![position]
        Picasso.get().load(post.postimage).into(holder.postImage)

        holder.postImage.setOnClickListener {
            val editor = mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("postId",post.postid)
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PostDetailsFragment())
                .commit()
        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView){
        var postImage :ImageView = itemView.post_image
    }
}