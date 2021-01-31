package com.example.learningaudioandvideo.utils

import android.content.Context
import android.content.res.Resources
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/1/31 21:53
 */
object ResReadUtils {

    /**
     * 读取资源
     *
     * @param resourceId
     * @return
     */
    fun readResource(resourceId : Int, context: Context): String{
        val builder = StringBuilder();
        try {
            val inputStream = context.getResources().openRawResource(resourceId);
            val streamReader = InputStreamReader(inputStream);

            val bufferedReader = BufferedReader(streamReader);
            while (bufferedReader.readLine() != null) {
                builder.append(bufferedReader.readLine());
                builder.append("\n");
            }
        } catch (e : IOException) {
            e.printStackTrace();
        } catch (e : Resources.NotFoundException ) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}