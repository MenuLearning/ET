package com.example.s1.menuui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.fragment_wish_menu.view.*
import kotlinx.android.synthetic.main.post_list_item.view.*
import java.util.zip.Inflater

class WishMenuFragment : Fragment() {

    private lateinit var wishMenuViewModel: WishMenuViewModel
    var firestore: FirebaseFirestore? = null
    var uid: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        wishMenuViewModel =
            ViewModelProviders.of(this).get(WishMenuViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_wish_menu, container, false)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        /*
        val textView: TextView = root.findViewById(R.id.text_wish_menu)
        wishMenuViewModel.text.observe(this, Observer {
            textView.text = it
        })
         */
        root.wish_menu_recycler_view.adapter = WishMenuRecyclerViewAdapter()
        root.wish_menu_recycler_view.layoutManager = LinearLayoutManager(activity)

        return root
    }

    inner class WishMenuRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var wishMenu: ArrayList<FreeBoardDTO> = arrayListOf()
        var snapshotIds: ArrayList<String> = arrayListOf()
        val TAG = "WISH"
        init {
            firestore?.collection("freeboard")?.addSnapshotListener { querySnapshot, exception ->
                wishMenu.clear()
                snapshotIds.clear()

                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents) {
                    var freeboardDTO = snapshot.toObject(FreeBoardDTO::class.java)!!

                    if (freeboardDTO.favorites.containsKey(uid)) {
                        wishMenu.add(freeboardDTO)
                        snapshotIds.add(snapshot.id)
                    }
                }
                Log.d(TAG, wishMenu.size.toString())
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.post_list_item, parent, false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return wishMenu.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var vh = (holder as CustomViewHolder).itemView

            Glide.with(vh.context).load(wishMenu[position].imgUrl).into(vh.post_food_iv)
            vh.thums_cnt_tv.text = wishMenu[position].thumbsCount.toString()
            vh.post_menu.text = wishMenu[position].menu
            vh.post_location.text = wishMenu[position].location

            if (wishMenu[position].favorites.containsKey(uid)) {
                vh.add_wish_button.setImageResource(R.drawable.heart_on_32)
            }
            else {
                vh.add_wish_button.setImageResource(R.drawable.heart_off_32)
            }

            vh.setOnClickListener {
                var intent = Intent(context, FreeBoardActivity::class.java)
                intent.putExtra("board", wishMenu[position])
                intent.putExtra("docId", snapshotIds[position])
                startActivity(intent)
            }

            vh.add_wish_button.setOnClickListener {
                addWishEvent(position)
            }
        }

        fun addWishEvent(position : Int) {
            var tsDoc = firestore?.collection("freeboard")?.document(snapshotIds[position])
            firestore?.runTransaction { transaction ->

                var freeBoardDTO = transaction.get(tsDoc!!).toObject(FreeBoardDTO::class.java)

                if (freeBoardDTO!!.favorites.containsKey(uid)) {
                    //When the button is clicked
                    freeBoardDTO?.favorites.remove(uid)
                } else {
                    //When the button is not clicked
                    freeBoardDTO?.favorites[uid!!] = true
//                    favoriteAlarm(freeboardDTOs[position].uid!!)
                }
                transaction.set(tsDoc, freeBoardDTO)
            }
        }
    }
}