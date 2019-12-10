package com.example.s1.menuui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore

class CommentListAdapter(var commentDTOList: ArrayList<CommentDTO>): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(parent?.context).inflate(R.layout.comment_list_item, null)
        val profilePhoto = view.findViewById<ImageView>(R.id.comment_profile_image)
        val profileId = view.findViewById<TextView>(R.id.comment_id_tv)
        val commentContents = view.findViewById<TextView>(R.id.comment_contents_tv)
//        val deleteBtn = parent?.findViewById<Button>(R.id.board_delete_btn)

        profileId.text = commentDTOList[position].userId
        commentContents.text = commentDTOList[position].comment

        FirebaseFirestore.getInstance().collection("profileImages").document(commentDTOList[position].uid!!).addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            var url = documentSnapshot?.data!!["image"].toString()
            Glide.with(view).load(url).apply(RequestOptions().circleCrop()).into(profilePhoto)
        }

        return view
    }

    override fun getCount(): Int {
        return commentDTOList.size
    }

    override fun getItem(position: Int): Any {
        return commentDTOList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }
}