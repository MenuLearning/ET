package com.example.s1.menuui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QnADTO (var uid : String? = null,
                   var userId : String? = null,
                   var timestamp: Long? = null,
                   var title : String? = null,
                   var question : String? = null,
                   var viewCount : Int = 0
                   /*var comments : CommentDTO? = null*/):Parcelable