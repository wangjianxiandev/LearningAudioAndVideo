package com.example.learningaudioandvideo.opengles.renderer

import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.example.learningaudioandvideo.utils.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/1/31 19:58
 */
class SimpleRenderer : GLSurfaceView.Renderer {
    private val POSITION_COMPONENT_COUNT = 3

    private lateinit var vertexBuffer: FloatBuffer

    private lateinit var colorBuffer: FloatBuffer

    private var mProgram = 0

    /**
     * 点的坐标
     */
    private val vertexPoints = floatArrayOf(
        0.0f, 0.5f, 0.0f,
        -1f, -0.5f, 0.0f,
        1f, -0.5f, 0.0f
    )

    /**
     * 顶点着色器
     * 第一行表示：着色器的版本，OpenGL ES 2.0版本可以不写。
     * 第二行表示：输入一个名为vPosition的4分量向量，layout (location = 0)表示这个变量的位置是顶点属性0。
     * 第三行表示：输入一个名为aColor的4分量向量，layout (location = 1)表示这个变量的位置是顶点属性1。
     * 第四行表示：输出一个名为vColor的4分量向量
     * 第八行表示：将输入数据aColor拷贝到vColor的变量中。
     */
    private val vertextShader = """
        #version 300 es 
        layout (location = 0) in vec4 vPosition;
        layout (location = 1) in vec4 aColor;
        out vec4 vColor;
        void main() { 
        gl_Position  = vPosition;
        gl_PointSize = 10.0;
        vColor = aColor;
        }
        
        """.trimIndent()

    private val fragmentShader = """
        #version 300 es 
        precision mediump float;
        in vec4 vColor;
        out vec4 fragColor;
        void main() { 
        fragColor = vColor; 
        }

        """.trimIndent()

    private val color = floatArrayOf(
        0.0f, 1.0f, 0.0f, 1.0f,
        1.0f, 0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f, 1.0f
    )

    init {
        // 分配内存空间，每个浮点型占4字节空间
        vertexBuffer = ByteBuffer.allocateDirect(vertexPoints.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        // 传入指定的坐标数据
        vertexBuffer.put(vertexPoints)
        vertexBuffer.position(0)

        colorBuffer = ByteBuffer.allocateDirect(color.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        //传入指定的数据
        colorBuffer.put(color)
        colorBuffer.position(0);
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        // 准备坐标数据
        GLES30.glVertexAttribPointer(
            0, POSITION_COMPONENT_COUNT, GLES30.GL_FLOAT, false
            , 0, vertexBuffer
        )
        // 启用顶点句柄，允许顶点着色器读取GPU（服务器端）数据
        GLES30.glEnableVertexAttribArray(0)

        // 绘制三角形颜色
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, 0, colorBuffer)

//        GL_POINTS	点精灵图元，对指定的每个顶点进行绘制
//        GL_LINES	绘制一系列不相连的线段
//        GL_LINE_STRIP	绘制一系列相连的线段
//        GL_LINE_LOOP	绘制一系列相连的线段，首尾相连
//        GL_TRIANGLES	绘制一系列单独的三角形
//        GL_TRIANGLE_STRIP	绘制一系列相互连接的三角形
//        GL_TRIANGLE_FAN	绘制一系列相互连接的三角形
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

        // 禁止顶点数组的句柄
        GLES30.glDisableVertexAttribArray(0)
        GLES30.glDisableVertexAttribArray(1);

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)
        val vertexShaderId = ShaderUtils.compileVertexShader(vertextShader)
        val fragmentShaderId = ShaderUtils.compileFragmentShader(fragmentShader)
        // 链接程序片段
        mProgram = ShaderUtils.linkProgram(vertexShaderId, fragmentShaderId)
        // 在GL3.0中使用该程序片段
        GLES30.glUseProgram(mProgram)
    }

}