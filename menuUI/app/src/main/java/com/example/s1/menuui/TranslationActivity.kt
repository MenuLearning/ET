package com.example.s1.menuui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_translation.*
import kotlinx.android.synthetic.main.item_translation.view.*

class TranslationActivity:AppCompatActivity() {
    private var result: ArrayList<String> = arrayListOf()
    private val TAG = "Translation"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translation)

        result = intent.getStringArrayListExtra("result")

        translate_recyclerview.adapter = TranslateRecyclerViewAdapter()
        translate_recyclerview.layoutManager = LinearLayoutManager(this)
        translate_recyclerview.addItemDecoration(DividerItemDecoration(this, 1))
    }

    private inner class TranslateRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//        var explanation: String? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_translation, parent, false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return result.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var vh = (holder as CustomViewHolder).itemView
            var arr = result[position].split(',')

//            val searchMenu = "가락국수"
            vh.item_translation_korean_tv.text = arr[0]
            vh.item_translation_result_tv.text = arr[1]
            vh.item_translation_price_tv.text = arr[2]

            Log.d(TAG, "selected position : $position")
            Log.d(TAG, "explanation : ${arr[3]}")

            if(arr[3] == " ") {
                Log.d(TAG, "empty : ${arr[0]}")
                vh.item_translation_more_info_btn.isVisible = false
            }

            vh.item_translation_more_info_btn.setOnClickListener {
                val searchMenu = arr[0]
//                Log.d(TAG, searchMenu)
                /*
                var db = FirebaseDatabase.getInstance().reference
                var ref = db.child("data")
                val infoValueEventListener = InfoValueEventListener()

                ref.child("jaZWAoZQn6yKbOERUtL2").orderByChild("korName").equalTo(searchMenu).
                    addListenerForSingleValueEvent(infoValueEventListener)
                 */

                if (vh.item_translation_explanation_tv.visibility == View.VISIBLE) {
                    vh.item_translation_explanation_tv.visibility = View.GONE
                    vh.item_line.visibility = View.GONE
                    vh.item_translation_more_info_btn.setImageResource(R.drawable.ic_arrow_down_24)
                }
                else {
                    vh.item_translation_explanation_tv.visibility = View.VISIBLE
                    vh.item_line.visibility = View.VISIBLE
                    vh.item_translation_explanation_tv.text = arr[3]
                    vh.item_translation_more_info_btn.setImageResource(R.drawable.ic_arrow_up_24)
                }
            }
        }

        private inner class InfoValueEventListener: ValueEventListener{
            var explanation:String? = null

            override fun onDataChange (p0: DataSnapshot) {
                p0.children.forEach {
                    explanation = it.getValue(FoodInfoDTO::class.java)?.explanation
//                    notifyDataSetChanged()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "error : " + p0.toString())
            }
        }
    }
}