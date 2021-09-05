#### EGL定义

* 连接OpenGL与设备窗口的中间层

#### EGL基础

* EGLDisplay
    * EGL定义的一个抽象的系统显示类，用于操作设备窗口
* EGLConfig
    * EGL配置

* EGLSurface
    * 渲染缓存，一块内存空间，所有要渲染到屏幕上的图像数据，都先要缓存到EGLSurface上

* EGlContext
    * OpenGL上下文，用于存储OpenGL的绘制状态信息、数据

#### 创建EGL
* 初始化
    * 通过eglGetDisplay创建EGLDisplay
    * 通过eglInitialize初始化了EGLDisplay
    * 通过eglCreateContext初始化EGLContext

* 创建EGLSurface，分为两种模式：
    * 可显示窗口，使用eglCreateWindowSurface创建
        * 通常将页面上的SurfaceView持有的Surface，或者SurfaceTexture传递进去进行绑定，使得openGL处理的数据可以显示在屏幕上。
    * 离屏（不可见）窗口，使用eglCreatePbufferSurface创建
        * 离屏渲染，将openGL处理的图像数据保存再缓存中，不会显示再屏幕上，但是整个渲染流程和普通模式一样，这样可以处理用户不需要看见的数据

* 绑定OpenGL渲染线程与绘制上下文：makeCurrent
    * 使用eglMakeCurrent来实现绑定
    * eglMakeCurrent这个方法，实现了设备显示窗口（EGLDisplay）、 OpenGL 上下文（EGLContext）、图像数据缓存（GLSurface） 、当前线程的绑定

* 交换缓存数据，并显示图像：swapBufferse
    * glSwapBuffers是EGL提供的用来将EGLSurface数据显示到设备屏幕上的方法。在OpenGL绘制完图像化，调用该方法，才能真正显示出来。
* 解绑数据缓存表面，以及释放资源
    * 当页面上的Surface被销毁（比如App到后台）的时候，需要将资源解绑。当页面退出时，这时SurfaceView被销毁，需要释放所有的资源
