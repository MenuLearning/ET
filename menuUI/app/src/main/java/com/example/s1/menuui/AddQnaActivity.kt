package com.example.s1.menuui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_add_qna.*

class AddQnaActivity : AppCompatActivity() {
    private var firestore: FirebaseFirestore? = null
    val TAG = "QNA"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_qna)
        firestore = FirebaseFirestore.getInstance()
        val sendBtn = findViewById<Button>(R.id.add_qna_send_btn)

        add_qna_send_btn.setOnClickListener {
            Log.d(TAG, "send listener in")
            var message : String? = null
            if (add_qna_title_edit_tv.text.toString() == "") {
                message = getString(R.string.title_empty)
                Log.d(TAG, "title is empty : " + message)
            }
            else if (add_qna_content_edit_tv.text.toString() == "") {
                message = getString(R.string.content_empty)
                Log.d(TAG, "content is empty : " + message)
            }

            if (message == null) {
                Log.d(TAG, "message is null")
                Log.d(TAG, "New Q&A upload")
                qnaUpload(add_qna_title_edit_tv.text.toString(), add_qna_content_edit_tv.text.toString())
            }
            else {
                Log.d(TAG, "message is not null")
                AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage(message)
                    .setPositiveButton("ok", null).create().show()
            }
        }
    }

    fun qnaUpload(title: String, content: String) {
        val auth = FirebaseAuth.getInstance()

        var qnaDTO = QnADTO()
        Log.d(TAG, "Q&A Upload in!")
        qnaDTO.title = title
        qnaDTO.question = content
        qnaDTO.timestamp = System.currentTimeMillis()
        qnaDTO.uid = auth.currentUser?.uid
        qnaDTO.userId = auth.currentUser?.email

        firestore?.collection("qna")?.document()?.set(qnaDTO)

        setResult(Activity.RESULT_OK)
        finish()
        Log.d(TAG, "out")
    }
}