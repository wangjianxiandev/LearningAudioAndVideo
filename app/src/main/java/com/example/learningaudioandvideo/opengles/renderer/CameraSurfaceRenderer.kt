package com.example.learningaudioandvideo.opengles.renderer

import android.graphics.Camera
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.example.learningaudioandvideo.R
import com.example.learningaudioandvideo.utils.ResReadUtils
import com.example.learningaudioandvideo.utils.ShaderUtils
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/1/31 21:46
 */
class CameraSurfaceRenderer : GLSurfaceView.Renderer {

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var mTexVertexBuffer: FloatBuffer

    private lateinit var mVertexIndexBuffer: ShortBuffer

    private var mProgram = 0

    private val textureId = 0

    private val transformMatrix = FloatArray(16)

    /**
     * 渲染容器
     */
    private val mGLSurfaceView: GLSurfaceView? = null

    /**
     * 相机ID
     */
    private val mCameraId = 0

    /**
     * 相机实例
     */
    private val mCamera: Camera? = null

    /**
     * Surface
     */
    private var mSurfaceTexture: SurfaceTexture? = null

    /**
     * 矩阵索引
     */
    private val uTextureMatrixLocation = 0

    private val uTextureSamplerLocation = 0

    /**
     * 顶点坐标
     * (x,y,z)
     */
    private val POSITION_VERTEX = floatArrayOf(
        0f, 0f, 0f,  //顶点坐标V0
        1f, 1f, 0f,  //顶点坐标V1
        -1f, 1f, 0f,  //顶点坐标V2
        -1f, -1f, 0f,  //顶点坐标V3
        1f, -1f, 0f //顶点坐标V4
    )

    /**
     * 纹理坐标
     * (s,t)
     */
    private val TEX_VERTEX = floatArrayOf(
        0.5f, 0.5f,  //纹理坐标V0
        1f, 1f,  //纹理坐标V1
        0f, 1f,  //纹理坐标V2
        0f, 0.0f,  //纹理坐标V3
        1f, 0.0f //纹理坐标V4
    )

    /**
     * 索引
     */
    private val VERTEX_INDEX = shortArrayOf(
        0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
        0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
        0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
        0, 4, 1 //V0,V4,V1 三个顶点组成一个三角形
    )

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        //使用程序片段
        GLES30.glUseProgram(mProgram);

        //更新纹理图像
        mSurfaceTexture?.updateTexImage();
        mSurfaceTexture?.getTransformMatrix(transformMatrix);

        //激活纹理单元0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //绑定外部纹理到纹理单元0
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        //将此纹理单元床位片段着色器的uTextureSampler外部纹理采样器
        GLES30.glUniform1i(uTextureSamplerLocation, 0);

        //将纹理矩阵传给片段着色器
        GLES30.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer);

        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, mTexVertexBuffer);

        // 绘制
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.size, GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //设置背景颜色
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);
        //编译
        val vertexShaderId = ShaderUtils.compileVertexShader(ResReadUtils.readResource(R.raw.vertex_camera_shader));
        val fragmentShaderId = ShaderUtils.compileFragmentShader(ResReadUtils.readResource(R.raw.fragment_camera_shader));
        //链接程序片段
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)

        uTextureMatrixLocation = GLES30.glGetUniformLocation(mProgram, "uTextureMatrix");
        //获取Shader中定义的变量在program中的位置
        uTextureSamplerLocation = GLES30.glGetUniformLocation(mProgram, "yuvTexSampler");

        //加载纹理
        textureId = loadTexture();
        //加载SurfaceTexture
        loadSurfaceTexture(textureId);
    }

    fun loadSurfaceTexture(textureId: Int): Boolean {
        //根据纹理ID创建SurfaceTexture
        mSurfaceTexture = SurfaceTexture(textureId)
        mSurfaceTexture?.setOnFrameAvailableListener(OnFrameAvailableListener { mGLSurfaceView!!.requestRender() })
        //设置SurfaceTexture作为相机预览输出
        try {
            mCamera?.setPreviewTexture(mSurfaceTexture)
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        //开启相机预览
        mCamera.startPreview()
        return true
    }

    fun loadTexture(): Int {
        val tex = IntArray(1)
        //创建一个纹理
        GLES30.glGenTextures(1, tex, 0)
        //绑定到外部纹理上
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        //设置纹理过滤参数
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_NEAREST.toFloat()
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES30.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE.toFloat()
        )
        //解除纹理绑定
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return tex[0]
    }
}