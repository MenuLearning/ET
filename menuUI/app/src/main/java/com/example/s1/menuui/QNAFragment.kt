package com.example.s1.menuui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_qna.*
import kotlinx.android.synthetic.main.fragment_qna.view.*
import kotlinx.android.synthetic.main.item_qna.view.*
import java.util.zip.Inflater

class QNAFragment : Fragment() {

    private lateinit var qnaViewModel: QNAViewModel
    private lateinit var firestore: FirebaseFirestore
    val ADD_NEW_QNA_POST = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        qnaViewModel =
            ViewModelProviders.of(this).get(QNAViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_qna, container, false)
        firestore = FirebaseFirestore.getInstance()
        /*
        val textView: TextView = root.findViewById(R.id.text_search)
        qnaViewModel.text.observe(this, Observer {
            textView.text = it
        })
         */
        root.qnafragment_recyclerview.adapter = QNARecyclerViewAdapter()
        root.qnafragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        root.qnafragment_recyclerview.addItemDecoration(DividerItemDecoration(context, 1))

        root.qna_add_btn.setOnClickListener {
            var intent = Intent(context, AddQnaActivity::class.java)
            startActivityForResult(intent, ADD_NEW_QNA_POST)
//            startActivity(intent)
        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_NEW_QNA_POST) {
            if (resultCode == RESULT_OK) {
//                qnafragment_recyclerview.adapter?.notifyDataSetChanged()
                qnafragment_recyclerview.adapter = QNARecyclerViewAdapter()
            }
        }
    }

    //DetailViewRecyclerViewAdapter
     inner class QNARecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var qnaDTOs : ArrayList<QnADTO> = arrayListOf()
        var qnaSnapshotId : ArrayList<String> = arrayListOf()

        init {
            firestore.collection("qna").orderBy("viewCount", Query.Direction.DESCENDING).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                qnaDTOs.clear()
                qnaSnapshotId.clear()

                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot!!.documents) {
                    qnaDTOs.add(snapshot.toObject(QnADTO::class.java)!!)
                    qnaSnapshotId.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_qna, parent, false)
            return CustomViewHolder(view)
        }

        override fun getItemCount(): Int {
            return qnaDTOs.size
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as CustomViewHolder).itemView

            viewHolder.qna_view_item_title.text = qnaDTOs[position].title

            viewHolder.setOnClickListener {
                var intent = Intent(activity, QnABoardActivity::class.java)
                intent.putExtra("Content", qnaDTOs[position])
                intent.putExtra("SnapshotId", qnaSnapshotId[position])
                updateViewCount(position)
                startActivity(intent)
            }
        }

        fun updateViewCount(pos: Int) {
            qnaDTOs[pos].viewCount += 1
            firestore.collection("qna").document(qnaSnapshotId[pos]).update("viewCount", qnaDTOs[pos].viewCount)
        }
    }
}