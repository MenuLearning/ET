package com.example.s1.menuui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
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
    }

    private inner class TranslateRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
            val searchMenu = arr[0]

            vh.item_translation_korean_tv.text = arr[0]
            vh.item_translation_result_tv.text = arr[1]
            vh.item_translation_price_tv.text = arr[2]

            vh.setOnClickListener {
                var db = FirebaseDatabase.getInstance().reference
                var ref = db.child("data")
                ref.child("jaZWAoZQn6yKbOERUtL2").orderByChild("korName").equalTo(searchMenu).
                    addListenerForSingleValueEvent(InfoValueEventListener())
            }
        }

        private inner class InfoValueEventListener: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                p0.children.forEach {
                    var ex = it.getValue(FoodInfoDTO::class.java)?.explanation
                    Log.d(TAG, ex)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "error : " + p0.toString())
            }
        }
    }


}