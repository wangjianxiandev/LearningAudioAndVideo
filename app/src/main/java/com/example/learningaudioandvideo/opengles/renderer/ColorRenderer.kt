package com.example.learningaudioandvideo.opengles.renderer

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/1/31 19:37
 */
class ColorRenderer (val color : Int) : GLSurfaceView.Renderer {
    private var mColor : Int
    init {
        this.mColor = color
    }

    override fun onDrawFrame(gl: GL10?) {
        // 将颜色色缓冲区设置为预设的颜色
        GLES30.glClear(GL10.GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // 设置视图窗口
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 设置背景颜色
        GLES30.glClearColor(255f, 0f, 0f, 100f)
    }
}