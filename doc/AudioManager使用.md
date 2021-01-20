#### 作用

* 官方提供的音量和铃声管理类

#### API
| 方法|意义 |
|-- |--|
| adjustVolume | 控制手机音量,调大或者调小一个单位,根据第一个参数进行判断 AudioManager.ADJUST_LOWER,可调小一个单位; AudioManager.ADJUST_RAISE,可调大一个单位 |
| setStreamVolume| 直接设置音量大小 |
| getStreamVolume | 获得手机的当前音量,最大值为7,最小值为0,当设置为0的时候,会自动调整为震动模式 |
| setStreamMute | 将手机某个声音类型设置为静音 |
[官方API](
https://developer.android.google.cn/reference/android/media/AudioManager.html)

#### 具体使用
```
// 创建AudioManager实例
// 创建AudioManager对象
val audioManager: AudioManager = getSystemService(Service.AUDIO_SERVICE) as AudioManager
// 指定调节音乐的音频，增大音量，而且显示音量图形示意
audioManager.adjustStreamVolume(
    AudioManager.STREAM_MUSIC,
    AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
// 指定调节音乐的音频，降低音量，只有声音,不显示图形条
audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
    AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
```