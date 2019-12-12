package com.example.s1.menuui

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.fragment_search.*
import com.google.gson.JsonParser
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class SearchFragment : Fragment() {
    var fragmentView : View? = null
    val TAG = "important"
    val PERMISSION_CODE  = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private val PICK_IMAGE_FROM_ALBUM_CODE = 1002
    var image_uri: Uri? = null
    var country: String? = "KRW"
    var country2: String? = "EUR"
    private var storage: FirebaseStorage? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_search,container,false)
        val translateButton = fragmentView?.findViewById<Button>(R.id.btn)
        val imgViewButton = fragmentView?.findViewById<ImageButton>(R.id.capture_btn)
        val money = fragmentView?.findViewById<TextView>(R.id.to)
        val spinner = fragmentView?.findViewById<Spinner>(R.id.from_country)
        val spinner2 = fragmentView?.findViewById<Spinner>(R.id.to_country)
//        val change = fragmentView?.findViewById<Button>(R.id.change_btn)
        val et = fragmentView?.findViewById<EditText>(R.id.from)
//        val testBtn = fragmentView?.findViewById<Button>(R.id.search_test_btn)
        val galleryBtn = fragmentView?.findViewById<ImageButton>(R.id.gallery_test_btn)

        storage = FirebaseStorage.getInstance()

        ArrayAdapter.createFromResource(
            this.context,
            R.array.my_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner!!.adapter = adapter
            spinner2!!.adapter = adapter
            spinner.setSelection(0)
            spinner2.setSelection(4)
        }
        spinner!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                country = resources.getStringArray(R.array.my_array)[position].split(" ")[1]
                if(et?.text!=null) {
                    GetExchangesTask().execute(country, country2)
                    money?.text = "wait..."
                }
//                Log.d("important", "country : $country")
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        spinner2!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                country2 = resources.getStringArray(R.array.my_array)[position].split(" ")[1]
                Log.d("important", "country : $country2")
                if(et?.text!=null) {
                    GetExchangesTask().execute(country, country2)
                    money?.text = "wait..."
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        et?.addTextChangedListener(object: TextWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if(et.text.toString().trim().isNotEmpty()) {
                    Log.d("important", "et.text string $et.text.toString()")
                    GetExchangesTask().execute(country, country2)
                    money?.text = "wait..."
                }
                else{
                    money?.text=" "
                }
            }

        })

        translateButton?.visibility = View.INVISIBLE
        imgViewButton?.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(this.context!!,android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(this.context!!,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                    //permission was not enabled
                    val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    //show popup to request permission
                    requestPermissions(permission,PERMISSION_CODE)
                }
                else{
                    //permission already granted
                    openCamera()
                }
            }
            else{
                //system os is < marshmallow
                openCamera()
            }
        }
        /*
        testBtn!!.setOnClickListener {
            var mango = "http://0ea7ad37.ngrok.io/image_download/20191206_191615_.png7c494ee3-fc6d-46ae-accf-8e1238ace952"
            Log.d(TAG, "mango : " + mango)
            PostUrlTask().execute(mango)
        }
         */

        galleryBtn!!.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM_CODE)
        }

        return fragmentView
    }

    private fun openCamera() {
        /*
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        val contentResolver = context?.getContentResolver()
        image_uri = contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)
        //camera intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
         */
        var takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity?.packageManager) != null) {
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_CODE)
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
                    openCamera()
