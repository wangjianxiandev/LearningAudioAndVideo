package com.example.learningaudioandvideo.codec

import android.hardware.Camera
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/1/24 19:52
 */
class VideoEncoder(size: Camera.Size) : AppCompatActivity() {
    private var mSize: Camera.Size

    private var mTrackIndex: Int = 0

    init {
        mSize = size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化编码器
        val mediaFormat =
            MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, mSize.width, mSize.height)
        mediaFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        )
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1048576)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        val mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val mp4Path = Environment.getExternalStorageDirectory().toString() + "wjx" + ".mp4"
        // 创建混合生成器MediaMuxer
        val mediaMuxer = MediaMuxer(mp4Path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        // 配置状态
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        mediaCodec.start()
        encodeVideo(mediaCodec, mediaMuxer)
    }

    /**
     * 通过getInputBuffers获取输入队列，然后调用dequeueInputBuffer获取输入队列空闲数组下标，
     * 注意dequeueOutputBuffer会有几个特殊的返回值表示当前编解码状态的变化，
     * 然后再通过queueInputBuffer把原始YUV数据送入编码器，
     * 而在输出队列端同样通过getOutputBuffers和dequeueOutputBuffer获取输出的h264流，
     * 处理完输出数据之后，需要通过releaseOutputBuffer把输出buffer还给系统，重新放到输出队列中。
     */
    private fun encodeVideo(mediaCodec: MediaCodec, mediaMuxer: MediaMuxer) {
        Thread(object : Runnable {
            override fun run() {
                while (true) {
                    try {
                        val bufferInfo = MediaCodec.BufferInfo()
                        val outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                        if (outputBufferId >= 0) {
                            val outPutBuffer = mediaCodec.getOutputBuffer(outputBufferId)
                            val h264: ByteArray = ByteArray(bufferInfo.size)
                            val outputBuffer = mediaCodec.getOutputBuffer(0)
                            outPutBuffer?.get(h264)
                            outPutBuffer?.position(bufferInfo.offset)
                            outPutBuffer?.limit(bufferInfo.offset + bufferInfo.size)
                            mediaMuxer.writeSampleData(mTrackIndex, outputBuffer!!, bufferInfo)
                            mediaCodec.releaseOutputBuffer(outputBufferId, false)
                        } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            val mediaFormat = mediaCodec.outputFormat
                            mTrackIndex = mediaMuxer.addTrack(mediaFormat)
                            mediaMuxer.start()
                        }
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                mediaCodec.stop()
                mediaCodec.release()
                mediaMuxer.stop()
                mediaMuxer.release()
            }
        }).start();
    }


}