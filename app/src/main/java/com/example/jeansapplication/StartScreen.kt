package com.example.jeansapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.apply


// app默认启动界面，包含开启动画、默认添加Jetpack Navigation Compose 页面切换管理器
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 初始化页面管理器
            val RegisterOrLogin = rememberNavController()

            // 中心管理器
            NavHost(navController = RegisterOrLogin, startDestination = "Start") {
                /*              此管理器用于判断是否是已经注册用户               */
                composable("Start") { StartScreen(RegisterOrLogin) }
                composable("Main") { MainPageManner(RegisterOrLogin) }
            }
        }
    }
}







@Composable
fun StartScreen(navController: NavHostController) {
    var isBlured by remember { mutableStateOf(0f) }
    // 使用动画平滑过渡模糊半径
    val blurRadius by animateFloatAsState(
        targetValue = isBlured,
        animationSpec = tween(durationMillis = 3000) // 设置持续时间为500毫秒
    )

    // 控制输入框是否显示变量
    var showInputEmail = remember { mutableStateOf(false) }
    var showInputName = remember { mutableStateOf(false) }
    var showInputPassword = remember { mutableStateOf(false) }
    var showInputCode = remember { mutableStateOf(false) }
    var showSentCodeBt = remember { mutableStateOf(false) }
    // 注册按钮,验证码页面
    var showregister  =  remember { mutableStateOf(false) }
    // 老用户登录按钮
    var showloginBt = remember { mutableStateOf(false) }


    // 存储输入内容变量，响应式
    var emailText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var codeText by remember { mutableStateOf("")}

    val alphaAnim by animateFloatAsState(
        targetValue = if (showInputEmail.value) 3f else 0f,
        animationSpec = tween(durationMillis = 3000)
    )
    // 登录 注册按钮是否可以点击
    var isLogInBtnEnabled by remember { mutableStateOf(true) }
    var isSignUpBtnEnabled by remember { mutableStateOf(true) }

    // 声明一个状态保存倒计时时间
    var countDown by remember { mutableStateOf(60) }
    val context = LocalContext.current

    // 验证错误弹窗内容变量
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    // 返回按钮
    val backButton = remember {mutableStateOf(false)}







    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0.071f, 0.071f, 0.071f, 1.0f))



    ) {
        // 模糊背景层
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius.dp)
                .zIndex(0f)
        ) {
            // 布局管理器uj
            ConstraintLayout(modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)) {
                val (loginBtn, signupBtn, text) = createRefs()




                fun BtClick(TextType: String) {
                    // 判断哪个按钮点击
                    if (TextType == "log_in") {
                        isBlured = 8f

                        // 输入框and按钮显示
                        showInputEmail.value = true
                        showInputName.value = true
                        showInputPassword.value = true
                        showInputCode.value = false
                        showSentCodeBt.value = false
                        // 老用户登录按钮显示
                        showloginBt.value = true


                        //登录 注册按钮不可点击
                        isLogInBtnEnabled = false
                        isSignUpBtnEnabled = false

                        // 注册按钮显示
                        showregister.value = false

                        // 返回按钮显示
                        backButton.value = true


                    } else if (TextType == "sign_up") {
                        isBlured = 8f

                        // 输入框and按钮显示
                        showInputEmail.value = true
                        showInputPassword.value = true
                        showInputCode.value = true
                        showSentCodeBt.value = true

                        //登录 注册按钮不可点击
                        isLogInBtnEnabled = false
                        isSignUpBtnEnabled = false

                        // 注册按钮显示
                        showregister.value = true

                        // 返回按钮显示
                        backButton.value = true

                    }
                }


                // 登录按钮
                Button(
                    onClick = { BtClick("log_in") }, // 按钮点击运行函数,并传参数
                    modifier = Modifier
                        .constrainAs(loginBtn) {
                            // 控件对齐左边
                            start.linkTo(parent.start)
                            // 控件对齐右边
                            end.linkTo(parent.end)
                            // 控件对齐顶部
                            top.linkTo(parent.top)
                            // 控件对齐底部
                            bottom.linkTo(parent.bottom)
                            // 横向位置比例
                            horizontalBias = 0.18f
                            // 竖向位置比例
                            verticalBias = 0.88f
                            // 宽度占父布局宽度的30%
                            width = Dimension.percent(0.3f)
                            // 高度固定70dp
                            height = Dimension.value(70.dp)

                        },
                    enabled = isLogInBtnEnabled, // 可点击
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0.161f, 0.161f, 0.161f, 1.0f), // 背景颜色哦
                        contentColor = Color.Black
                    ), // 内容颜色
                    shape = RoundedCornerShape(18.dp), // 圆角
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 0.dp, pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    ), // 阴影

                )
                {
                    Text(
                        "Log in",
                        fontSize = 15.sp,
                        color = Color(0.937f, 0.937f, 0.937f, 1.0f),
                        fontWeight = FontWeight.Bold, // 粗体
                        textAlign = TextAlign.Center // 剧中对齐
                    )

                }

                // 注册按钮
                Button(
                    onClick = { BtClick("sign_up") },
                    modifier = Modifier
                        .constrainAs(signupBtn) {
                            // 控件对齐左边
                            start.linkTo(parent.start)
                            // 控件对齐右边
                            end.linkTo(parent.end)
                            // 控件对齐顶部
                            top.linkTo(parent.top)
                            // 控件对齐底部
                            bottom.linkTo(parent.bottom)
                            // 横向位置比例
                            horizontalBias = 0.84f
                            // 竖向位置比例
                            verticalBias = 0.88f
                            // 宽度占父布局宽度的30%
                            width = Dimension.percent(0.3f)
                            // 高度固定70dp
                            height = Dimension.value(70.dp)

                        },
                    enabled = isSignUpBtnEnabled, // 可点击
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0.161f, 0.161f, 0.161f, 1.0f), // 背景颜色哦
                        contentColor = Color.Black
                    ), // 内容颜色
                    shape = RoundedCornerShape(18.dp), // 圆角
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 0.dp, pressedElevation = 0.dp,
                        disabledElevation = 0.dp
                    ), // 阴影

                ) {
                    Text(
                        "sign up",
                        fontSize = 15.sp,
                        color = Color(0.937f, 0.937f, 0.937f, 1.0f),
                        fontWeight = FontWeight.Bold, // 粗体
                        textAlign = TextAlign.Center, // 剧中对齐

                    )
                }

                // 欢迎文本标签
                Text(
                    "Welcome to my App😘",
                    color = Color(0.937f, 0.937f, 0.937f, 1.0f),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .constrainAs(text) {
                            // 控件对齐左边
                            start.linkTo(parent.start)
                            // 控件对齐右边
                            end.linkTo(parent.end)
                            // 控件对齐顶部
                            top.linkTo(parent.top)
                            // 控件对齐底部
                            bottom.linkTo(parent.bottom)
                            // 横向位置比例
                            horizontalBias = 0.5f
                            // 竖向位置比例
                            verticalBias = 0.477f
                            // 宽度占父布局宽度的30%
                            width = Dimension.percent(0.9f)
                            // 高度固定70dp
                            height = Dimension.value(50.dp)

                        }

                )


            }
        }


    }


    // 布局管理器
    ConstraintLayout(modifier = Modifier
        .fillMaxSize()
        .zIndex(1f)) {
        // 引用
        val (InputEmail, InputName,InputPassword, InputCode,sentCode,registerBt,loginBtnyi,backBt) = createRefs()
        // 倒计时操作
        LaunchedEffect(countDown) {
            if (countDown in 0..59) {
                if (countDown==0) {
                    countDown=60
                }
                else {
                    delay(1000L)
                    countDown -= 1}
            }
        }

        /*                                 用户数据提交表单                                          */
        // 老用户登录按钮
        if (showloginBt.value) {
            Button(
                onClick = {
                    // 创建JSON文件
                    val json = JSONObject().apply {
                        put("name",nameText)
                        put("email",emailText)
                        put("password",passwordText)
                    }

                    // 构建实例
                    val client = OkHttpClient()
                    // 请求体类型
                    val mediaType = "application/json;charset=utf-8".toMediaType()
                    // 转化请求体
                    val body = json.toString().toRequestBody(mediaType)

                    // 创建构建器
                    val request = Request.Builder()
                        .url("http://192.168.1.249:2000/login")
                        .post(body)
                        .build()


                    // 开启一个协程，分配给另一个线程使用,减少占用
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // 发起通话请求并同步等待服务器响应
                            val response = client.newCall(request).execute()
                            val responseText = response.body?.string() ?: "无返回"

                            // 区分msg和status的值
                            val jsonObject = JSONObject(responseText)
                            val msg = jsonObject.getString("msg")
                            val status = jsonObject.getString("status")

                            // 切回主线程
                            withContext(Dispatchers.Main) {
                                // 处理信息
                                if (msg == "用户存在") {
                                    navController.navigate("Main")
                                } else {
                                    // 显示错误弹窗
                                    dialogMessage = responseText
                                    showDialog = true
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                // 显示错误弹窗或Toast提示
                                dialogMessage = "登录失败：" + e.message
                                showDialog = true
                                e.printStackTrace()
                            }
                        }

                    }

                }, // 数据库查询等操作
                modifier = Modifier
                    .constrainAs(loginBtnyi) {
                        // 控件对齐左边
                        start.linkTo(parent.start)
                        // 控件对齐右边
                        end.linkTo(parent.end)
                        // 控件对齐顶部
                        top.linkTo(parent.top)
                        // 控件对齐底部
                        bottom.linkTo(parent.bottom)
                        // 横向位置比例
                        horizontalBias = 0.45f
                        // 竖向位置比例
                        verticalBias = 0.68f
                        // 宽度占父布局宽度的30%
                        width = Dimension.percent(0.3f)
                        // 高度固定70dp
                        height = Dimension.value(70.dp)

                    },
                enabled = true, // 可点击
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0.161f, 0.161f, 0.161f, 1.0f), // 背景颜色哦
                    contentColor = Color.Black
                ), // 内容颜色
                shape = RoundedCornerShape(18.dp), // 圆角
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 0.dp, pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ), // 阴影

            )
            {
                Text(
                    "Log in",
                    fontSize = 15.sp,
                    color = Color(0.937f, 0.937f, 0.937f, 1.0f),
                    fontWeight = FontWeight.Bold, // 粗体
                    textAlign = TextAlign.Center // 剧中对齐
                )

            }
        }

        // 用户名输入框
        if (showInputName.value) {
            // 邮件输入框
            OutlinedTextField(
                value = nameText, // 当前输入框显示内容
                onValueChange = { nameText = it }, // 添加空实现防止报错
                textStyle = TextStyle(
                    color = Color(
                        0.937f,
                        0.937f,
                        0.937f,
                        1.0f),

                    ),
                label = { Text(text = "Name", fontSize = 14.sp, color = Color(
                    0.937f,
                    0.937f,
                    0.937f,
                    1.0f
                ), fontWeight = FontWeight.Bold
                ) }, // 上方显示标签
                shape = RoundedCornerShape(15.dp), // 圆觉
                placeholder = { Text(
                    text = "Enter Your Name",
                    color = Color(0.678f, 0.843f, 0.561f, 1.0f)
                ) }, // 提示文本
                singleLine = true, // 只允许输入一行
                enabled = true, // 允许输入
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .alpha(alphaAnim)
                    .zIndex(2f) // 图层提高
                    .constrainAs(InputName) {
                        // 控件对齐左边
                        start.linkTo(parent.start)
                        // 控件对齐右边
                        end.linkTo(parent.end)
                        // 控件对齐顶部
                        top.linkTo(parent.top)
                        // 控件对齐底部
                        bottom.linkTo(parent.bottom)
                        // 横向位置比例
                        horizontalBias = 0.4f
                        // 竖向位置比例
                        verticalBias = 0.2f
                        // 宽度占父布局宽度的66%
                        width = Dimension.percent(0.66f)
                        // 高度固定70dp
                        height = Dimension.value(70.dp)

                    })

        }





        // 邮件输入框
        if (showInputEmail.value) {
            // 邮件输入框
            OutlinedTextField(
                value = emailText, // 当前输入框显示内容
                onValueChange = { emailText = it }, // 添加空实现防止报错
                textStyle = TextStyle(
                    color = Color(
                        0.937f,
                        0.937f,
                        0.937f,
                        1.0f),

                ),
                label = { Text(text = "Email", fontSize = 14.sp, color = Color(
                    0.937f,
                    0.937f,
                    0.937f,
                    1.0f
                ), fontWeight = FontWeight.Bold
                ) }, // 上方显示标签
                shape = RoundedCornerShape(15.dp), // 圆觉
                placeholder = { Text(
                    text = "Enter Email Address",
                    color = Color(0.678f, 0.843f, 0.561f, 1.0f)
                ) }, // 提示文本
                singleLine = true, // 只允许输入一行
                enabled = true, // 允许输入
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .alpha(alphaAnim)
                    .zIndex(2f) // 图层提高
                    .constrainAs(InputEmail) {
                        // 控件对齐左边
                        start.linkTo(parent.start)
                        // 控件对齐右边
                        end.linkTo(parent.end)
                        // 控件对齐顶部
                        top.linkTo(parent.top)
                        // 控件对齐底部
                        bottom.linkTo(parent.bottom)
                        // 横向位置比例
                        horizontalBias = 0.4f
                        // 竖向位置比例
                        verticalBias = 0.3f
                        // 宽度占父布局宽度的65%
                        width = Dimension.percent(0.66f)
                        // 高度固定70dp
                        height = Dimension.value(70.dp)

                    })

        }


        // 密码输入框
        if (showInputPassword.value) {
            // 邮件输入框
            OutlinedTextField(
                value = passwordText, // 当前输入框显示内容
                onValueChange = { passwordText = it }, // 添加空实现防止报错
                textStyle = TextStyle(
                    color = Color(
                        0.937f,
                        0.937f,
                        0.937f,
                        1.0f),

                    ),
                label =  { Text(text = "Password", fontSize = 14.sp, color = Color(
                    0.937f,
                    0.937f,
                    0.937f,
                    1.0f
                ), fontWeight = FontWeight.Bold
                ) }, // 上方显示标签
                placeholder = { Text(
                    text = "Enter Password",
                    color = Color(0.678f, 0.843f, 0.561f, 1.0f)
                ) }, // 提示文本
                shape = RoundedCornerShape(15.dp), // 圆觉
                singleLine = true, // 只允许输入一行
                enabled = true, // 允许输入
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .alpha(alphaAnim)
                    .zIndex(2f) // 图层提高
                    .constrainAs(InputPassword) {
                        // 控件对齐左边
                        start.linkTo(parent.start)
                        // 控件对齐右边
                        end.linkTo(parent.end)
                        // 控件对齐顶部
                        top.linkTo(parent.top)
                        // 控件对齐底部
                        bottom.linkTo(parent.bottom)
                        // 横向位置比例
                        horizontalBias = 0.4f
                        // 竖向位置比例
                        verticalBias = 0.4f
                        // 宽度占父布局宽度的65%
                        width = Dimension.percent(0.65f)
                        // 高度固定70dp
                        height = Dimension.value(70.dp)

                    })


        }


        // 验证码输入框
        if (showInputCode.value) {
            // 邮件输入框
            OutlinedTextField(
                value = codeText, // 当前输入框显示内容
                onValueChange = { codeText = it }, // 添加空实现防止报错
                textStyle = TextStyle(
                    color = Color(
                        0.937f,
                        0.937f,
                        0.937f,
                        1.0f),

                    ),
                label =  { Text(text = "Code", fontSize = 14.sp, color = Color(
                    0.937f,
                    0.937f,
                    0.937f,
                    1.0f
                ), fontWeight = FontWeight.Bold
                ) }, // 上方显示标签
                placeholder = { Text(
                    text = "Enter Code",
                    color = Color(0.678f, 0.843f, 0.561f, 1.0f)
                ) }, // 提示文本
                shape = RoundedCornerShape(15.dp), // 圆觉
                singleLine = true, // 只允许输入一行
                enabled = true, // 允许输入
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier
                    .alpha(alphaAnim)
                    .zIndex(2f) // 图层提高
                    .constrainAs(InputCode) {
                        // 控件对齐左边
                        start.linkTo(parent.start)
                        // 控件对齐右边
                        end.linkTo(parent.end)
                        // 控件对齐顶部
                        top.linkTo(parent.top)
                        // 控件对齐底部
                        bottom.linkTo(parent.bottom)
                        // 横向位置比例
                        horizontalBias = 0.25f
                        // 竖向位置比例
                        verticalBias = 0.5f
                        // 宽度占父布局宽度的40%
                        width = Dimension.percent(0.4f)
                        // 高度固定70dp
                        height = Dimension.value(70.dp)

                    })

        }

        if (showSentCodeBt.value) {


            // 登录按钮
            Button(
                onClick = {if (countDown == 60) {
                    countDown = 59 // 开始倒计时
                }

                }, // 按钮点击运行函数
                modifier = Modifier
                    .zIndex(2f)
                    .alpha(alphaAnim)
                    .constrainAs(sentCode) {
                        // 控件对齐左边
                        start.linkTo(parent.start)
                        // 控件对齐右边
                        end.linkTo(parent.end)
                        // 控件对齐顶部
                        top.linkTo(parent.top)
                        // 控件对齐底部
                        bottom.linkTo(parent.bottom)
                        // 横向位置比例
                        horizontalBias = 0.8f
                        // 竖向位置比例
                        verticalBias = 0.5f
                        // 宽度占父布局宽度的30%
                        width = Dimension.percent(0.3f)
                        // 高度固定70dp
                        height = Dimension.value(60.dp)

                    },
                enabled = true, // 可点击
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0.161f, 0.161f, 0.161f, 1.0f), // 背景颜色哦
                    contentColor = Color.Black
                ), // 内容颜色
                shape = RoundedCornerShape(18.dp), // 圆角
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 0.dp, pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )) {Text(
                if (countDown<60) "重新发送(${countDown})" else "Sent Code",
                fontSize = 15.sp,
                color = Color(0.937f, 0.937f, 0.937f, 1.0f),
                fontWeight = FontWeight.Bold, // 粗体
                textAlign = TextAlign.Center, // 剧中对齐

                )}



        }

        if (showDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("确定")
                    }
                },
                title = {
                    Text("注册失败")
                },
                text = {
                    Text(dialogMessage)
                }
            )
        }

        // 返回按钮
        if (backButton.value) {
            IconButton(onClick = {navController.navigate("Start")}, modifier = Modifier.size(30.dp).offset(x = 15.dp, y = 50.dp), content = {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back",tint = Color.White)
            })

        }

        // 最终注册按钮
        if (showregister.value) {
            Button(
                onClick = {
                    navController.navigate("Main")

//                    // 打包email/password是否存在于数据库
//                    val json = JSONObject().apply{
//                        put("name",nameText)
//                        put("email",emailText)
//                        put("password",passwordText)
//                        put("code",codeText)
//                    }
//
//                    // 实例
//                    val client = OkHttpClient()
//
//                    // 声明请求体类型
//                    val mediaType = "application/json; charset=utf-8".toMediaType()
//                    // 转化请求体
//                    val body = json.toString().toRequestBody(mediaType)
//
//                    // 创建构建器
//                    val request = Request.Builder()
//                        .url("http://192.168.1.249:2000/register")
//                        .post(body)
//                        .build()
//
//                    // 开启一个协程，分配给另一个线程使用,减少占用
//                    CoroutineScope(Dispatchers.IO).launch {
//                            try {
//                                // 发起通话请求并同步等待服务器响应
//                                val response = client.newCall(request).execute()
//                                val responseText = response.body?.string() ?: "无饭回"
//
//                                // 区分msg和status的值
//                                val jsonObject = JSONObject(responseText)
//                                val msg = jsonObject.getString("msg")
//                                val status = jsonObject.getString("status")
//
//                                // 切回主线程
//                                withContext(Dispatchers.Main){
//                                    // 处理信息
//                                    if (msg=="注册成功") {
//                                        navController.navigate("Main")
//                                    }
//                                    else {
//                                        // 显示错误弹窗
//                                        dialogMessage = responseText
//                                        showDialog = true
//                                }   }
//                            }
//                            catch (e: Exception) {
//                                withContext(Dispatchers.Main) {
//                                    // 显示错误弹窗或Toast提示
//                                    dialogMessage = "请求失败：" + e.message
//                                    showDialog = true
//                                    e.printStackTrace()
//                                }
//                            }
//
//                    }
                },
                modifier = Modifier
                    .zIndex(2f)
                    .alpha(alphaAnim)
                    .constrainAs(registerBt) {
                        // 控件对齐左边
                        start.linkTo(parent.start)
                        // 控件对齐右边
                        end.linkTo(parent.end)
                        // 控件对齐顶部
                        top.linkTo(parent.top)
                        // 控件对齐底部
                        bottom.linkTo(parent.bottom)
                        // 横向位置比例
                        horizontalBias = 0.5f
                        // 竖向位置比例
                        verticalBias = 0.7f
                        // 宽度占父布局宽度的30%
                        width = Dimension.percent(0.4f)
                        // 高度固定60dp
                        height = Dimension.value(60.dp)

                    },
                enabled = true, // 可点击
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0.161f, 0.161f, 0.161f, 1.0f), // 背景颜色哦
                    contentColor = Color.Black
                ), // 内容颜色
                shape = RoundedCornerShape(13.dp), // 圆角
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 0.dp, pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                )) {Text(
                "Register",
                fontSize = 15.sp,
                color = Color(0.937f, 0.937f, 0.937f, 1.0f),
                fontWeight = FontWeight.Bold, // 粗体
                textAlign = TextAlign.Center, // 剧中对齐

            )}

        }


    }
}






