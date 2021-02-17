package com.example.wancamera.util

import android.graphics.BitmapFactory
import android.hardware.Camera
import android.opengl.GLES20
import android.opengl.GLUtils
import java.io.InputStream
import java.nio.IntBuffer


/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/2/17 22:49
 */
object TextureUtils {
    private const val TAG = "TextureUtils"

    //InputStream数据
    fun loadTexture(ins: InputStream?): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0) //生成一个纹理
        val textureId = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        GLES20.glTexParameterf(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE.toFloat()
        )
        //上面是纹理贴图的取样方式，包括拉伸方式，取临近值和线性值
        val bitmap = BitmapFactory.decodeStream(ins)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0) //让图片和纹理关联起来，加载到OpenGl空间中
        bitmap.recycle() //不需要，可以释放
        return textureId
    }

    //RGBA数据
    fun loadTexture(data: IntBuffer?, size: Camera.Size, usedTexId: Int): Int {
        val textures = IntArray(1)
        if (usedTexId == 0) { //NO_TEXTURE
            GLES20.glGenTextures(1, textures, 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexParameterf(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat()
            )
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, size.width, size.height,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data
            )
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId)
            GLES20.glTexSubImage2D(
                GLES20.GL_TEXTURE_2D, 0, 0, 0, size.width,
                size.height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data
            )
            textures[0] = usedTexId
        }
        return textures[0]
    }

    fun createTexture(
        width: Int,
        height: Int,
        format: Int,
        textureId: IntArray
    ) {
        //创建纹理
        GLES20.glGenTextures(1, textureId, 0)
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
        //设置纹理属性
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            format,
            width,
            height,
            0,
            format,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )
    }
}