package com.example.learningaudioandvideo.camera2stream

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.opengl.GLES20
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.learningaudioandvideo.R
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import javax.microedition.khronos.opengles.GL10


class Camera2StreamActivity : AppCompatActivity(R.layout.activity_camera2_stream), SurfaceTexture.OnFrameAvailableListener {
    private var previewSurface: SurfaceView? = null
    private var previewSurfaceHolder: SurfaceHolder? = null

    // 预览流
    private var mediaPreviewEncode
            : MediaCodec? = null

    // 拍照流
    private var mediaCaptureEncode
            : MediaCodec? = null

    // 预览编码格式
    private var mediaPreviewFormat
            : MediaFormat? = null

    // 拍摄编码格式
    private var mediaCaptureFormat
            : MediaFormat? = null

    //预览流使用的surface
    private var mediaPreviewSurface
            : Surface? = null

    //拍摄流使用的surface
    private var mediaCaptureSurface
            : Surface? = null
    private var mPreviewBuilder: CaptureRequest.Builder? = null

    //压缩格式
    private val codecType = MediaFormat.MIMETYPE_VIDEO_AVC

    private var cameraDevice //camera
            : CameraDevice? = null

    //录制标志
    private var upRecordFlag = false

    //录制标志
    private val saveRecordFlag = false

    private var upBufferInfo: MediaCodec.BufferInfo? = null
    private var saveBufferInfo: MediaCodec.BufferInfo? = null
    private val cameraCharacteristics: CameraCharacteristics? = null

