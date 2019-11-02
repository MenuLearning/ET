package com.example.s1.menuui

import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.board.*


class BoardActivity : AppCompatActivity() {
    val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) { //persistemt...d왜안되지?
        super.onCreate(savedInstanceState)
        setContentView(R.layout.board)

        val boardImgView = findViewById<ImageView>(R.id.detailviewitem_imageview_content)
        val boardTextView = findViewById<TextView>(R.id.detailviewitem_explain_textview)
        val favoriteCounterTextView = findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview)
        val commentListView = findViewById<ListView>(R.id.detailView_comment_lv)
        val newCommentsEditText = findViewById<EditText>(R.id.comments_edit_text)
        val commentWriteBtn = findViewById<Button>(R.id.comments_write_btn)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayHomeAsUpEnabled(true)

        var intent = intent
        var contents: String
        var contentDTO: ContentDTO = intent.getParcelableExtra("content")
        var docId = intent.getStringExtra("snapshotId")

        // set Image
        Glide.with(this).load(contentDTO.imageUrl).into(boardImgView)
        // set main content
        boardTextView.text = contentDTO.explain
        // set favorite count
        favoriteCounterTextView.text = contentDTO.favoriteCount.toString()
        // set user Id
        detailviewitem_profile_textview.text = contentDTO.userId
        // set profile Image

        var comments = arrayListOf<CommentDTO>()
        firestore.collection("images").document(docId).collection("comments")
            .orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                comments.clear()

                if (querySnapshot == null)return@addSnapshotListener

                for (snapshot in querySnapshot) {
                    comments.add(snapshot.toObject(CommentDTO::class.java))
                }
            }

        var commentListAdapter = CommentListAdapter(comments)
        commentListView.adapter = commentListAdapter

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        commentWriteBtn.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            var newComment = CommentDTO()
            newComment.uid = user?.uid
            newComment.userId = user?.email
            newComment.timestamp = System.currentTimeMillis()
            newComment.comment = newCommentsEditText.text.toString()
            newCommentsEditText.text = null

            comments.add(newComment)
            commentListAdapter.notifyDataSetChanged()
            firestore.collection("images").document(docId).collection("comments").document().set(newComment)
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