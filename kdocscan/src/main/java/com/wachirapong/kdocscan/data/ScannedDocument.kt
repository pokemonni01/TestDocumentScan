package com.wachirapong.kdocscan.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ScannedDocument(
    val imageAbsolutePath: String,
    val listDocumentPoint: List<DocumentPoint>
): Parcelable