//package com.example.wancamera
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import androidx.appcompat.app.AppCompatActivity
//import com.example.learningaudioandvideo.R
//import kotlinx.android.synthetic.main.activity_main.*
//
//
//class MainActivity : AppCompatActivity(R.layout.activity_main) {
//    private lateinit var mFrameLayout : FrameLayout
//
//    private lateinit var mCameraLayout : FrameLayout
//
//    private lateinit var mMyGlSurfaceView : MyGLSurfaceView
//
//    private lateinit var mMyCameraSurfaceView: MyCameraSurfaceView
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        sample_text.text = stringFromJNI()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(arrayOf<String>({Manifest.permission.CAMERA}.toString()), 1);
//            }
//        }
//        mFrameLayout = frame_layout
//        mCameraLayout = camera_layout
//        mMyGlSurfaceView = MyGLSurfaceView(this)
//        mMyCameraSurfaceView = MyCameraSurfaceView(this)
//        mFrameLayout.addView(
//            mMyGlSurfaceView,
//            ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT
//            )
//        )
//        mCameraLayout.addView(
//            mMyCameraSurfaceView,
//            ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT
//            )
//        )
//    }
//
//    /**
//     * A native method that is implemented by the 'native-lib' native library,
//     * which is packaged with this application.
//     */
//    external fun stringFromJNI(): String
//
//    companion object {
//        // Used to load the 'native-lib' library on application startup.
//        init {
//            System.loadLibrary("native-lib")
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        //mMyCameraSurfaceView.exitCamera();
//    }
//}
