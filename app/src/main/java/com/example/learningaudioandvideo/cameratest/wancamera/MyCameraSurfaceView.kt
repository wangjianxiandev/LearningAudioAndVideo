package com.example.wancamera

import android.content.Context
import android.hardware.Camera
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/2/17 23:04
 */
class MyCameraSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback,
    Camera.PreviewCallback {
    private var mCamera: Camera? = null
    private var mHolder: SurfaceHolder? = null
    private var mICaptureDataCallback: ICaptureDataCallback? = null

    init {
        mHolder = holder
        mHolder!!.addCallback(this)
    }

    fun exitCamera() {
        mCamera!!.release()
    }

    //SurfaceHolder.Callback
    override fun surfaceCreated(holder: SurfaceHolder?) {
        mCamera = Camera.open()
        try {
            mCamera!!.setPreviewDisplay(holder)
            mCamera!!.setPreviewCallback(this)
            mCamera!!.setDisplayOrientation(90)
            mCamera!!.startPreview()
        } catch (e: IOException) {
            mCamera!!.release()
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(
        holder: SurfaceHolder?,
        format: Int,
        width: Int,
        height: Int
    ) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mCamera!!.stopPreview()
        mCamera!!.release()
        mCamera = null
    }

    override fun onPreviewFrame(
        data: ByteArray?,
        camera: Camera?
    ) {
        synchronized(this) {
            if (mICaptureDataCallback != null) {
                mICaptureDataCallback!!.onPreviewCaptured(data!!, mCamera!!)
            }
        }
        mCamera!!.addCallbackBuffer(data)
    }

    fun setICaptureDataCallback(ICaptureDataCallback: ICaptureDataCallback?) {
        mICaptureDataCallback = ICaptureDataCallback
    }

}