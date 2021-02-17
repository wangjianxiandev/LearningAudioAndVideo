package com.example.wancamera

import android.R.attr
import android.content.Context
import android.hardware.Camera

import android.opengl.GLSurfaceView


/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/2/17 22:39
 */

class MyGLSurfaceView(context: Context) : GLSurfaceView(context), ICaptureDataCallback{
    private lateinit var mRenderer: CameraRenderer

    init {
        mRenderer = CameraRenderer()
        setRenderer(mRenderer)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun onPreviewCaptured(data: ByteArray, camera: Camera) {
        val width: Int = camera.getParameters().getPreviewSize().width
        val height: Int = camera.getParameters().getPreviewSize().height
        mRenderer.changeYUVFrame(data, width, height)
        requestRender()
    }
}