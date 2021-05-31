package com.harasio.savemeapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PingData(
    var kejahatan: String,
    var lat: String,
    var long: String,
    var time: String
) : Parcelable
