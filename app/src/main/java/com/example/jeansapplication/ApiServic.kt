package com.example.jeansapplication

import android.content.Context
import android.util.Log
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
   val msg : String,
   val status : String
)

// 定义接口
interface ApiService {
    @FormUrlEncoded
    // 发起post请求
    @POST("/add_contact")
    fun makeARequest(@Field("name") name: String): Call<QueryResults>
}


fun wda(context: Context,name: String) {
    try {
        // 接口的实现和调用接口方法
        val call = RetrofitInstance.api.makeARequest(name)
        Log.i("Retrofit", "接口方法调用，发送请求至后端")
        // 发起异步请求
        call.enqueue(object : Callback<QueryResults> {
            // 连接成功
            override fun onResponse(call: Call<QueryResults?>, response: Response<QueryResults?>) {
                if (response.code() == 200) {
                    Log.w("Retrofit", "状态码 ${response.code()}")
                    android.app.AlertDialog.Builder(context)
                        .setTitle("Error Message")
                        .setMessage("状态码 ${response.code()} 错误")
                        .setPositiveButton("YES",null)
                        .show()
                } else {
                    val body = response.body() // 主动获取响应体
                    Log.w("Retrofit", "请求成功 ${body}")
                    if (body?.msg == "找到用户" && body?.status == "success") {
                        Log.i("Retrofit", "成功接收到响应体")

                    } else {
                        Log.w("Retrofit", "响应体接收失败")
                        android.app.AlertDialog.Builder(context)
                            .setTitle("Error Message")
                            .setMessage("响应体接收错误 ")
                            .setPositiveButton("YES",null)
                            .show()
                    }

                }
            }

            // 连接失败
            override fun onFailure(call: Call<QueryResults?>, t: Throwable) {
                android.app.AlertDialog.Builder(context)
                    .setTitle("Error Message")
                    .setMessage("${t.message}")
                    .setPositiveButton("YES", null)
                    .show()
                Log.w("Retrofit", "连接失败,错误信息 ${t.message}")
            }

        })
    }
    catch (e: Exception) {
        Log.w("Retrofit","错误: ${e.message}")
    }
    finally {
        Log.i("Retrofit", "请求流程结束")
    }
}






