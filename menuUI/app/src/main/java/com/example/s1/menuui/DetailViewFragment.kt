package com.example.s1.menuui

import android.app.AlertDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.fragment_user.view.*
import kotlinx.android.synthetic.main.item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DetailViewFragment : Fragment() {

    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    val TAG = "ADD"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)

        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        view.detailviewfragment_recyclerview.adapter = DetailViewRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)

        view.freeboard_btn.setOnClickListener{
            val freeBoardFragment = FreeBoardFragment()
            fragmentManager?.beginTransaction()?.replace(R.id.main_content, freeBoardFragment)?.commit()
        }

        view.qna_btn.setOnClickListener{
            val qnaFragment = QNAFragment()
            fragmentManager?.beginTransaction()?.replace(R.id.main_content, qnaFragment)?.commit()
        }
        return view
    }
    private inner class InfoValueEventListener: ValueEventListener {
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

    private inner class PostUrlTask : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String {
            var url = URL("http://9cb82d98.ngrok.io")
            var message = "success"
            var con: HttpURLConnection = url.openConnection() as HttpURLConnection

            try {
                con.doOutput = true
                con.requestMethod = "POST"

                con.setChunkedStreamingMode(0)
                con.setRequestProperty("Accept_Encoding", "UTF-8")

                var dos = DataOutputStream(con.outputStream)
                var out: OutputStream = BufferedOutputStream(con.outputStream)
                dos.writeChars(params[0])
                dos.flush()
            }catch (e: Exception) {
                message = "connection failed"
                Log.d("TAG", "Connection Error : " + e.toString())
            }finally {
                con.disconnect()
            }
            return message
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(result)
                .setPositiveButton("ok", null).create().show()
        }
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        var contentUidList : ArrayList<String> = arrayListOf() // post id

        init {
            firestore?.collection("images")?.orderBy("timestamp", Query.Direction.DESCENDING)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()

                //Sometimes, This code return null of querySnapshot when it signout
                if(querySnapshot == null) return@addSnapshotListener

                for(snapshot in querySnapshot!!.documents){
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(p0.context).inflate(R.layout.item_detail,p0,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var viewholder = (p0 as CustomViewHolder).itemView

            //UserId
            viewholder.detailviewitem_profile_textview.text = contentDTOs!![p1].userId

            //User Profile Image
            firestore?.collection("profileImages")?.document(contentDTOs[p1].uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if(documentSnapshot == null) return@addSnapshotListener
                if(documentSnapshot.data != null){
                    var url = documentSnapshot?.data!!["image"]
                    Glide.with(context!!).load(url).apply(RequestOptions().circleCrop()).into(viewholder.detailviewitem_profile_image)
                }
            }

            //Image
            Glide.with(p0.itemView.context).load(contentDTOs!![p1].imageUrl).into(viewholder.detailviewitem_imageview_content)

            //Explain of content
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![p1].explain

            //likes
            viewholder.detailviewitem_favoritecounter_textview.text = "Likes " + contentDTOs!![p1].favoriteCount

            //This code is when the button is clicked
            viewholder.detailviewitem_favorite_imageview.setOnClickListener {
                favoriteEvent(p1)
            }

            //This code is when the page is loaded
            if(contentDTOs!![p1].favorites.containsKey(uid)){
                //This is like status
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite)

            }else{
                //This is unlike status
                viewholder.detailviewitem_favorite_imageview.setImageResource(R.drawable.ic_favorite_border)
            }

            //This code is when the profile image is clicked
            viewholder.detailviewitem_profile_image.setOnClickListener {
                var fragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("destinationUid",contentDTOs[p1].uid)
                bundle.putString("userId",contentDTOs[p1].userId)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,fragment)?.commit()
            }
            // move to commentActivity
            viewholder.detailviewitem_comment_imageview.setOnClickListener { v ->
                var intent = Intent(v.context, CommentActivity::class.java)
                intent.putExtra("contentUid",contentUidList[p1])
                intent.putExtra("destinationUid",contentDTOs[p1].uid)
                intent.putExtra("content", contentDTOs[p1].explain)
                intent.putExtra("contentUserId", contentDTOs[p1].userId)
//                intent.putExtra("contentProfileUrl", profileImgUrl.get(contentDTOs[p1].uid))
                intent.putExtra("collection", "images")
                startActivity(intent)
            }
        }

        fun getProfileImgUrl(position: Int) {
            /*
            firestore?.collection("profileImages")?.document(contentDTOs[position].uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if(documentSnapshot == null) return@addSnapshotListener
                if(documentSnapshot.data != null){
                    var url = documentSnapshot?.data!!["image"]
                    Glide.with(this).load(url).apply(RequestOptions().circleCrop()).into(detailviewitem_profile_image)
                }
            }
             */
        }

        fun favoriteEvent(position : Int){
            var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
            firestore?.runTransaction { transaction ->


                var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

                if(contentDTO!!.favorites.containsKey(uid)){
                    //When the button is clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                    contentDTO?.favorites.remove(uid)
                }else{
                    //When the button is not clicked
                    contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                    contentDTO?.favorites[uid!!] = true
                    favoriteAlarm(contentDTOs[position].uid!!)

                }
                transaction.set(tsDoc,contentDTO)
            }
        }

        fun favoriteAlarm(destinationUid : String){
            var alarmDTO = AlarmDTO()
            alarmDTO.destinationUid = destinationUid
            alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
            alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
            alarmDTO.kind = 0
            alarmDTO.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
        }
    }
}