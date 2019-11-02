package com.example.s1.menuui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.w3c.dom.Text

var images = arrayListOf<Int>(R.drawable.food1, R.drawable.food2, R.drawable.food3, R.drawable.food4)
//var isHeartOn = false

class PostListAdapter (var postList: ArrayList<Post>): BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = LayoutInflater.from(parent?.context).inflate(R.layout.post_list_item, null)

        val foodPhoto = view.findViewById<ImageView>(R.id.imageView3)
        val foodLocation = view.findViewById<TextView>(R.id.post_location)
        val foodMenu = view.findViewById<TextView>(R.id.post_menu)
        val addButton = view.findViewById<ImageButton>(R.id.add_wish_button)
        val thumsUpBtn = view.findViewById<Button>(R.id.button2)
        val thumsDownBtn = view.findViewById<Button>(R.id.button3)
        val thumsNum = view.findViewById<TextView>(R.id.thums_tv)

        val post = postList[position]
        var isHeartOn = post.isAdded

        foodPhoto.setImageResource(images[position])
        foodLocation.text = post.location
        foodMenu.text = post.menu

        addButton.setOnClickListener{
            Toast.makeText(parent?.context, "CLICK IN", Toast.LENGTH_LONG).show()
            if (isHeartOn) {
                isHeartOn = false
                addButton.setImageResource(R.drawable.heart_off_32)
            }
            else {
                isHeartOn = true
                addButton.setImageResource(R.drawable.heart_on_32)
            }
        }

        thumsUpBtn.setOnClickListener {
            var cnt = Integer.parseInt(thumsNum.text.toString())
            cnt += 1;
            thumsNum.text = cnt.toString()
        }

        thumsDownBtn.setOnClickListener {
            var cnt = Integer.parseInt(thumsNum.text.toString())
            cnt -= 1;
            thumsNum.text = cnt.toString()
        }
        return view
    }

    override fun getItem(position: Int): Any {
        return postList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return postList.size
    }


}