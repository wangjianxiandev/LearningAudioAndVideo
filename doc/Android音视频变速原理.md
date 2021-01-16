#### 视频倍速播放
*  ![8ae5095a88677cd9e91fb78feb6f911f.png](en-resource://database/10854:1)

* 假设视频帧率是24fps, 则播放器必须在1000/24 = 41.66ms 内 解封装 + 解码 + 渲染完一帧，一般只计算出把YUV数据从渲染队列中取出到渲染结束的时间（RenderTime），超过了这个时间就会出现播放卡顿，画面延迟
* 渲染是花不到41.66ms的，假设渲染时间是10ms，那么我们就要休眠 41.66-10 = 31.66ms ，再继续从渲染队列拿帧数据。
* 倍速播放就是24fps换成48fps，即1000ms内播放出48帧，即RenderTime = 41.66/2 = 20.83ms, 所以按照原理只需要改变RenderTime即可实现倍速播放


#### 音频倍速播放

* Android的音频播放大多采用两种方案，AudioTrack和OpenSL ES
* 注意参数：
    * 采样率：sampleRate
    * 声道数：channel
    * 采样格式：audioFormat
* 假设现在有一段48000Hz，16位的单声道音频，用AudioTrack播放的时候，按照前面视频的思路，就是一秒钟的时间内，需要喂给播放器的数据是
48000 * 16 * 1 / 8 = 9600 字节/秒

* 如果是两倍速那么就是将9600 * 2 = 48000 * 16 * / 40 即提高采样率即可实现音频倍速播放

##### 音频倍速实现
```

MediaPlayer.setPlaybackParams(PlaybackParams params) throws IllegalStateException, IllegalArgumentException
```

* 具体实现：
```
val mediaplayer = MediaPlayer()
if (mediaplayer != null) {
    //倍速设置，必须在23以上
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        mediaplayer.playbackParams = mediaplayer.playbackParams.setSpeed(1.5f)
        mediaplayer.pause()
        mediaplayer.start()
    }
}
```