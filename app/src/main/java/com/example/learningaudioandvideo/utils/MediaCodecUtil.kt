package com.example.learningaudioandvideo.utils

import android.media.*
import android.media.AudioFormat.CHANNEL_IN_STEREO
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.util.Size

/**
 * Created with Android Studio.
 * Description:
 * @author: Wangjianxian
 * @CreateDate: 2021/1/19 0:54
 */
object MediaCodecUtil {
    // 音频源：音频输入-麦克风
    private const val AUDIO_INPUT = MediaRecorder.AudioSource.MIC

    // 采样率
    // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private const val AUDIO_SAMPLE_RATE = 44100

    // 音频通道 单声道
    private const val AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO

    // 音频通道 立体声：CHANNEL_OUT_STEREO或CHANNEL_IN_STEREO
    private const val AUDIO_CHANNEL2 = AudioFormat.CHANNEL_IN_STEREO

    // 音频格式：PCM编码
    private const val AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT

    private var bufferSizeInBytes: Int = 0

    /**
     * 获取缓冲大小
     */
    fun getBufferSizeInBytes(): Int {
        return bufferSizeInBytes
    }

    fun createVideoEncode(surfaceSize: Size): MediaCodec {
        //视频编码器
        val videoEncoder = MediaCodec.createEncoderByType("video/avc")
        // 创建视频MediaFormat
        val videoFormat = MediaFormat.createVideoFormat(
            "video/avc", surfaceSize.width
            , surfaceSize.height
        )
        // 指定编码器颜色格式
        videoFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        // 指定编码器码率
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, 0)
        // 指定编码器帧率
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        // 指定编码器关键帧间隔
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
        // BITRATE_MODE_CBR输出码率恒定
        // BITRATE_MODE_CQ保证图像质量
        // BITRATE_MODE_VBR图像复杂则码率高，图像简单则码率低
        videoFormat.setInteger(
            MediaFormat.KEY_BITRATE_MODE,
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR
        )
        videoFormat.setInteger(
            MediaFormat.KEY_COMPLEXITY,
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR
        )
        videoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        return videoEncoder
    }

    fun createAudioEncoder(): MediaCodec {
        //音频编码器
        val audioEncoder = MediaCodec.createEncoderByType("audio/mp4a-latm")
        // 创建音频MediaFormat,参数2：采样率，参数3：通道
        val audioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", 44100, 1)

        // 仅编码器指定比特率
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 4 * 1024)

        var bufferSizeInBytes = getBufferSizeInBytes()
        if (bufferSizeInBytes == 0) {
            bufferSizeInBytes = AudioRecord.getMinBufferSize(
                AUDIO_SAMPLE_RATE ,
                CHANNEL_IN_STEREO,
                ENCODING_PCM_16BIT
            )
        }

        //可选的，输入数据缓冲区的最大大小
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSizeInBytes)

        audioFormat.setInteger(
            MediaFormat.KEY_AAC_PROFILE,
            MediaCodecInfo.CodecProfileLevel.AACObjectLC
        )

        audioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        return audioEncoder
    }

    /**
     * 默认获取单声道AudioRecord
     */
    fun getSingleAudioRecord(
        channelConfig: Int = AUDIO_CHANNEL,
        audioSource: Int = AUDIO_INPUT,
        sampleRateInHz: Int = AUDIO_SAMPLE_RATE,
        audioFormat: Int = AUDIO_ENCODING
    ): AudioRecord {
        //audioRecord能接受的最小的buffer大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
        return AudioRecord(
            audioSource,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            bufferSizeInBytes
        )
    }
}