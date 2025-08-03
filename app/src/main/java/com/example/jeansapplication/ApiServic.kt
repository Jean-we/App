package com.example.jeansapplication

import android.content.Context
import android.os.Message
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import okhttp3.internal.http2.Http2Connection
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.Callback
import retrofit2.Response




// 可重复使用类
object RetrofitInstance {
    // 构建器
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://192.168.1.249:2008/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    fun b(context: Context,name: String) {
        wda(context,name)
    }
    init {
        Log.i("Retrofit", "构建器创建")
    }

}

// 数据类
data class QueryResults(
    val exists: Boolean
)

// 定义接口
interface ApiService {
    @FormUrlEncoded
    // 发起post请求
    @POST("/add_contact")
    fun makeARequest(@Field("name") name: String): Call<QueryResults>
}


fun wda(context: Context,name: String) {
    // 接口的实现和调用接口方法
    val call = RetrofitInstance.api.makeARequest(name)
    Log.i("Retrofit", "接口方法调用，发送请求至后端")
    // 发起异步请求
    call.enqueue(object : Callback<QueryResults> {
        // 连接成功
        override fun onResponse(call: Call<QueryResults?>, response: Response<QueryResults?>) {
            if (response.code()==200)   {
                Log.w("Retrofit","返回码 ${response.code()}")
            }
            else {
                val body = response.body()
                Log.w("Retrofit","请求成功 ${body}")
            }
        }

        // 连接失败
        override fun onFailure(call: Call<QueryResults?>, t: Throwable) {
            android.app.AlertDialog.Builder(context)
                .setTitle("Error Message")
                .setMessage("${t.message}")
                .setPositiveButton("YES",null)
                .show()

            Log.w("Retrofit","连接失败,错误信息 ${t.message}")
        }

    })
}






