#### OpenGL 渲染管线（OpenGL 渲染图像的流程）
* 几何图元：包括点、直线、三角形、均是铜鼓哦顶点vertex来指定的
* 模型：根据几何图元创建的物体
* 渲染：计算机根据模型创建图像的过程
    * 渲染结束后：在内存中，像素点组成一个大的一维数组，每4个Byte表示一个像素点的RGBA数据，在显卡中，这些像素点可以组成帧缓冲区（保存了图形硬件为了控制屏幕上所有像素的颜色和强度所需要的全部信息）

#### 渲染管线阶段
##### 阶段一：指定几何对象
* GL_POINTS：以点的形式进行绘制
* GL_LINES：以线的形式进行绘制
* GL_TRIANGLE_STRIP：以三角形的形式进行绘制
##### 阶段二：顶点处理 (Vertex Shader)
* 根据模型视图和投影矩阵进行变换来改变顶点的位置，根据纹理坐标与纹理矩阵来改变纹理坐标的位置
##### 阶段三：图元组装
* 顶点将会根据应用程序送往图元的几何对象，将纹理组装成图元
##### 阶段四：栅格化操作
* 确定每一个片元是什么（阶段三传递过来的数据，将会被分解成更小的单元，并对应于帧缓冲区的各个像素，这些单元称为片元，包含着窗口颜色， 纹理坐标等属性）
##### 阶段五：片元处理 (Fragment Shader)
* 通过纹理坐标取得纹理中相对应的片元像素值，根据自己的业务处理来变换该片元的颜色
##### 阶段六：帧缓冲操作
* 将最终的像素值写入帧缓冲区中


#### Android OpenGL ES使用流程
* 使用EGL搭建出OpenGL上下文环境以及渲染的目标屏幕
    * 1. EGLDisplay是一个封装系统物理屏幕的数据类型，调用eglGetDisplay返回的EGLDisplay来作为OpenGL ES的渲染目标
    * 2. 获取渲染屏幕之后调用eglInitialize来初始化这个显示设备
    * 3. 配置选项：指定色彩格式、像素格式、RGBA以及surfaceType
    * 4. 创建上下文环境
    * 5. 创建输出设备，使用EGLSurface将EGL和屏幕进行连接
    * 6. 开启新线程执行OpenGL ES的渲染操作，并且需要调用eglMakeCurrent为该线程绑定设备和上下文环境
    * 7. 双缓冲机制进行绘制图像，调用eglSwapBuffers，将前后台的frameBuffer进行调换，使得绘制的图像呈现在用户面前
    * 8. 使用后调用eglDestroySurface以及eglDestroyContext进行销毁操作

##### 示例代码 [grafika](https://github.com/wangjianxiandev/grafika/edit/master/app/src/main/java/com/android/grafika/gles/EglCore.java)

```

public final class EglCore {

    private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
    private EGLConfig mEGLConfig = null;
    private int mGlVersion = -1;


    /**
     * Prepares EGL display and context.
     * @param sharedContext The context to share, or null if sharing is not desired.
     * @param flags Configuration bit flags, e.g. FLAG_RECORDABLE.
     */
    public EglCore(EGLContext sharedContext, int flags) {
        ...
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            mEGLDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }


        // Try to get a GLES3 context, if requested.
        if ((flags & FLAG_TRY_GLES3) != 0) {
            //Log.d(TAG, "Trying GLES 3");
            EGLConfig config = getConfig(flags, 3);
            ...
        }
        if (mEGLContext == EGL14.EGL_NO_CONTEXT) {  // GLES 2 only, or GLES 3 attempt failed
            //Log.d(TAG, "Trying GLES 2");
            EGLConfig config = getConfig(flags, 2);
            if (config == null) {
                throw new RuntimeException("Unable to find a suitable EGLConfig");
            }
            int[] attrib2_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            EGLContext context = EGL14.eglCreateContext(mEGLDisplay, config, sharedContext,
                    attrib2_list, 0);
            checkEglError("eglCreateContext");
            mEGLConfig = config;
            mEGLContext = context;
            mGlVersion = 2;
        }

    /**
     * Finds a suitable EGLConfig.
     *
     * @param flags Bit flags from constructor.
     * @param version Must be 2 or 3.
     */
    private EGLConfig getConfig(int flags, int version) {
        ...
        // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
        // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
        // when reading into a GL_RGBA buffer.
        int[] attribList = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                //EGL14.EGL_DEPTH_SIZE, 16,
                //EGL14.EGL_STENCIL_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, renderableType,
                EGL14.EGL_NONE, 0,      // placeholder for recordable [@-3]
                EGL14.EGL_NONE
        };
        ...
        return configs[0];
    }


    /**
     * Discards all resources held by this class, notably the EGL context.  This must be
     * called from the thread where the context was created.
     * <p>
     * On completion, no context will be current.
     */
    public void release() {
        if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
            // Android is unusual in that it uses a reference-counted EGLDisplay.  So for
            // every eglInitialize() we need an eglTerminate().
            EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
            EGL14.eglReleaseThread();
            EGL14.eglTerminate(mEGLDisplay);
        }


        mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        mEGLContext = EGL14.EGL_NO_CONTEXT;
        mEGLConfig = null;
    }


    /**
     * Creates an EGL surface associated with a Surface.
     * <p>
     * If this is destined for MediaCodec, the EGLConfig should have the "recordable" attribute.
     */
    public EGLSurface createWindowSurface(Object surface) {
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture)) {
            throw new RuntimeException("invalid surface: " + surface);
        }
        // Create a window surface, and attach it to the Surface we received.
        int[] surfaceAttribs = {
                EGL14.EGL_NONE
        };
        EGLSurface eglSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, surface,
                surfaceAttribs, 0);
        checkEglError("eglCreateWindowSurface");
        if (eglSurface == null) {
            throw new RuntimeException("surface was null");
        }
        return eglSurface;
    }


    /**
     * Creates an EGL surface associated with an offscreen buffer.
     */
    public EGLSurface createOffscreenSurface(int width, int height) {
        int[] surfaceAttribs = {
                EGL14.EGL_WIDTH, width,
                EGL14.EGL_HEIGHT, height,
                EGL14.EGL_NONE
        };
        EGLSurface eglSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mEGLConfig,
                surfaceAttribs, 0);
        checkEglError("eglCreatePbufferSurface");
        if (eglSurface == null) {
            throw new RuntimeException("surface was null");
        }
        return eglSurface;
    }


    /**
     * Makes our EGL context current, using the supplied surface for both "draw" and "read".
     */
    public void makeCurrent(EGLSurface eglSurface) {
        if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
            // called makeCurrent() before create?
            Log.d(TAG, "NOTE: makeCurrent w/o display");
        }
        if (!EGL14.eglMakeCurrent(mEGLDisplay, eglSurface, eglSurface, mEGLContext)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    /**
     * Calls eglSwapBuffers.  Use this to "publish" the current frame.
     *
     * @return false on failure
     */
    public boolean swapBuffers(EGLSurface eglSurface) {
        return EGL14.eglSwapBuffers(mEGLDisplay, eglSurface);
    }
}
```