#### MediaRecorder状态图
* MediaRecorder是一个状态机，通过调用一系列方法进行状态之间的转换
![在这里插入图片描述](https://img-blog.csdnimg.cn/20210216214508691.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzM5NDI0MTQz,size_16,color_FFFFFF,t_70)

##### 状态说明
* Initial：通过MediaRecorder实例化类对象时处于初始化状态，此时MediaRecorder会占用系统资源，所有状态会通过reset()方法返回到该状态
* Initialized：使用setAudioSource()或者时setVideoSource()方法后进入音频或者视频录制，并可以指定音视频的文件属性，设置完成之后进入DataSourceConfigured状态
* Prepare：当用户使用MediaRecorder类中的prepare()方法时将进入到就绪状态，录制开始前的状态已就绪
* Recording：使用start()方法时将进入到录制状态，并且一直持续到录制结束
* Released：Idle state，可以通过在Initial状态调用release()方法来进入这个状态，这时将会释放所有和MediaRecorder对象绑定的资源
* Error：当错误发生的时候进入这个状态，它可以通过reset()方法进入Initial状态

#### 创建MediaRecorder
```
MediaRecorder recorder=newMediaRecorder();
 recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
 recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
 recorder.setOutputFile(PATH_NAME);
 recorder.prepare();
 recorder.start();  // Recording is now started
 ...
 recorder.stop();
 recorder.reset();  // You can reuse the object by going back to setAudioSource() step
 recorder.release();// Now the object cannot be reused
```