package com.example.s1.menuui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_qna_board.*
import kotlinx.android.synthetic.main.comment_list_item.view.*

class QnABoardActivity : AppCompatActivity() {
    private var firestore : FirebaseFirestore? = null
    var qnaDTO : QnADTO? = null
    var snapshotId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qna_board)
        firestore = FirebaseFirestore.getInstance()
        setSupportActionBar(qna_toolbar)
        val sb = supportActionBar!!
        sb.setDisplayHomeAsUpEnabled(true)
        sb.setDisplayShowTitleEnabled(false)

        var intent = intent
        qnaDTO = intent.getParcelableExtra<QnADTO>("Content")
        snapshotId = intent.getStringExtra("SnapshotId")

        qna_board_title_tv.text = qnaDTO?.title
        qna_board_content_tv.text = qnaDTO?.question
        qna_writer_tv.text = qnaDTO?.userId
        qna_date_tv.text = qnaDTO?.timestamp.toString()
        qna_view_cnt_tv.text = qnaDTO?.viewCount.toString()

        qna_comment_recyclerview.adapter = QnABoardRecyclerViewAdapter()
        qna_comment_recyclerview.layoutManager = LinearLayoutManager(this)
    }

    inner class QnABoardRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments : ArrayList<CommentDTO> = arrayListOf()

        init {
            firestore?.collection("qna")?.document("snapshotId")?.collection("comments")?.addSnapshotListener { querySnapshot, exception ->
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents) {
                    comments.add(snapshot.toObject(CommentDTO::class.java)!!)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.comment_list_item, parent, false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = (parent as CustomViewHolder).itemView

            view.comment_id_tv.text = comments[position].userId
            view.comment_contents_tv.text = comments[position].comment

            firestore?.collection("profileImages")?.document(comments[position].uid!!)?.addSnapshotListener { documentSnapshot, exception ->
                if (documentSnapshot == null)return@addSnapshotListener

                if (documentSnapshot.data != null) {
                    var url = documentSnapshot.data!!["image"].toString()
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.comment_profile_image)
                }
            }
        }

        override fun getItemCount(): Int {
            return comments.size
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