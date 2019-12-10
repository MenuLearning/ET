package com.example.s1.menuui

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_add_post.*
import java.text.SimpleDateFormat
import java.util.*

class AddPostActivitiy: AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    val REGISTER_CURRENT_ADDRESS = 1
    val PERMISSION_CODE  = 1000
    var photoUri : Uri? = null
    var firestore : FirebaseFirestore? = null
    var auth : FirebaseAuth? = null
    var storage : FirebaseStorage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        setSupportActionBar(toolbar)

        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayHomeAsUpEnabled(true)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // add Photo
        freeboard_add_image_btn.setOnClickListener {
            Toast.makeText(baseContext, "Add Image Btn", Toast.LENGTH_SHORT).show()
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)
        }

        // find location
        freeboard_location_btn.setOnClickListener {
            Toast.makeText(baseContext, "Find Location", Toast.LENGTH_SHORT).show()

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                var intent = Intent(this, LocationActivity::class.java)
                startActivityForResult(intent, REGISTER_CURRENT_ADDRESS)
            }
            else {
                val permission = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                //show popup to request permission
                requestPermissions(permission,PERMISSION_CODE)
                Toast.makeText(this, "GPS Permission Denied", Toast.LENGTH_SHORT).show()
            }

        }

        freeboard_add_btn.setOnClickListener {
            if (photoUri == null) {
                AlertDialog.Builder(this)
                    .setTitle("Warning")
                    .setMessage(getString(R.string.image_null)).create().show()
            }
            else {
                contentUpload()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0  && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup was granted
                    var intent = Intent(this, LocationActivity::class.java)
                    startActivityForResult(intent, REGISTER_CURRENT_ADDRESS)
                }
                else{
                    //permission from popup was denied
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun contentUpload() {
        //Make filename
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("freeboard")?.child(imageFileName)

        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var newBoard = FreeBoardDTO()
            newBoard.location = freeboard_restaurant_edit_tv.text.toString()
            newBoard.menu = freeboard_menu_edit_tv.text.toString()
            newBoard.review = freeboard_review_edit_tv.text.toString()
            newBoard.rating = freeboard_rating_bar.rating
            newBoard.uid = auth?.currentUser?.uid
            newBoard.userId = auth?.currentUser?.email
            newBoard.timestamp = System.currentTimeMillis()
            newBoard.imgUrl = uri.toString()

            firestore?.collection("freeboard")?.document()?.set(newBoard)
                ?.addOnSuccessListener {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                ?.addOnFailureListener {
                    Toast.makeText(this, getString(R.string.upload_fail), Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if(resultCode == Activity.RESULT_OK){
                //This is path to the selected image
                photoUri = data?.data
                freeboard_new_iv.setImageURI(photoUri)
            } else{
                finish()
            }
        }
        else if(requestCode == REGISTER_CURRENT_ADDRESS) {
            if (resultCode == Activity.RESULT_OK) {
                val address = data?.getStringExtra("address")
                Log.d("BLUEMING", address)
                freeboard_restaurant_edit_tv.setText(address)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}