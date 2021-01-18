#### 使用MediaCodec目的
* MediaCodec是Android底层多媒体框架的一部分，通常与MediaExtractor、MediaMuxer、AudioTrack结合使用，可以编码H264、H265、AAC、3gp等常见的音视频格式
* MediaCodec工作原理是处理输入数据以产生输出数据

#### MediaCodec工作流程
* MediaCodec的数据流分为input和output流，并通过异步的方式处理两路数据流，直到手动释放output缓冲区，MediaCodec才将数据处理完毕
    * input流：客户端输入待解码或者待编码的数据
    * output流：客户端输出的已解码或者已编码的数据
* 官方示例图：
    * ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210119011148923.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM5NDI0MTQz,size_16,color_FFFFFF,t_70#pic_center)

#### MediaCodec API说明
* getInputBuffers：获取需要输入流队列，返回ByteBuffer数组
* queueInputBuffer：输入流入队
* dequeueInputBuffer: 从输入流队列中取数据进行编码操作
* getOutputBuffers：获取已经编解码之后的数据输出流队列，返回ByteBuffer数组
* dequeueOutputBuffer：从输出队列中取出已经编码操作之后的数据
* releaseOutputBuffer: 处理完成，释放output缓冲区

#### 基本流程
* ![在这里插入图片描述](https://img-blog.csdnimg.cn/20210119011217396.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM5NDI0MTQz,size_16,color_FFFFFF,t_70#pic_center)

* MediaCodec的基本使用遵循上图所示，它的生命周期如下所示：
    * Stoped：创建好MediaCodec，进行配置，或者出现错误
        * Uninitialized: 当创建了一个MediaCodec对象，此时MediaCodec处于Uninitialized，在任何状态调用reset()方法使MediaCodec返回到Uninitialized状态
        * Configured: 使用configure(…)方法对MediaCodec进行配置转为Configured状态
        * Error: 出现错误
    * Executing：可以在Executing状态的任何时候通过调用flush()方法返回到Flushed状态
        * Flushed：调用start()方法后MediaCodec立即进入Flushed状态
        * Running：调用dequeueInputBuffer后，MediaCodec就转入Running状态
        * End-of-Stream：编解码结束后，MediaCodec将转入End-of-Stream子状态
    * Released：当使用完MediaCodec后，必须调用release()方法释放其资源 
#### 基本使用
```
//解码器
val mVideoDecoder = MediaCodec.createDecoderByType("video/avc")
//编码器
val mVideoEncoder = MediaCodec.createEncoderByType("video/avc")
```

* **注意：** [常见音视频格式](
https://developer.android.google.cn/reference/kotlin/android/media/MediaFormat?hl=en)

##### MediaCodec工具类
```

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
```
##### 录制音视频并编码
```
// 基本使用
val videoEncoder = MediaCodecUtil.createVideoEncode(size)
// 设置buffer
videoEncoder.setInputSurface(surface)
videoEncoder.start()
//音频录制类
val audioRecord = MediaCodecUtil.getSingleAudioRecord(AudioFormat.CHANNEL_IN_STEREO)
//音频编码器
val audioEncoder = MediaCodecUtil.createAudioEncoder()
audioEncoder.start()


GlobalScope.launch (Dispatchers.IO) {
    while (isActive) {
        val length = AudioRecordUtil.getBufferSizeInBytes()
        audioRecord.read(mAudioBuffer, 0, length)


        val inputIndex = audioEncoder.dequeueInputBuffer(0)
        if (inputIndex >= 0) {
            val byteBuffer = audioEncoder.getInputBuffer(inputIndex)
            if (byteBuffer != null) {
                byteBuffer.clear()
                byteBuffer.put(mAudioBuffer)
                byteBuffer.limit(length);// 设定上限值
                audioEncoder.queueInputBuffer(
                    inputIndex,
                    0,
                    length,
                    System.nanoTime(),
                    0
                ); // 第三个参数为时间戳，这里是使用当前
            }
        }


        val outputIndex = audioEncoder.dequeueOutputBuffer(mBufferInfo, 0)
        if (outputIndex >= 0) {
            val byteBuffer = audioEncoder.getOutputBuffer(outputIndex)
            if (byteBuffer != null) {
                val byte = byteBuffer.get(outputIndex)
            }
            audioEncoder.releaseOutputBuffer(outputIndex, false)
        }
    }
}
```