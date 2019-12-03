package com.example.s1.menuui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.gesture.Gesture
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*


class UserFragment : Fragment() {
    val TAG = "UserF"
    var fragmentView : View? = null
    var firestore : FirebaseFirestore? = null
    var uid : String? = null
    var auth : FirebaseAuth? = null
    var currentUserUid : String? = null
    var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
    var contentSnapshotId : ArrayList<String> = arrayListOf()
    var myContext : FragmentActivity? = null

    companion object {
        var PICK_PROFILE_FROM_ALBUM = 10
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_user,container,false)
//        var uid = FirebaseAuth.getInstance().currentUser?.uid
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        Log.d(TAG, "uid: " + uid)
        Log.d(TAG, "currentUserId : " + currentUserUid)
        /*
        val database = FirebaseDatabase.getInstance().reference
        database.child("data").child("KorName").addChildEventListener({DataSnapshot

        })

         */
        if(uid == currentUserUid){
            //MyPage
            Log.d(TAG, "my page in")
            Log.d(TAG, "uid : " + uid)
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut()
            }
            fragmentView?.account_reyclerview?.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                private val gestureDetector : GestureDetector = GestureDetector(context, object: GestureDetector.SimpleOnGestureListener() {
                    override fun onSingleTapUp(e: MotionEvent?): Boolean {
                        Log.d("Touch", "Single Tap Up in")
                        return true
                    }
                    /*
                    override fun onLongPress(e: MotionEvent?) {
                        Log.d("Touch", "Long Press in")
                        super.onLongPress(e)
                    }
                     */
                })
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    val child = account_reyclerview.findChildViewUnder(e.x, e.y)
                    if (child != null && gestureDetector.onTouchEvent(e)) {
                        val position = account_reyclerview.getChildLayoutPosition(child!!)
                        Log.d("TouchEvent", position.toString())
                        val intent = Intent(context, BoardActivity::class.java)
                        intent.putExtra("content", contentDTOs[position])
                        intent.putExtra("snapshotId", contentSnapshotId[position])
                        startActivity(intent)
                    }
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            })

        }else{
            //OtherUserPage
            Log.d(TAG, "other user page in")
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)

            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                requestFollow()
            }
        }
        fragmentView?.account_reyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_reyclerview?.layoutManager = GridLayoutManager(activity!!, 3)

        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent,PICK_PROFILE_FROM_ALBUM)
        }

        getProfileImage()
        getFollowerAndFollowing()
        return fragmentView
    }
    fun getFollowerAndFollowing(){
        firestore?.collection("users")?.document(currentUserUid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if(followDTO?.followingCount != null){
                fragmentView?.account_tv_following_count?.text = followDTO?.followingCount?.toString()
            }
            if(followDTO?.followerCount != null){
                fragmentView?.account_tv_follower_count?.text = followDTO?.followerCount?.toString()
                if(followDTO?.followers?.containsKey(currentUserUid!!)){
                    fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow_cancel)
                    fragmentView?.account_btn_follow_signout?.background
                        ?.setColorFilter(ContextCompat.getColor(activity!!,R.color.grey_500),PorterDuff.Mode.MULTIPLY)
                }else{
                    if(uid != currentUserUid){
                        fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
                        fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                    }

                }
            }
        }
    }
    fun requestFollow(){
        //Save data to my account
        var tsDocFollowing = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followings[uid!!] = true

                transaction.set(tsDocFollowing,followDTO!!)
                return@runTransaction
            }

            if(followDTO.followings.containsKey(uid)){
                //It remove following third person when a third person follow me
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followings.remove(uid)
            }else{
                //It add following third person when a third person do not follow me
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followings[uid!!] = true
            }
            transaction.set(tsDocFollowing,followDTO)
            return@runTransaction
        }

        //Save data to third account

        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
                transaction.set(tsDocFollower,followDTO!!)
                return@runTransaction
            }

            if(followDTO!!.followers.containsKey(currentUserUid!!)){
                //It cancel my follower when I follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)
            }else{
                //It add my follower when I don't follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollower,followDTO!!)
            return@runTransaction
        }
    }
    fun followerAlarm(destinationUid : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }
    fun getProfileImage(){
        Log.d(TAG, "getProfileImage UID : " + uid)
        // document ???ｋ뒗 parameter uid濡??ㅼ젙???댁쑀..!!? -> ?쇰떒 currentUserUid濡?諛붽퓭?볦쓬
        firestore?.collection("profileImages")?.document(currentUserUid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener
            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!)
            }
        }
    }

    fun deleteThePost(pos: Int) {
        firestore?.collection("images")?.whereEqualTo("timestamp", contentDTOs[pos].timestamp)?.addSnapshotListener{
                querySnapshot, exception ->

            if (querySnapshot == null) return@addSnapshotListener

            for (snapshot in querySnapshot.documents) {
                contentDTOs.removeAt(pos)
                snapshot.reference.delete()
                fragmentView?.account_reyclerview?.adapter?.notifyDataSetChanged()
            }
        }
    }

    fun showConfirmMessage(pos: Int) {
        var dialog = AlertDialog.Builder(activity)
            .setMessage("Do you want to delete this post?")
            .setPositiveButton("OK", DialogInterface.OnClickListener{
                    dialog, which -> deleteThePost(pos)
                Toast.makeText(context, "Delete!", Toast.LENGTH_SHORT).show()
            }).create().show()

    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        //        var contentDTOs : ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.whereEqualTo("uid",uid)?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                //Sometimes, This code return null of querySnapshot when it signout
                contentDTOs.clear()
                contentSnapshotId.clear()

                if(querySnapshot == null) return@addSnapshotListener

                //Get data
                for(snapshot in querySnapshot.documents){
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    contentSnapshotId.add(snapshot.id)
                }
                fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageview = ImageView(p0.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)

            imageview.setOnLongClickListener {
                Log.d("Touch", "LONG PRESS IN")
                showConfirmMessage(p1)
                true
            }
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) : RecyclerView.ViewHolder(imageview) {
        }

        inner class Holder(itemView: View, itemClick: (ContentDTO) -> Unit) : RecyclerView.ViewHolder(itemView) {

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            val TAG = "Touch"
            var imageview = (p0 as CustomViewHolder).imageview
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }
    }
}