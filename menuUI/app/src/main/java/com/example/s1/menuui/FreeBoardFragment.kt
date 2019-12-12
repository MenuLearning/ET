package com.example.s1.menuui

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.s1.menuui.ui.freeboard.FreeBoardViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_freeboard.*
import kotlinx.android.synthetic.main.fragment_freeboard.view.*
import kotlinx.android.synthetic.main.post_list_item.view.*

class FreeBoardFragment : Fragment() {

    private lateinit var freeBoardViewModel: FreeBoardViewModel
    var currentUserUid: String? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null
    val UPLOAD_NEW_POST = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        freeBoardViewModel =
            ViewModelProviders.of(this).get(FreeBoardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_freeboard, container, false)
        val addFloatingBtn: FloatingActionButton = root.findViewById(R.id.plus_fab)

        //Initiate storage
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid
        /*
        freeBoardViewModel.text.observe(this, Observer {
            textView.text = it
        })
         */
        root.freeboard_recycler_view.adapter = FreeBoardRecyclerAdapter()
        root.freeboard_recycler_view.layoutManager = LinearLayoutManager(activity)

        addFloatingBtn.setOnClickListener { view ->
            val intent = Intent(context, AddPostActivitiy::class.java)
            startActivityForResult(intent, UPLOAD_NEW_POST)
        }
        /*
        root.freeboard_search_btn.setOnClickListener {
            if (freeboard_search_edit_tv.text == null) {
                AlertDialog.Builder(context)
                    .setTitle("Warning")
                    .setMessage(getString(R.string.search_warning_message))
                    .setPositiveButton("ok", null)
            }
            else {
                var keyword = freeboard_search_edit_tv.text.toString()
                searchContents(keyword)
            }
        }
         */
        return root
    }

    fun searchContents(keyword: String) {
        var searchContents: ArrayList<FreeBoardDTO> = arrayListOf()
        val regex = keyword.toRegex()
        val matchResult = regex.findAll(keyword)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UPLOAD_NEW_POST) {
            if (resultCode == RESULT_OK) {
                freeboard_recycler_view.adapter?.notifyDataSetChanged()
            }
        }
    }

    inner class FreeBoardRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var freeboardDTOs : ArrayList<FreeBoardDTO> = arrayListOf()
        var snapshotIds : ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("freeboard")?.orderBy("timestamp", Query.Direction.DESCENDING)?.addSnapshotListener { querySnapshot, exception ->
                freeboardDTOs.clear()
                snapshotIds.clear()

                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents) {
                    var board = snapshot.toObject(FreeBoardDTO::class.java)
                    Log.d("TEST", board?.imgUrl.toString())
                    freeboardDTOs.add(board!!)
                    snapshotIds.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.post_list_item, parent, false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view : View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewHolder = (holder as CustomViewHolder).itemView

            viewHolder.post_location.text = freeboardDTOs[position].location
            viewHolder.post_menu.text = freeboardDTOs[position].menu
            viewHolder.post_comments_num.text = getCommentsNum(position)
            viewHolder.thums_cnt_tv.text = freeboardDTOs[position].thumbsCount.toString()
            Glide.with(viewHolder.context).load(freeboardDTOs[position].imgUrl).into(viewHolder.post_food_iv)

            // like status
            if (freeboardDTOs[position].favorites.containsKey(currentUserUid!!)) {
                viewHolder.add_wish_button.setImageResource(R.drawable.heart_on_32)
            }
            else {
                viewHolder.add_wish_button.setImageResource(R.drawable.heart_off_32)
            }

            viewHolder.setOnClickListener {
                var intent = Intent(context, FreeBoardActivity::class.java)
                intent.putExtra("board", freeboardDTOs[position])
                intent.putExtra("docId", snapshotIds[position])
                startActivity(intent)
            }

            viewHolder.thums_up_btn.setOnClickListener {
                freeboardDTOs[position].thumbsCount +=  1
                firestore?.collection("freeboard")?.document(snapshotIds[position])?.update("thumbsCount", freeboardDTOs[position].thumbsCount)
                viewHolder.thums_cnt_tv.text = freeboardDTOs[position].toString()
                notifyItemChanged(position)
            }

            viewHolder.thums_down_btn.setOnClickListener {
                freeboardDTOs[position].thumbsCount -=  1
                firestore?.collection("freeboard")?.document(snapshotIds[position])?.update("thumbsCount", freeboardDTOs[position].thumbsCount)
                viewHolder.thums_cnt_tv.text = freeboardDTOs[position].toString()
                notifyItemChanged(position)
            }

            viewHolder.add_wish_button.setOnClickListener {
                addWishEvent(position)
            }
        }

        override fun getItemCount(): Int {
            return freeboardDTOs.size
        }

        fun getCommentsNum(pos: Int): String {
            var cnt = 0
            firestore?.collection("freeboard")?.document(snapshotIds[pos])?.collection("comments")?.get()
                ?.addOnSuccessListener {
                    if (it != null) {
                        cnt = it.size()
                    }
                }
            return cnt.toString()
        }

        fun getThumsTotalCnt(pos: Int): String {
            var cnt = "0"
            firestore?.collection("freeboard")?.document(snapshotIds[pos])?.addSnapshotListener { documentSnapshot, exception ->
                if (documentSnapshot == null) return@addSnapshotListener
                if (documentSnapshot.data != null) {
                    cnt = documentSnapshot?.data!!["thumsCount"].toString()
                }
            }
            return cnt
        }

        fun addWishEvent(position : Int) {
            var tsDoc = firestore?.collection("freeboard")?.document(snapshotIds[position])
            firestore?.runTransaction { transaction ->

                var freeBoardDTO = transaction.get(tsDoc!!).toObject(FreeBoardDTO::class.java)

                if (freeBoardDTO!!.favorites.containsKey(currentUserUid)) {
                    //When the button is clicked
                    freeBoardDTO?.favorites.remove(currentUserUid)
                } else {
                    //When the button is not clicked
                    freeBoardDTO?.favorites[currentUserUid!!] = true
//                    favoriteAlarm(freeboardDTOs[position].uid!!)
                }
                transaction.set(tsDoc, freeBoardDTO)
            }
        }
    }
}