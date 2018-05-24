package com.tencent.lukeyu.humanbodydetect

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

const val API_URL = "https://api-cn.faceplusplus.com/humanbodypp/v1/detect"
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val keyJson = assets.open("api.store")?.bufferedReader()?.use { it.readText() }
        val jsonObj = JSONObject(keyJson)
        val api_key = jsonObj.get("api_key")
        val api_secret = jsonObj.get("api_secret")

        Thread( {
            val boundary = UUID.randomUUID().toString()
            val url = URL(API_URL)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
//            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded")
            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setRequestProperty("Charset", "UTF-8")
            conn.requestMethod = "POST"
            conn.doInput = true
            conn.doOutput = true
            conn.useCaches = false
            conn.connect()

            //    curl -X POST "https://api-cn.faceplusplus.com/humanbodypp/v1/detect"
            //    -F "api_key=<api_key>" \
            //    -F "api_secret=<api_secret>" \
            //    -F "image_file=@image_file.jpg" \
            //    -F "return_attributes=gender"
            val outputStream = DataOutputStream(conn.outputStream)
            outputStream.writeBytes("api_key=$api_key")
            outputStream.writeBytes("api_secret=$api_secret")
            outputStream.writeBytes("image_url=\"http://inews.gtimg.com/newsapp_match/0/3687254466/0\"")
            outputStream.writeBytes("return_attributes=gender")
            outputStream.flush()
            outputStream.close()

            val respCode = conn.responseCode
            runOnUiThread { this@MainActivity.sample_text.text = respCode.toString() }
//            val inputStream = conn.inputStream
//            val reader = BufferedReader(InputStreamReader(inputStream))
//            val strBuilder = StringBuilder()
//            reader.forEachLine { strBuilder.append(it) }
//            reader.close()
            conn.disconnect()

//            runOnUiThread { this@MainActivity.sample_text.text = strBuilder.toString() }
        }).start()

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
