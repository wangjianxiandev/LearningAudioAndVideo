package com.example.wancamera

import android.hardware.Camera


/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/2/17 22:41
 */
interface ICaptureDataCallback {
    fun onPreviewCaptured(data : ByteArray, camera : Camera)
}