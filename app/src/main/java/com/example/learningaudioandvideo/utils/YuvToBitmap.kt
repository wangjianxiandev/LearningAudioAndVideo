package com.example.learningaudioandvideo.utils

import android.content.Context
import android.graphics.*
import android.os.Environment
import android.util.Log
import android.util.Size
import java.io.*
import java.nio.ByteBuffer

/**
 * Created with Android Studio.
 * Description: Yuv convert to bitmap and save it to file
 * @author: Wangjianxian
 * @CreateDate: 2021/1/21 0:24
 */
object YuvToBitmap {
    fun YuvToBitmap(data: ByteBuffer, size: Size, context : Context) {
        val dirPath = File(Environment.getExternalStorageDirectory(), "wjx")
        if (!dirPath.exists()) {
            dirPath.mkdir()
        }
        val fileName = System.currentTimeMillis().toString() + ".jpeg"
        val file = File(dirPath, fileName)
        try {
            val fos = FileOutputStream(file)
            val jpegBytes = ByteArray(data.remaining())
            val yuvImage = YuvImage(jpegBytes, ImageFormat.NV21, size.width, size.height, null)
            if (yuvImage != null) {
                var byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()
                yuvImage.compressToJpeg(Rect(0, 0, size.width, size.height), 80, byteArrayOutputStream)
                BitmapFactory.decodeByteArray(
                    byteArrayOutputStream.toByteArray(),
                    0,
                    byteArrayOutputStream.size()
                ).compress(Bitmap.CompressFormat.JPEG, 50, fos)
                fos.flush()
                fos.close()
            }
        } catch (e : FileNotFoundException) {
            Log.e("WJX", "file is not found")
        } catch (e : IOException) {
            Log.e("WJX", "Io exception")
        }
    }
}