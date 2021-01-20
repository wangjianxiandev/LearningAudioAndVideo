package com.example.learningaudioandvideo.utils

import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.util.Log
import java.io.ByteArrayOutputStream


/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/1/21 0:22
 */
class PreviewToBitmap {
    /**
     * 预览回调
     */
    private val previewCallback: Camera.PreviewCallback = object : Camera.PreviewCallback() {
        fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
            val size: Camera.Size = camera.getParameters().getPreviewSize()
            try {
                val image =
                    YuvImage(data, ImageFormat.NV21, size.width, size.height, null)
                if (image != null) {
                    val stream = ByteArrayOutputStream()
                    image.compressToJpeg(Rect(0, 0, size.width, size.height), 80, stream)
                    val bmp =
                        BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
                    //TODO：此处可以对位图进行处理，如显示，保存等
                    stream.close()
                }
            } catch (ex: Exception) {
                Log.e("WJX", "Error:" + ex.message)
            }
        }
    }
}