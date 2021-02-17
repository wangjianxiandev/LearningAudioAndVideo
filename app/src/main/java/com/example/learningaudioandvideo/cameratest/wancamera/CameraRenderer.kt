package com.example.wancamera

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.wancamera.util.OpenGLUtils
import com.example.wancamera.util.TextureUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/2/17 22:45
 */

class CameraRenderer : GLSurfaceView.Renderer {
    private var mProgram = 0
    private val getFrameData = false
    var positionHandle = 0
    private var texCoordHandle: Int = 0
    private var textureYHandle: Int = 0
    private var textureUVHandle: Int = 0
    private val textureId = -1
    var id_y = IntArray(1)
    var id_uv = IntArray(1)
    val vertexBuffer: FloatBuffer? = null
    private val textureBuffer: FloatBuffer? = null
    private var yuvBuffer: ByteBuffer? = null

    private var frameWidth = 0
    private var frameHeight: Int = 0

    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "attribute vec2 aTexCoord;" +
            "varying vec2 vTextureCoord;" +
            "void main() {" +
            "gl_Position = vPosition;" +
            "vTextureCoord = aTexCoord;" +
            "}"
    private val fragmentShaderCode = "precision mediump float;" +
            "varying vec2 vTextureCoord;" +
            "uniform sampler2D sTexture;" +
            "void main() {" +
            "vec2 flipped_texcoord = vec2(vTextureCoord.x, 1.0 - vTextureCoord.y);" +
            "gl_FragColor = texture2D(sTexture, flipped_texcoord);" +  //贴图相反
            "}"


    private val fragmentShaderCode2 = "precision mediump float;" +
            "uniform sampler2D mGLUniformTexture;" +
            "uniform sampler2D mGLUniformTexture1;" +
            "varying highp vec2 vTextureCoord;" +
            "const mat3 yuv2rgb = mat3(" +
            "1, 0, 1.2802," +
            "1, -0.214821, -0.380589," +
            "1, 2.127982, 0" +
            ");" +
            "void main() {" +
            "vec2 flippedTexCoord = vec2(1.0 - vTextureCoord.x ,vTextureCoord.y);" +
            "vec3 yuv = vec3(" +
            "1.1643 * (texture2D(mGLUniformTexture, flippedTexCoord).r - 0.0625)," +
            "texture2D(mGLUniformTexture1, flippedTexCoord).a - 0.5," +
            "texture2D(mGLUniformTexture1, flippedTexCoord).r - 0.5" +
            ");" +
            "vec3 rgb = yuv * yuv2rgb;" +
            "gl_FragColor = vec4(rgb, 1);" +
            "}"

    fun CameraRenderer() {}

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        //背景颜色
        GLES20.glClearColor(0.1f, 0.5f, 0.5f, 0.5f)

        //启动纹理
        GLES20.glEnable(GLES20.GL_TEXTURE_2D)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        mProgram = OpenGLUtils.loadProgram(vertexShaderCode, fragmentShaderCode2)

        //获取指向vertex shander 的成员vPosition的handle
        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        texCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord")
        textureYHandle = GLES20.glGetUniformLocation(mProgram, "mGLUniformTexture")
        textureUVHandle = GLES20.glGetUniformLocation(mProgram, "mGLUniformTexture1")
        GLES20.glUseProgram(mProgram)
        GLES20.glUniform1i(textureYHandle, 0)
        GLES20.glUniform1i(textureUVHandle, 1)

        //启用一个指向三角形的顶点数组的handle
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer)
        GLES20.glVertexAttribPointer(
            texCoordHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            2 * 4,
            textureBuffer
        )

        //创建纹理
        TextureUtils.createTexture(320, 240, GLES20.GL_LUMINANCE, id_y)
        TextureUtils.createTexture(320, 240, GLES20.GL_LUMINANCE_ALPHA, id_uv)

//        InputStream ins = null;
//        try {
//            ins = mContext.getAssets().open("luna.jpg");
//            textureId = TextureUtils.loadTexture(ins);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // 重绘背景色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        if (yuvBuffer != null) {
            yuvBuffer!!.position(0)
            yuvBuffer!!.limit(frameWidth * frameHeight - 1)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id_y[0])
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_LUMINANCE,
                frameWidth,
                frameHeight,
                0,
                GLES20.GL_LUMINANCE,
                GLES20.GL_UNSIGNED_BYTE,
                yuvBuffer
            )
            yuvBuffer!!.limit(frameWidth * frameHeight * 3 / 2 - 1)
            yuvBuffer!!.position(frameWidth * frameHeight)
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id_uv[0])
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_LUMINANCE_ALPHA,
                frameWidth / 2,
                frameHeight / 2,
                0,
                GLES20.GL_LUMINANCE_ALPHA,
                GLES20.GL_UNSIGNED_BYTE,
                yuvBuffer
            )
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }
    }

    public fun changeYUVFrame(data: ByteArray?, width: Int, height: Int) {
        frameWidth = width
        frameHeight = height
        yuvBuffer = ByteBuffer.allocateDirect(width * height * 3 / 2)
        yuvBuffer!!.order(ByteOrder.nativeOrder())
        yuvBuffer!!.put(data)
    }
}