package com.example.learningaudioandvideo.opengles

import android.graphics.Color
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.learningaudioandvideo.opengles.renderer.ColorRenderer
import com.example.learningaudioandvideo.opengles.renderer.SimpleRenderer

class RenderActivity : AppCompatActivity() {
    private lateinit var mGLSurfaceView : GLSurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
    }

    private fun initViews() {
        mGLSurfaceView = GLSurfaceView(this)
        setContentView(mGLSurfaceView)
        mGLSurfaceView.setEGLContextClientVersion(3)
//        var renderer = ColorRenderer(Color.GREEN)
        var renderer = SimpleRenderer()
        mGLSurfaceView.setRenderer(renderer)
    }
}