//                    var intent = Intent(context,OpencvActivity::class.java)
//                    startActivity(intent)
                }
                else{
                    //permission from popup was denied
                    Toast.makeText(this.context,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //called when image was captured from camera intent
        val translateButton = fragmentView?.findViewById<Button>(R.id.btn)
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK){
            //set image captured to image view
            var extras = data?.extras
            var imageBitmap = extras?.get("data") as Bitmap
            image_view.setImageBitmap(imageBitmap)

            translateButton?.visibility = View.VISIBLE

            /*
 String path = String.valueOf(Environment.getExternalStorageDirectory()) + "/your_name_folder";
            try {
                File ruta_sd = new File(path);
                File folder = new File(ruta_sd.getAbsolutePath(), nameFol);
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdir();
                }
                if (success) {
                    Toast.makeText(MainActivity.this, "Carpeta Creada...", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Log.e("Carpetas", "Error al crear Carpeta a tarjeta SD");
            }

            Intent i = new Intent(MainActivity.this, MainActivity.class);
             */
            translateButton?.setOnClickListener {
                var baos = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val sendData = baos.toByteArray()
                var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                var imageFileName = "IMAGE_" + timestamp + "_.png"

                var storageRef = storage?.reference?.child("translation")?.child(imageFileName)

                storageRef?.putBytes(sendData)
                    ?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                        return@continueWithTask storageRef.downloadUrl
                    }?.addOnSuccessListener { uri ->
                        Log.d(TAG, "success" + uri.toString())
                        var url = uri.toString()
                        var str = "http://497914d4.ngrok.io/image_download/"
                        var arr = url.split("_")
                        str += arr[1] + "_" + arr[2] + "_.png"
                        str += url.split("=")[2]
                        Log.d(TAG, "Sending URL: " + str)
                        var mango = "http://d9809da3.ngrok.io/image_download/20191206_191615_.png7c494ee3-fc6d-46ae-accf-8e1238ace952"
//                        Log.d(TAG, "mango : " + mango)
//                        PostUrlTask().execute(mango)
                        PostUrlTask().execute(str)
                    }?.addOnFailureListener {
                        Log.d(TAG, "failure")
                    }
            }
        }
        else if (requestCode == PICK_IMAGE_FROM_ALBUM_CODE && resultCode == Activity.RESULT_OK) {
            var photoUri = data?.data
            image_view.setImageURI(photoUri)
            Log.d(TAG, "Gallery : " + photoUri)

            translateButton?.visibility = View.VISIBLE
            translateButton?.setOnClickListener{
                Log.d(TAG, "PHOTO URI : " + photoUri?.toString())
                uploadMenuPhotoToFirebase(photoUri!!)
            }
        }
    }

    fun uploadMenuPhotoToFirebase(uploadUri: Uri) {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        var storageRef = storage?.reference?.child("freeboard")?.child(imageFileName)

        storageRef?.putFile(uploadUri)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { uri ->
            var url = formatUriToServerAddress(uri.toString())
            Log.d(TAG, "GALLERY: " + url)
            PostUrlTask().execute(url)
        }

    }

    private fun formatUriToServerAddress(uri_str: String): String{
        var str = "http://497914d4.ngrok.io/image_download/"
        var arr = uri_str.split("_")
        str += arr[1] + "_" + arr[2] + "_.png"
        str += uri_str.split("=")[2]

        return str
    }

    private inner class GetExchangesTask : AsyncTask<String, Void, Double>() {
        override fun doInBackground(vararg params: String?): Double {
            var str = "https://api.exchangerate-api.com/v4/latest/" + params[0]
            var url = URL(str)
            var con: HttpURLConnection = url.openConnection() as HttpURLConnection
            var req_result = 0.0

            try {
                val jp = JsonParser()
                val root = jp.parse(InputStreamReader(con.content as InputStream))
                val jsonobj = root.asJsonObject
                req_result = jsonobj.get("rates").asJsonObject.get(params[1]).asDouble
            }catch (e: Exception) {
                Log.d("important", "Connection Error : $e")
            }finally {
                con.disconnect()
            }
            return req_result
        }

        override fun onPostExecute(result: Double?) {
            super.onPostExecute(result)
            var money = from.text.toString().toDouble()
            var exchange = money * result!!
//            Log.d("important", "Before Exchange : $money")
//            Log.d("important", "After Exchange : $exchange")
            to.setText(exchange.toString())
        }
    }

    private inner class PostUrlTask : AsyncTask<String, Void, String>() {
        var stringResult: ArrayList<String> = arrayListOf()

        override fun doInBackground(vararg params: String?): String {
            var url = URL(params[0])
            Log.d(TAG, "final url : " + url.toString())
            var message = "Post success"
            var con: HttpURLConnection = url.openConnection() as HttpURLConnection
            try {
                Log.d(TAG, con.responseMessage)
                val jp = JsonParser()
                val root = jp.parse(InputStreamReader(con.content as InputStream))
                val resultArray = root.asJsonArray
                var cnt = 0
                Log.d(TAG, root.toString())

                for (result in resultArray) {
                    val tmp = result.asJsonArray
                    Log.d(TAG, "explanation : ${tmp[3]}")
                    if (tmp[3].toString() == "" && tmp[3].toString() == " ") {
                        Log.d(TAG, "${tmp[0]} is null")
                    }
                    var str = tmp[0].toString() + "," + tmp[2].toString() + "," + tmp[1].toString() + "," + tmp[3].toString()
                    str = str.replace("\"", "")
                    Log.d(TAG, str)
                    stringResult.add(str)
//                    Log.d(TAG, cnt.toString() + ": " + tmp[0])
                }
                for (s in stringResult) {
                    Log.d(TAG, cnt.toString() + ": " + s)
                }
                Log.d(TAG, stringResult.toString())
            }catch (e: Exception) {
                message = "connection failed"
                Log.d(TAG, "PostUrlTask Connection Error : " + e.toString())
            }finally {
                con.disconnect()
            }
            return message
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            /*
            AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(result)
                .setPositiveButton("ok", null).create().show()
             */

            if (stringResult != null) {
                var intent = Intent(context, TranslationActivity::class.java)
                intent.putStringArrayListExtra("result", stringResult)
                startActivity(intent)
            }
        }
    }
}