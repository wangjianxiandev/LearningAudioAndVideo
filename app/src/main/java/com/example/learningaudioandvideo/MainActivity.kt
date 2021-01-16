package com.example.learningaudioandvideo

import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mediaplayer = MediaPlayer()
        if (mediaplayer != null) {
            //倍速设置，必须在23以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mediaplayer.playbackParams = mediaplayer.playbackParams.setSpeed(1.5f)
                mediaplayer.pause()
                mediaplayer.start()
            }
        }
    }
}