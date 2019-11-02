package com.example.s1.menuui

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ContentDTO(var explain : String? = null,
                      var imageUrl : String? = null,
                      var uid : String? = null,
                      var userId : String? = null,
                      var timestamp : Long? = null,
                      var favoriteCount : Int = 0,
                      var favorites : MutableMap<String,Boolean> = HashMap(),
                      var comments : CommentDTO? = null):Parcelable //{
/*    @Parcelize
    data class Comment(var uid : String? = null,
                       var userId : String? = null,
                       var comment : String? = null,
                       var timestamp : Long? = null):Parcelable */
//}