package com.example.wancamera.util

import android.opengl.GLES20
import android.util.Log


/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/2/17 22:48
 */
object OpenGLUtils {
    private const val TAG = "OpenGLUtils"
    fun loadShader(strSource: String?, iType: Int): Int {
        val compiled = IntArray(1)
        val iShader = GLES20.glCreateShader(iType)
        GLES20.glShaderSource(iShader, strSource)
        GLES20.glCompileShader(iShader)
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.d(
                TAG, """
     Compilation
     ${GLES20.glGetShaderInfoLog(iShader)}
     """.trimIndent()
            )
            return 0
        }
        return iShader
    }

    fun loadProgram(strVSource: String?, strFSource: String?): Int {
        val iVShader: Int
        val iFShader: Int
        val iProgId: Int
        val link = IntArray(1)
        iVShader = loadShader(strVSource, GLES20.GL_VERTEX_SHADER)
        if (iVShader == 0) {
            Log.d(TAG, "Vertex Shader Failed")
            return 0
        }
        iFShader = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER)
        if (iFShader == 0) {
            Log.d(TAG, "Fragment Shader Failed")
            return 0
        }
        iProgId = GLES20.glCreateProgram()
        GLES20.glAttachShader(iProgId, iVShader)
        GLES20.glAttachShader(iProgId, iFShader)
        GLES20.glLinkProgram(iProgId)
        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0)
        if (link[0] <= 0) {
            Log.d(TAG, "Linking Failed")
            return 0
        }
        GLES20.glDeleteShader(iVShader)
        GLES20.glDeleteShader(iFShader)
        return iProgId
    }
}