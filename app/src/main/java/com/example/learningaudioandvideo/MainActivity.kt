package com.example.learningaudioandvideo

import android.app.Service
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.learningaudioandvideo.utils.MediaCodecUtil

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

//        // 基本使用
//        val videoEncoder = MediaCodecUtil.createVideoEncode(size)
//        // 设置buffer
//        videoEncoder.setInputSurface(surface)
//        videoEncoder.start()
//        //音频录制类
//        val audioRecord = MediaCodecUtil.getSingleAudioRecord(AudioFormat.CHANNEL_IN_STEREO)
//        //音频编码器
//        val audioEncoder = MediaCodecUtil.createAudioEncoder()
//        audioEncoder.start()
//
//        GlobalScope.launch(Dispatchers.IO) {
//            while (isActive) {
//                val length = AudioRecordUtil.getBufferSizeInBytes()
//                audioRecord.read(mAudioBuffer, 0, length)
//
//                val inputIndex = audioEncoder.dequeueInputBuffer(0)
//                if (inputIndex >= 0) {
//                    val byteBuffer = audioEncoder.getInputBuffer(inputIndex)
//                    if (byteBuffer != null) {
//                        byteBuffer.clear()
//                        byteBuffer.put(mAudioBuffer)
//                        byteBuffer.limit(length);// 设定上限值
//                        audioEncoder.queueInputBuffer(
//                            inputIndex,
//                            0,
//                            length,
//                            System.nanoTime(),
//                            0
//                        ); // 第三个参数为时间戳，这里是使用当前
//                    }
//                }
//
//                val outputIndex = audioEncoder.dequeueOutputBuffer(mBufferInfo, 0)
//                if (outputIndex >= 0) {
//                    val byteBuffer = audioEncoder.getOutputBuffer(outputIndex)
//                    if (byteBuffer != null) {
//                        val byte = byteBuffer.get(outputIndex)
//                    }
//                    audioEncoder.releaseOutputBuffer(outputIndex, false)
//                }
//            }
//        }

        // 创建AudioManager对象
        val audioManager: AudioManager = getSystemService(Service.AUDIO_SERVICE) as AudioManager
        // 指定调节音乐的音频，增大音量，而且显示音量图形示意
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
        // 指定调节音乐的音频，降低音量，只有声音,不显示图形条
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
    }
}