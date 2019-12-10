package com.example.s1.menuui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_freeboard.*
import kotlinx.android.synthetic.main.comment_list_item.*
import kotlinx.android.synthetic.main.comment_list_item.view.*

class FreeBoardActivity : AppCompatActivity() {
    var profileImgUrl:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_freeboard)

        setSupportActionBar(free_post_toolbar)

        val sb = supportActionBar!!
        sb.setDisplayHomeAsUpEnabled(true)
        sb.setDisplayShowTitleEnabled(false)

        var intent = intent
        var freeBoardDTO = intent.getParcelableExtra<FreeBoardDTO>("board")
        var docId = intent.getStringExtra("docId")

        Glide.with(this).load(freeBoardDTO.imgUrl).into(free_post_iv)
        free_item_menu_tv.text = freeBoardDTO.menu
        free_item_restaurant_tv.text = freeBoardDTO.location
        free_post_review_tv.text = freeBoardDTO.review
        free_post_rating_bar.rating = freeBoardDTO.rating
        free_post_rating_bar.isClickable = false
        free_post_profile_tv.text = freeBoardDTO.userId
        addProfileImg(freeBoardDTO.uid!!)

        free_post_view_cmt_btn.setOnClickListener {
            var nextIntent = Intent(this, CommentActivity::class.java)
            var content = "Menu? " + freeBoardDTO.menu + "\nLocation? " + freeBoardDTO.location

            nextIntent.putExtra("contentUid",docId)
            nextIntent.putExtra("destinationUid",freeBoardDTO.uid)
            nextIntent.putExtra("content", content)
            nextIntent.putExtra("contentUserId", freeBoardDTO.userId)
            nextIntent.putExtra("contentProfileUrl", profileImgUrl)
            nextIntent.putExtra("docId", docId)
            nextIntent.putExtra("collection", "freeboard")

            startActivity(nextIntent)
        }
    }

    fun addProfileImg(uid: String) {
        FirebaseFirestore.getInstance()?.collection("profileImages")?.document(uid)?.addSnapshotListener { documentSnapshot, exception ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                if (url != null) {
                    profileImgUrl = url.toString()
                    Glide.with(this).load(url).apply(RequestOptions().circleCrop())
                        .into(free_post_profile_iv)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}