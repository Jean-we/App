package com.example.jeansapplication

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import com.konovalov.vad.webrtc.VadWebRTC
import com.konovalov.vad.webrtc.config.FrameSize
import com.konovalov.vad.webrtc.config.Mode
import com.konovalov.vad.webrtc.config.SampleRate
import kotlin.concurrent.thread


@Composable
fun MainPageManner(navController: NavHostController) {
    val  pageManner = rememberNavController()

    NavHost(navController = pageManner, startDestination = "Chat"){
        composable("Chat"){ ChatPage(pageManner) }
        composable("VideoCalling"){ VideoCalling(pageManner) }
        composable("AllOn"){ allOn(pageManner) }
        composable("AddContacts"){AddContacts(pageManner)}
    }
}



class VideoProcessing: ComponentActivity() {
    // 请求码
    private val REQUEST_CODE_PERMISSIONS = 10
    // 数组变量，包含了App运行时需要的权限
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO)


    // 权限判断函数
    private fun allPermissionsGranted(): Boolean {
        // 判断权限
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                this, it
            ) == PackageManager.PERMISSION_GRANTED



        }
    }

    // 判断函数
    fun upoff(): Boolean {
        // 判断权限是否开启
        return if (allPermissionsGranted()) {
            true
        } else {
            false
        }
    }

}


// 视频显示函数
@Composable
fun VideoCalling(navController: NavHostController){
    // 上下文
    val context = LocalContext.current
    // 获取当前环境生命周期控制者
    val lifecycleOwner = LocalLifecycleOwner.current
    // 保证不会每次重组都会创建PreviewView
    val previewView = remember {androidx.camera.view.PreviewView(context)} // 每个需要调用服务的功能都需要Context
    // 异步获取CameraX摄像头管理器的实例
    val cameraProviderFuture = remember {androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)}

    val hasAudioPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    if (hasAudioPermission) {
        // 嵌入android原生view
        AndroidView(
            factory = { previewView },// 原声view
            modifier = Modifier.height(400.dp).width(350.dp).clip(RoundedCornerShape(25.dp)),
            update = {
                // 拿到实例
                val camara = cameraProviderFuture.get()
                // 创建预览实例
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider) // 把画面输出给 PreviewView
                }
                // 后置摄像头
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    camara.unbindAll() // 清除已有的绑定（避免冲突）
                    camara.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview // 绑定生命周期 + 用例
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        )
        thread(start = true,name = "语音监听线程") {
            // 修复：完善 AudioFormat 配置
            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)  // 设置编码为 16 位 PCM
                .setSampleRate(44100)                         // 设置采样率为 44.1kHz
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)  // 设置为单声道
                .build()

            // 修复：使用完整配置的 AudioFormat
            val audioRecord = AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .setAudioFormat(audioFormat)                  // 添加必要的音频格式
                .setPrivacySensitive(true)
                .setContext(context)
                .setBufferSizeInBytes(2048)
                .build()

            audioRecord.startRecording()
            // 缓冲区
            val bufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT)
            val pcmarrays= ShortArray(320)

            // 缓冲区达到指定指定大小读取PCM数据
            while (true) {
                val readdatasize = audioRecord.read(pcmarrays,0,320)

                // VAD配置
                val vad = VadWebRTC(
                    sampleRate = SampleRate.SAMPLE_RATE_16K, // 采样率
                    frameSize = FrameSize.FRAME_SIZE_320, // // 帧大小：320个采样点（约20ms音频）
                    mode = Mode.VERY_AGGRESSIVE, // 检测严格程度，越激进越容易把静音判断为无语音
                    silenceDurationMs = 300, // 语音检测结束值
                    speechDurationMs = 50 // 开始语音的时间门限
                )

                if (readdatasize == 320) {
                    if (vad.isSpeech(pcmarrays)) {
                        // 检测到人声！
                    } else {

                    }
                }


            }

        }

    }
}



