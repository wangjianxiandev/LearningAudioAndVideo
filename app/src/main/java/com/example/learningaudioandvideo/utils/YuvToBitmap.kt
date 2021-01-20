package com.example.learningaudioandvideo.utils

import android.graphics.*
import android.util.Size
import java.io.ByteArrayOutputStream

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/1/21 0:24
 */
object YuvToBitmap {
    fun YuvToBitmap(data: ByteArray, size: Size) {
        val yuvImage = YuvImage(data, ImageFormat.NV21, size.width, size.height, null)
        if (yuvImage != null) {
            var byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, size.width, size.height), 80, byteArrayOutputStream)
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(
                byteArrayOutputStream.toByteArray(),
                0,
                byteArrayOutputStream.size()
            )
            byteArrayOutputStream.close()
        }
    }
}