    private var mSurfaceTexture : SurfaceTexture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSurfaceTexture = SurfaceTexture(1)
        mSurfaceTexture!!.setOnFrameAvailableListener(this)
        previewSurface = findViewById(R.id.previewSurface)
        previewSurfaceHolder = previewSurface!!.holder
        previewSurfaceHolder!!.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                openCamera(1920, 1080)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {}
        })
    }

    /*open camera*/
    @SuppressLint("MissingPermission")
    private fun openCamera(width: Int, height: Int) {
        /*获得相机管理服务*/
        val manager =
            this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            /*获取CameraID列表*/
            val cameralist = manager.cameraIdList
            manager.openCamera(cameralist[0], CameraOpenCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /*close camera*/
    private fun closeCamera() {}

    /*摄像头打开回调*/
    var CameraOpenCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(@NonNull camera: CameraDevice) {
            cameraDevice = camera
            prepareEncoder(camera)
        }

        override fun onDisconnected(@NonNull camera: CameraDevice) {}
        override fun onError(@NonNull camera: CameraDevice, error: Int) {}
    }

    fun onFrameAvailable(gl: GL10?) {
        Log.e("WJX", "onDrawFrame...")
    }
    fun prepareEncoder(camera : CameraDevice) {
        try {
            upBufferInfo = MediaCodec.BufferInfo()
            //创建编码器
            mediaPreviewEncode = MediaCodec.createEncoderByType(codecType)
            //设置编码参数
            mediaPreviewFormat = MediaFormat.createVideoFormat(codecType, 1280, 720)
            //设置颜色格式
            mediaPreviewFormat!!.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            // 设置比特率
            mediaPreviewFormat!!.setInteger(MediaFormat.KEY_BIT_RATE, 2000000)
            // 设置帧率
            mediaPreviewFormat!!.setInteger(MediaFormat.KEY_FRAME_RATE, 25)
            // 设置关键帧间隔时间（S）
            mediaPreviewFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            // 将设置好的参数配置给编码器
            mediaPreviewEncode!!.configure(
                mediaPreviewFormat,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
            //使用surface代替mediacodec数据输入buffer
            mediaPreviewSurface = Surface(mSurfaceTexture)
//                mediaPreviewEncode!!.createInputSurface()
            saveBufferInfo = MediaCodec.BufferInfo()
            mediaCaptureEncode = MediaCodec.createEncoderByType(codecType)
            mediaCaptureFormat = MediaFormat.createVideoFormat(codecType, 1920, 1080)
            mediaCaptureFormat!!.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            mediaCaptureFormat!!.setInteger(MediaFormat.KEY_BIT_RATE, 4000000)
            mediaCaptureFormat!!.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            mediaCaptureFormat!!.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
            mediaCaptureEncode!!.configure(
                mediaCaptureFormat,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
            mediaCaptureSurface = mediaCaptureEncode!!.createInputSurface()

            //设置预览尺寸
            previewSurfaceHolder!!.setFixedSize(1920, 1080)
            // 创建预览请求
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewBuilder!!.addTarget(previewSurfaceHolder!!.surface)
            mPreviewBuilder!!.addTarget(mediaPreviewSurface!!)
            mPreviewBuilder!!.addTarget(mediaCaptureSurface!!)
            //创建会话
            camera.createCaptureSession(
                Arrays.asList<Surface>(
                    previewSurfaceHolder!!.surface,
                    mediaCaptureSurface,
                    mediaPreviewSurface
                ), Sessioncallback, null
            )
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    /*录制视频回调*/
    var Sessioncallback: CameraCaptureSession.StateCallback =
        object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(@NonNull session: CameraCaptureSession) {
                try {
                    startMediaCodecRecording()
                    session.setRepeatingRequest(mPreviewBuilder!!.build(), null, null)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
                //开始录制
            }

            override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {}
        }

    /*开始录制视频*/
    private fun startMediaCodecRecording() {
        /*预览流模拟录制，保存到本地*/
        val recordThread: Thread = object : Thread() {
            override fun run() {
                super.run()
                if (mediaPreviewEncode == null) {
                    return
                }
                Log.d("MediaCodec", "预览流开始录制###################")
                upRecordFlag = true
                mediaPreviewEncode!!.start()
                while (upRecordFlag) {
                    val status = mediaPreviewEncode!!.dequeueOutputBuffer(upBufferInfo!!, 10000)
                    if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        Log.e("MdiaCodec ", " time out")
                    } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        Log.e("MediaCodec", "format changed")
                    } else if (status >= 0) {
                        val data = mediaPreviewEncode!!.getOutputBuffer(status)
                        if (data != null) {
                            upBufferInfo!!.presentationTimeUs =
                                SystemClock.uptimeMillis() * 1000
//                             drainEncoder()
                        }
                        // releasing buffer is important
                        mediaPreviewEncode!!.releaseOutputBuffer(status, false)
                        val endOfStream =
                            upBufferInfo!!.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        if (endOfStream == MediaCodec.BUFFER_FLAG_END_OF_STREAM) break
                    }
                }
                mediaPreviewSurface!!.release()
                mediaPreviewSurface = null
                mediaPreviewEncode!!.stop()
                mediaPreviewEncode!!.release()
                mediaPreviewEncode = null
            }
        }
        // 开始录制
        recordThread.start()


        /*保存到拍摄流录制*/
        val recordThread1: Thread = object : Thread() {
            override fun run() {
                super.run()
                if (mediaCaptureEncode == null) {
                    return
                }
                Log.d("MediaCodec", "拍摄流开始录制###################")
                upRecordFlag = true
                mediaCaptureEncode!!.start()
                while (upRecordFlag) {
                    val status = mediaCaptureEncode!!.dequeueOutputBuffer(saveBufferInfo!!, 10000)
                    if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        Log.e("MdiaCodec ", " time out")
                    } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        Log.e("MediaCodec", "format changed")
                    } else if (status >= 0) {
                        val data = mediaCaptureEncode!!.getOutputBuffer(status)
                        if (data != null) {
                            saveBufferInfo!!.presentationTimeUs =
                                SystemClock.uptimeMillis() * 1000
//                             drainEncoder()
                        }
                        // releasing buffer is important
                        mediaCaptureEncode!!.releaseOutputBuffer(status, false)
                        val endOfStream =
                            saveBufferInfo!!.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        if (endOfStream == MediaCodec.BUFFER_FLAG_END_OF_STREAM) break
                    }
                }
                mediaCaptureSurface!!.release()
                mediaCaptureSurface = null
                mediaCaptureEncode!!.stop()
                mediaCaptureEncode!!.release()
                mediaCaptureEncode = null
            }
        }
        recordThread1.start()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        try {
            mediaPreviewSurface!!.release()
            mediaPreviewEncode!!.stop()
            mediaPreviewEncode!!.release()
            cameraDevice!!.close()
            mediaCaptureSurface!!.release()
            mediaCaptureEncode!!.stop()
            mediaCaptureEncode!!.release()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun release() {
//        Surface(SurfaceTexture(1))
    }

//    private fun drainEncoder(endOfStream: Boolean) {
//        val TIMEOUT_USEC = 10000
//        if (endOfStream) {
//            Log.d(
//                com.android.grafika.SoftInputSurfaceActivity.TAG,
//                "drainEncoder($endOfStream)"
//            )
//        }
//        if (endOfStream) {
//            mediaPreviewEncode!!.signalEndOfInputStream()
//        }
//        var encoderOutputBuffers: Array<ByteBuffer?> = mediaPreviewEncode.getOutputBuffers()
//        while (true) {
//            val encoderStatus: Int =
//                mediaPreviewEncode.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC.toLong())
//            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                // no output available yet
//                if (!endOfStream) {
//                    break // out of while
//                } else {
//                    if (com.android.grafika.SoftInputSurfaceActivity.VERBOSE) Log.d(
//                        com.android.grafika.SoftInputSurfaceActivity.TAG,
//                        "no output available, spinning to await EOS"
//                    )
//                }
//            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
//                // not expected for an encoder
//                encoderOutputBuffers = mediaPreviewEncode!!.getOutputBuffers()
//            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                // should happen before receiving buffers, and should only happen once
//                if (mMuxerStarted) {
//                    throw RuntimeException("format changed twice")
//                }
//                val newFormat: MediaFormat = mediaPreviewEncode!!.getOutputFormat()
//                Log.d(
//                    com.android.grafika.SoftInputSurfaceActivity.TAG,
//                    "encoder output format changed: $newFormat"
//                )
//
//                // now that we have the Magic Goodies, start the muxer
//                mTrackIndex = mMuxer.addTrack(newFormat)
//                mMuxer.start()
//                mMuxerStarted = true
//            } else if (encoderStatus < 0) {
//                Log.w(
//                    com.android.grafika.SoftInputSurfaceActivity.TAG,
//                    "unexpected result from encoder.dequeueOutputBuffer: " +
//                            encoderStatus
//                )
//                // let's ignore it
//            } else {
//                val encodedData = encoderOutputBuffers[encoderStatus]
//                    ?: throw RuntimeException(
//                        "encoderOutputBuffer " + encoderStatus +
//                                " was null"
//                    )
//                if (mBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
//                    // The codec config data was pulled out and fed to the muxer when we got
//                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
//                    if (com.android.grafika.SoftInputSurfaceActivity.VERBOSE) Log.d(
//                        com.android.grafika.SoftInputSurfaceActivity.TAG,
//                        "ignoring BUFFER_FLAG_CODEC_CONFIG"
//                    )
//                    mBufferInfo.size = 0
//                }
//                if (mBufferInfo.size != 0) {
//                    if (!mMuxerStarted) {
//                        throw RuntimeException("muxer hasn't started")
//                    }
//
//                    // adjust the ByteBuffer values to match BufferInfo
//                    encodedData.position(mBufferInfo.offset)
//                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size)
//                    mBufferInfo.presentationTimeUs = mFakePts
//                    mFakePts += 1000000L / com.android.grafika.SoftInputSurfaceActivity.FRAMES_PER_SECOND
//                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo)
//                    if (com.android.grafika.SoftInputSurfaceActivity.VERBOSE) Log.d(
//                        com.android.grafika.SoftInputSurfaceActivity.TAG,
//                        "sent " + mBufferInfo.size + " bytes to muxer"
//                    )
//                }
//                mediaPreviewEncode!!.releaseOutputBuffer(encoderStatus, false)
//                if (mBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
//                    if (!endOfStream) {
//                        Log.w(
//                            com.android.grafika.SoftInputSurfaceActivity.TAG,
//                            "reached end of stream unexpectedly"
//                        )
//                    } else {
//
//                    }
//                    break // out of while
//                }
//            }
//        }
//    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        Log.e("WJX", "onframe available");
    }
}