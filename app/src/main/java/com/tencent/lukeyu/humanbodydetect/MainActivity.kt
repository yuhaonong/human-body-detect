package com.tencent.lukeyu.humanbodydetect

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.LinkedHashMap


const val API_URL = "https://api-cn.faceplusplus.com/humanbodypp/v1/detect"
const val IMAGE_URL = "http://img.chinaluxus.com/pic/view/2011/07/20/20110720085657323.jpg"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Observable
                .create(ObservableOnSubscribe<String> {
                    it.onNext(detectUsingApi().toString())
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    this@MainActivity.sample_text.text = it
                })
    }

    companion object {
        private const val TIME_OUT = 8 * 1000                          //超时时间
        private const val CHARSET = "UTF-8"                         //编码格式
        private const val PREFIX = "--"                            //前缀
        private val BOUNDARY = UUID.randomUUID().toString()  //边界标识 随机生成
        private const val CONTENT_TYPE = "multipart/form-data"     //内容类型
        private const val LINE_END = "\n"                        //换行
    }

    private fun detectUsingApi() : String? {
        val keyJson = assets.open("api.store")?.bufferedReader()?.use { it.readText() }
        val jsonObj = JSONObject(keyJson)
        val apiKey = jsonObj.get("api_key") as String
        val apiSecret = jsonObj.get("api_secret") as String

        val url = URL(API_URL)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.connectTimeout = Companion.TIME_OUT
        conn.readTimeout = Companion.TIME_OUT
        conn.doInput = true
        conn.doOutput = true
        conn.useCaches = false
        conn.setRequestProperty("Connection", "Keep-Alive")
        conn.setRequestProperty("Content-Type", "${Companion.CONTENT_TYPE};boundary=${Companion.BOUNDARY}")
        conn.setRequestProperty("Charset", "UTF-8")

        val strParams = LinkedHashMap<String, String>()
        strParams["api_key"] = apiKey
        strParams["api_secret"] = apiSecret
        strParams["return_attributes"] = "gender"
        val outputStream = DataOutputStream(conn.outputStream)
        val paramPart = getStrParams(strParams).toString()
        outputStream.writeBytes(paramPart)
        Log.i("DEBUG", paramPart)

        //文件上传
        val fileSb = StringBuilder()
        fileSb.append(PREFIX).append(BOUNDARY).append(LINE_END)
                .append("""Content-Disposition: form-data; name="image_file"; filename="pony.jpg"$LINE_END""")
                .append("Content-Type: image/jpg$LINE_END")
                .append(LINE_END)// 参数头设置完以后需要两个换行，然后才是参数内容
        outputStream.writeBytes(fileSb.toString())
        assets.open("pony.jpg")?.buffered()?.use { outputStream.write(it.readBytes()) }
        outputStream.writeBytes(LINE_END + PREFIX + BOUNDARY + PREFIX + LINE_END)
        outputStream.flush()

        val respCode = conn.responseCode
        return if (respCode != 200) {
            conn.errorStream?.bufferedReader()?.use { it.readText() }
        } else {
            conn.inputStream?.bufferedReader()?.use { it.readText() }
        }
        conn.disconnect()
    }

    /**
     * 对post参数进行编码处理
     */
    private fun getStrParams(strParams: Map<String, String>): StringBuilder {
        val strSb = StringBuilder()
        for ((key, value) in strParams) {
            strSb.append(PREFIX).append(BOUNDARY).append(LINE_END)
                    .append("""Content-Disposition: form-data; name="$key"$LINE_END""")
                    .append(LINE_END)// 参数头设置完以后需要两个换行，然后才是参数内容
                    .append(value)
                    .append(LINE_END)
        }
        return strSb
    }
}
