package com.example.learningaudioandvideo;

/**
 * Created with Android Studio.
 * Description:
 *
 * @author: Wangjianxian
 * @CreateDate: 2021/1/24 20:50
 */
class test {

    private synchronized void test() {
        synchronized (this) {
            //...
            synchronized (test.class) {
                // ...
            }
        }
    }
}
