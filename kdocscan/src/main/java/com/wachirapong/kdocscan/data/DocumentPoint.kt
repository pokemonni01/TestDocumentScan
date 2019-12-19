package com.wachirapong.kdocscan.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DocumentPoint(val x: Double = 0.0, val y: Double = 0.0): Parcelable