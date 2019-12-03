package com.example.s1.menuui

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.board.*
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.Color
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.app.ComponentActivity.ExtraData
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.board.view.*


class BoardActivity : AppCompatActivity() {
    val firestore = FirebaseFirestore.getInstance()
//    var docId : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.board)

        val boardImgView = findViewById<ImageView>(R.id.detailviewitem_imageview_content)
        val boardTextView = findViewById<TextView>(R.id.detailviewitem_explain_textview)
        val favoriteCounterTextView = findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview)
        val commentListView = findViewById<ListView>(R.id.detailView_comment_lv)
        val newCommentsEditText = findViewById<EditText>(R.id.comments_edit_text)
        val commentWriteBtn = findViewById<Button>(R.id.comments_write_btn)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val commentDeleteBtn = findViewById<Button>(R.id.board_delete_btn)
        var selectedItemPos = -1

        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayHomeAsUpEnabled(true)
        commentDeleteBtn.isVisible = false

        var intent = intent
//        var contents: String
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
//        var commentProfileURL = hashMapOf<String, String>()
        firestore.collection("images").document(docId).collection("comments")
            .orderBy("timestamp").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                comments.clear()

                if (querySnapshot == null)return@addSnapshotListener

                for (snapshot in querySnapshot) {
                    var comment = snapshot.toObject(CommentDTO::class.java)
                    comments.add(comment)
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
            firestore.collection("images").document(docId).collection("comments").document()
                .set(newComment)
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(newCommentsEditText.windowToken, 0)
        }
        var isClicked = false
        commentListView.setOnItemClickListener { parent, view, position, id ->
            commentDeleteBtn.isVisible = true
            selectedItemPos = position
            Log.d("SELECT", "select : " + parent.selectedItemPosition)
            if (isClicked) {
//                var builder = AlertDialog.Builder(ContextThemeWrapper(this@BoardActivity, R.style.Theme_AppCompat_Dialog))
//                parent.isSelected = false
//                view.invalidate()
//                commentListView.setItemChecked(position, false)
                view.setBackgroundColor(Color.WHITE)
//                Log.d("SELECT", parent.selectedItemPosition.toString())
                if (comments[position].uid == FirebaseAuth.getInstance().currentUser?.uid!!) {
                    commentDeleteBtn.isVisible = false
                }
            }
            else {
                view.setBackgroundColor(Color.WHITE)
                commentDeleteBtn.isVisible = true
            }
            isClicked = !isClicked
        }

        commentDeleteBtn.setOnClickListener {
            //            var pos = commentListView.selectedItemPosition
            Log.d("SELECT", selectedItemPos.toString())
            var cmtId : String = ""
            firestore.collection("images").document(docId).collection("comments")
                .whereEqualTo("timestamp", comments[selectedItemPos].timestamp).addSnapshotListener { querySnapshot, exception ->
                    if (querySnapshot == null) return@addSnapshotListener

                    for (snapshot in querySnapshot.documents) {
                        cmtId = snapshot.id
                        Log.d("SELECT", snapshot.reference.toString())
                        Log.d("SELECT", "selected : " + commentListView.selectedItemPosition.toString())
//                        commentListView.selector.setVisible(false, true)
                        snapshot.reference.delete()
//                        Log.d("SELECT", cmtId)
                        Log.d("SELECT", "after delete" + commentListView.selectedItemPosition.toString())
                        comments.removeAt(selectedItemPos)
                        commentListView.adapter = commentListAdapter
                    }
                }
            commentDeleteBtn.isVisible = false
        }

    }

    fun deleteCurrentPost() {
        Log.d("Board", "Delete in")
        /*
        firestore.collection("images").document(docId!!).delete().addOnSuccessListener {
            Toast.makeText(baseContext, "Complete Deletion", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(baseContext, "Failed", Toast.LENGTH_SHORT).show()
        }
        finish()
         */
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.board_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.post_delete -> {
                deleteCurrentPost()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }
}