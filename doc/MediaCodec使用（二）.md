#### 图像数据格式简介

* YUV格式：
    * planar：先连续存储所有像素点的Y，紧接着存储所有像素点的U，再存储所有像素点的V，
将Y、U、V的三个分量分别存放在不同的矩阵中
    * packed：将Y、U、V值存储成Macro Pixels数组，和RGB的存放方式类似
* YUV存储：
    * 主流的采样方式主要有：YUV444，YUV422，YUV420，只有正确的还原每个像素点的YUV值，才能通过YUV与RGB的转换公式提取出每个像素点的RGB值，然后显示出来
        * YUV 4:4:4表示完全取样，每一个Y对应一组UV分量,一个YUV占8+8+8 = 24bits 3个字节
        * YUV 4:2:2表示2:1的水平取样，垂直完全采样，每两个Y共用一组UV分量,一个YUV占8+4+4 = 16bits 2个字节
        * YUV 4:2:0表示2:1的水平取样，垂直2:1采样，每四个Y共用一组UV分量,一个YUV占8+2+2 = 12bits 1.5个字节
        * YUV4:1:1表示4:1的水平取样，垂直完全采样
        
##### 获取图像数据帧并进行编码
* 使用MediaCodec对onPreviewFrame获取返回的图像帧(格式默认为NV21)进行编码，并使用MediaMuxer进行保存
##### 创建编码器并打包
```
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
```
