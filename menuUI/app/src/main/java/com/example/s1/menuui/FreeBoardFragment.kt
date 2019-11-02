package com.example.s1.menuui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.ListFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.s1.menuui.*
import com.example.s1.menuui.Post
import com.example.s1.menuui.PostListAdapter
import com.example.s1.menuui.ui.freeboard.FreeBoardViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class FreeBoardFragment : Fragment() {

    private lateinit var freeBoardViewModel: FreeBoardViewModel
    var storage : FirebaseStorage? = null
    var photoUri : Uri? = null
    var auth : FirebaseAuth? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        freeBoardViewModel =
            ViewModelProviders.of(this).get(FreeBoardViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_freeboard, container, false)
//        val textView: TextView = root.findViewById(R.id.text_send)
        val postListView: ListView = root.findViewById(R.id.post_list)
        val addFloatingBtn: FloatingActionButton = root.findViewById(R.id.plus_fab)
        val db : FirebaseFirestore = FirebaseFirestore.getInstance()

        //Initiate storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        /*
        freeBoardViewModel.text.observe(this, Observer {
            textView.text = it
        })
         */
        var myPost1 = Post("Seoul Jongro", "Chicken & Beer", 5, 5, 6, false)
        var myPost2 = Post("Yongin Suji", "김밥", 1, 3, 4, false)
        var myPost3 = Post("Suwon Yeongtong", "설렁탕", 10, 3, 4, false)
        var myPost4 = Post("Seoul Yeongsan", "떡볶이", 3, 3, 4, false)
        var postList = arrayListOf<Post>(myPost1, myPost2, myPost3, myPost4)

        var contentsList : Array<ContentDTO>

        val TAG1 = "Post"
        // load whole post
        db.collection("images").get().addOnSuccessListener { documents ->
            Log.d(TAG1, "success listener")
            Log.d(TAG1, "document size : ${documents.size()}")
            for (document in documents) {
                Log.d(TAG1, "${document.id} => ${document.data}")
            }
        }
            .addOnFailureListener { exception ->
                Log.d(TAG1, "Error getting documents: ", exception)
            }

        val postAdapter = PostListAdapter(postList)
        postListView.adapter = postAdapter

        postListView.setOnItemClickListener { parent, view, position, id ->
            Log.d("tag", "click in!")
            val intent = Intent(context, BoardActivity::class.java)
            intent.putExtra("img", images[position])
            intent.putExtra("menu", postList[position].menu)
            intent.putExtra("location", postList[position].location)
            startActivity(intent)
        }

        addFloatingBtn.setOnClickListener { view ->
            val intent = Intent(context, AddPhotoActivity::class.java)
            startActivity(intent)
        }

        return root
    }

}