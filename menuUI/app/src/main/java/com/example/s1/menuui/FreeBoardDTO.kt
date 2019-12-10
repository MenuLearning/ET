package com.example.s1.menuui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FreeBoardDTO(var uid : String? = null,
                        var userId : String? = null,
                        var imgUrl : String? = null,
                        var timestamp: Long? = null,
                        var location : String? = null,
                        var menu : String? = null,
                        var review : String? = null,
                        var rating : Float = 0F,
                        var thumbsCount : Int = 0,
                        var favorites : MutableMap<String,Boolean> = HashMap(),
                        var thumbs_up : MutableMap<String,Boolean> = HashMap(),
                        var thumbs_down : MutableMap<String,Boolean> = HashMap()):Parcelable