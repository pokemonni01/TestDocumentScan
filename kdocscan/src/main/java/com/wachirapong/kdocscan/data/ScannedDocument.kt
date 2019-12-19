package com.wachirapong.kdocscan.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size

@Parcelize
data class ScannedDocument(
    val imageUri: Uri,
    val listDocumentPoint: List<DocumentPoint>
): Parcelable