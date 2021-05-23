package com.harasio.savemeapp

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var fullname: String? = null,
    var email: String? = null,
    var password: String? = null,
    var umur: String? = null,
    var gender: String? = null,
    var alamat: String? = null,
    var kota: String? = null,
    var provinsi: String? = null,
    var zipcode: String? = null

): Parcelable {
    constructor() : this("", "", "") {

    }
}
