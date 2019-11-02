package com.example.s1.menuui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CommentDTO(var uid : String? = null,
                   var userId : String? = null,
                   var comment : String? = null,
                   var timestamp : Long? = null): Parcelable