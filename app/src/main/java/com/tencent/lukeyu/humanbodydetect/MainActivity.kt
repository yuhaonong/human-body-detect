package com.tencent.lukeyu.humanbodydetect

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
//    curl -X POST "https://api-cn.faceplusplus.com/humanbodypp/v1/detect" -F "api_key=<api_key>" \
//    -F "api_secret=<api_secret>" \
//    -F "image_file=@image_file.jpg" \
//    -F "return_attributes=gender"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        sample_text.text = stringFromJNI()
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
