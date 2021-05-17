package com.harasio.savemeapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var fullname: String? = null,
    var email: String? = null,
    var password: String? = null
): Parcelable {
    constructor() : this("", "", "") {

    }
}
