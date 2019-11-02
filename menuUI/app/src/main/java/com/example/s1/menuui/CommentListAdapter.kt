package com.example.s1.menuui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class CommentListAdapter(var commentDTOList: ArrayList<CommentDTO>): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(parent?.context).inflate(R.layout.comment_list_item, null)
        val profilePhoto = view.findViewById<ImageView>(R.id.comment_profile_image)
        val profileId = view.findViewById<TextView>(R.id.comment_id_tv)
        val commentContents = view.findViewById<TextView>(R.id.comment_contents_tv)

        profileId.text = commentDTOList[position].userId
        commentContents.text = commentDTOList[position].comment
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