// 判断权限是否全部开启函数
@Composable
fun allOn(navController: NavHostController) {
    val context = LocalContext.current

    val hasPermission = arrayOf(Manifest.permission.CAMERA).all {
        ContextCompat.checkSelfPermission(
            context, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    if (hasPermission) {
        navController.navigate("VideoCalling")
    } else {
        Toast.makeText(context, "权限未授权", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun ChatPage(navController: NavHostController) {
    // 路由（页面ID）
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showWindow = remember { mutableStateOf(false) }

    // 小窗口弹出动画
    val offsetY by animateDpAsState(
        targetValue = if (showWindow.value) 310.dp else 800.dp,
        animationSpec = tween(
            durationMillis = 1100,
            easing = FastOutSlowInEasing
        )
    )


    // 背景图层阴影变量
    val isbackgroundshadow = remember { mutableStateOf(false) }


    // 我的页面弹出动画
    val showMyPage = remember { mutableStateOf(false) }


    // 联系人聊天卡是否可以点击变量
    val isContactsClick = remember { mutableStateOf(true) }
    if (showWindow.value) {
        isContactsClick.value = false
    }

    // 个人头像框按钮是否可点击变量
    val isAvatarFrameClick = remember { mutableStateOf(true) }
    if (showWindow.value) {
        isAvatarFrameClick.value = false
    }
    // 聊天按钮是否可以点击变量
    val chatBtIsClick by remember { mutableStateOf(true) }




    // 一体 聊天卡片
    @Composable
    fun ChatCard() {

        Column(modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = isContactsClick.value) {}) {
                // 头像框
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .width(100.dp)
                ) {
                    Image(
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .height(60.dp)
                            .width(60.dp)
                            .clip(RoundedCornerShape(11.dp)),
                        contentDescription = "avatar",
                        painter = painterResource(id = R.drawable.testavatar)
                    )
                }

                // 聊天框
                Box(
                    modifier = Modifier
                        .width(270.dp)
                        .height(60.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Color(0.161f, 0.161f, 0.161f, 1.0f)
                        )
                ) {
                    // 信息
                    Text(
                        modifier = Modifier.padding(start = 30.dp, top = 15.dp),
                        text = "Message",
                        fontSize = 16.sp,
                        color = Color(0.937f, 0.937f, 0.937f, 1.0f),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center // 文字居中
                    )


                }


            }


        }
    }


    // 创建drawerState
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        drawerState = drawerState,
        gesturesEnabled = true, // 滑动边缘手势
        scrimColor = Color.Black.copy(alpha = 0.52f), // 背景层颜色
        // 我的小页面显示内容
        drawerContent = {
            // 页面管理器
            Column(modifier = Modifier
                .fillMaxHeight().width(300.dp).background(Color(0.122f, 0.122f, 0.122f, 1.0f)))
            {
                ListItem(
                    modifier = Modifier.background(Color(0.122f, 0.122f, 0.122f, 1.0f)).clickable{},
                    colors = ListItemDefaults.colors(Color(0.122f, 0.122f, 0.122f, 1.0f)),
                    headlineContent = {Text(text = "Jean",color = Color(0.937f, 0.937f, 0.937f, 1.0f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold)}, // 主标题
                    supportingContent = {Text(text = "View profile",color = Color(0.702f, 0.702f, 0.702f, 1.0f), fontSize = 10.sp)},
                            // 左侧内容
                    leadingContent = {
                        // 个人头像框及选项
                        Image(
                            modifier = Modifier
                                .height(65.dp)
                                .width(65.dp)
                                .clip(CircleShape)
                                .clickable(
                                    enabled = isAvatarFrameClick.value,
                                ) {},
                            painter = painterResource(id = R.drawable.avatar),
                            contentDescription = "avatar"

                        )
                    },
                    tonalElevation = 15.dp

                )


            }







        }) {

    ConstraintLayout(modifier = Modifier
        .fillMaxSize()
        .zIndex(0f)) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0.071f, 0.071f, 0.071f, 1.0f))
        ) {


            // 搜索区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .zIndex(0f)
            )
            {

                // 个人头像框及选项
                Image(
                    modifier = Modifier
                        .height(50.dp)
                        .width(50.dp)
                        .padding(start = 10.dp, top = 10.dp)
                        .clip(CircleShape)
                        .clickable(
                            enabled = isAvatarFrameClick.value,
                        ) {scope.launch { drawerState.open() }},
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = "avatar"

                )
            }


            // 聊天框显示区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(4f)
                    .zIndex(0f)
            )
            {
                ChatCard()
            }


            // 导航栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .weight(0.3f)
                    .zIndex(0f)
            )

            {


                // 聊天图标动画
                val interactionSource1 = remember { MutableInteractionSource() } // 事件状态库
                val isPressed1 by interactionSource1.collectIsPressedAsState() // 绑定按压事件

                // 缩放动画核心部分
                val scale1 by animateFloatAsState(
                    targetValue = if (isPressed1) 0.75f else 1f
                )


                // 添加图标
                val interactionSource3 = remember { MutableInteractionSource() } // 事件状态库
                val isPressed3 by interactionSource3.collectIsPressedAsState() // 绑定按压事件

                // 缩放动画核心部分
                val scale3 by animateFloatAsState(
                    targetValue = if (isPressed3) 0.65f else 1f
                )


                // 聊天图标
                Icon(
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp)
                        .scale(scale1)
                        .clickable(
                            enabled = if (showWindow.value) true else false,
                            interactionSource = interactionSource1,
                            indication = null
                        ) { navController.navigate("Chat") },
                    painter = painterResource(id = R.drawable.chatlcon),
                    tint = if (currentRoute == "Chat") Color(
                        1.0f,
                        1.0f,
                        1.0f,
                        1.0f
                    ) else Color.Unspecified,
                    contentDescription = "Chat" // 无障碍描述
                )

                // 添加图标
                Icon(
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                        .scale(scale3)
                        .clickable(
                            enabled = true,
                            interactionSource = interactionSource3,
                            indication = null
                        ) { showWindow.value = true },
                    painter = painterResource(id = R.drawable.createlcon),
                    tint = Color.Unspecified,
                    contentDescription = "Create" // 无障碍描述
                )

            }
        }

        // 阴影图层
        if (isbackgroundshadow.value) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0f, 0f, 0f, 0.5f)) // 黑色半透明遮罩层
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .zIndex(1f)
            )
        }

        // 小窗口页面显示
        if (showWindow.value) {
            Box(
                modifier = Modifier
                    .zIndex(2f)
                    .height(500.dp)
                    .width(400.dp)
                    .padding(bottom = 30.dp)
            ) {

                Column(
                    modifier = Modifier
                        .offset(y = offsetY)
                        .clip(RoundedCornerShape(20.dp))
                        .height(500.dp)
                        .width(350.dp)
                        .background(Color(0.122f, 0.122f, 0.122f, 1.0f))
                        .align(Alignment.Center)
                        .padding(bottom = 30.dp),
                ) {


                    // 添加联系人按钮区域
                    Box(
                        modifier = Modifier
                            .padding(top = 15.dp, start = 15.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .width(320.dp)
                            .height(100.dp)
                            .background(Color(0.122f, 0.122f, 0.122f, 1.0f))
                            .clickable(enabled = true) {}
                    ) {

                        // 圆形图标显示
                        Icon(
                            modifier = Modifier
                                .width(90.dp)
                                .height(90.dp)
                                .padding(start = 5.dp, top = 15.dp),
                            painter = painterResource(id = R.drawable.contactlcon),
                            tint = Color.Unspecified,
                            contentDescription = "Create" // 无障碍描述
                        )

                        // 功能文字
                        Text(
                            text = "Add Contacts",
                            modifier = Modifier
                                .width(200.dp)
                                .height(50.dp)
                                .padding(start = 95.dp, top = 30.dp),
                            color = Color(0.937f, 0.937f, 0.937f, 1.0f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Left
                        )

                        // 说明文字
                        Text(
                            text = "Make new friends and chat away!!",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(start = 95.dp, top = 57.dp),
                            color = Color(0.62f, 0.62f, 0.62f, 1.0f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Left
                        )

                    }


                    // 视频通话按钮区域
                    Box(
                        modifier = Modifier
                            .padding(top = 15.dp, start = 15.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .width(320.dp)
                            .height(100.dp)
                            .background(Color(0.122f, 0.122f, 0.122f, 0.102f))
                            .clickable(enabled = true) {
                                navController.navigate("AllOn")
                            }
                    ) {
                        // 图形显示区域
                        Icon(
                            modifier = Modifier
                                .width(90.dp)
                                .height(90.dp)
                                .padding(start = 5.dp, top = 15.dp),
                            painter = painterResource(id = R.drawable.videocallinglcon),
                            tint = Color.Unspecified,
                            contentDescription = "Video Calling" // 无障碍描述
                        )

                        // 功能文字
                        Text(
                            text = "Video Calling",
                            modifier = Modifier
                                .width(200.dp)
                                .height(50.dp)
                                .padding(start = 95.dp, top = 28.dp),
                            color = Color(0.937f, 0.937f, 0.937f, 1.0f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Left
                        )

                        // 说明文字
                        Text(
                            text = "Tap to talk face-to-face — it’s like teleporting your voice (and face)! \uD83D\uDE80",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .padding(start = 95.dp, top = 52.dp),
                            color = Color(0.62f, 0.62f, 0.62f, 1.0f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Left
                        )
                    }


                }
            }
            isbackgroundshadow.value = true
        }

    }
    }
}




// 添加联系人页面
@Composable
fun AddContacts(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0.122f, 0.122f, 0.122f, 1.0f))){
        // 账户按钮
        Box(modifier = Modifier.fillMaxWidth().height(100.dp)){
            Text(text = "Name",
                textAlign = TextAlign.Center, // 文本局中
                modifier = Modifier.width(100.dp).height(50.dp))
        }
        
    }

}





















