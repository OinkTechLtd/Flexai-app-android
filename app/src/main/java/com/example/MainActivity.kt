package com.example

import android.annotation.SuppressLint
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.SavedPage
import com.example.ui.theme.*
import com.example.ui.viewmodel.FlexViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContainer()
            }
        }
    }
}

@Composable
fun MainContainer() {
    val context = LocalContext.current
    val viewModel: FlexViewModel = viewModel()
    
    val isOnline by viewModel.isOnline.collectAsState()
    val savedPages by viewModel.savedPages.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    var primaryUrl by remember { mutableStateOf("https://flexai-ru.lovable.app") }
    val backupUrl = "https://flexai-ru.base44.app"
    
    var currentUrl by remember { mutableStateOf(primaryUrl) }
    var pageTitle by remember { mutableStateOf("FLEXAI") }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webProgress by remember { mutableFloatStateOf(0f) }
    var isWebLoading by remember { mutableStateOf(true) }
    
    // UI Panels toggle
    var showSplash by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var alarmMessage by remember { mutableStateOf("") }

    // Coroutine scope
    val scope = rememberCoroutineScope()

    // Request Notification Permission on SDK 33+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
            if (isGranted) {
                Toast.makeText(context, "Уведомления FLEXAI активированы!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Dismiss splash and show home screen after elegant initialization sequence
        delay(2600)
        showSplash = false
    }

    // Helper to perform light vibratory feedback for futuristic physical satisfaction
    val triggerVibe = {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(30, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(CyberDarkBg),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberDarkBg)
        ) {
            // Animated Network starfield background inside the client shell
            StarfieldCanvas()

            Column(modifier = Modifier.fillMaxSize()) {
                // Futuristic Header Element
                TopCyberBar(
                    currentUrl = currentUrl,
                    isOnline = isOnline,
                    isWebLoading = isWebLoading,
                    webProgress = webProgress,
                    onToggleSource = {
                        triggerVibe()
                        currentUrl = if (currentUrl.contains("lovable")) backupUrl else primaryUrl
                        webViewInstance?.loadUrl(currentUrl)
                    },
                    onRefresh = {
                        triggerVibe()
                        webViewInstance?.reload()
                    },
                    onToggleMenu = {
                        triggerVibe()
                        showMenu = !showMenu
                    }
                )

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Core WebView Integration
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        isWebLoading = true
                                        url?.let { currentUrl = it }
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isWebLoading = false
                                        url?.let { currentUrl = it }
                                        view?.title?.let { pageTitle = it }
                                    }

                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        url?.let { view?.loadUrl(it) }
                                        return true
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        super.onProgressChanged(view, newProgress)
                                        webProgress = newProgress / 100f
                                    }
                                }

                                configureWebViewSettings(this, isOnline)
                                loadUrl(currentUrl)
                                webViewInstance = this
                            }
                        },
                        update = { webView ->
                            // Dynamically update cache settings on network drift
                            val settings = webView.settings
                            settings.cacheMode = if (isOnline) {
                                WebSettings.LOAD_DEFAULT
                            } else {
                                WebSettings.LOAD_CACHE_ELSE_NETWORK
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Offline Banner when disconnected from grid
                    if (!isOnline) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(Brush.horizontalGradient(listOf(CyberAlarmRed.copy(alpha = 0.85f), CyberDarkBg)))
                                .align(Alignment.TopCenter)
                                .border(1.dp, CyberAlarmRed.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(CyberAlarmRed, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "АВТОНОМНЫЙ РЕЖИМ • ДОСТУПЕН ЛОКАЛЬНЫЙ КЭШ",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Floating Action Button to instantly capture current page for offline viewing
                    if (isOnline) {
                        FloatingActionButton(
                            onClick = {
                                triggerVibe()
                                webViewInstance?.let { webView ->
                                    // Inject a JS routine to fetch both page title and entire serialised page HTML content securely
                                    webView.evaluateJavascript(
                                        "(function() { return { html: document.documentElement.outerHTML, title: document.title }; })()"
                                    ) { resultJson ->
                                        try {
                                            if (resultJson != null && resultJson != "null") {
                                                // Handle JavaScript output
                                                val token = if (resultJson.startsWith("\"") && resultJson.endsWith("\"")) {
                                                    // Parse escaped json
                                                    val cleanStr = resultJson.substring(1, resultJson.length - 1)
                                                        .replace("\\u003C", "<")
                                                        .replace("\\u003E", ">")
                                                        .replace("\\\"", "\"")
                                                        .replace("\\\\", "\\")
                                                        .replace("\\n", "\n")
                                                    cleanStr
                                                } else {
                                                    resultJson
                                                }

                                                // If parsing raw JSON fails, fallback to simple DOM fetch
                                                val cleanTitle = webView.title ?: "FLEXAI Сохраненная страница"
                                                val hostUrl = webView.url ?: currentUrl
                                                viewModel.savePage(hostUrl, cleanTitle, token)
                                                scope.launch {
                                                    Toast.makeText(context, "Страница успешно сохранена для просмотра оффлайн!", Toast.LENGTH_LONG).show()
                                                    alarmMessage = "Успешно: '$cleanTitle' теперь в вашем оффлайн-кэше!"
                                                    delay(3000)
                                                    alarmMessage = ""
                                                }
                                            }
                                        } catch (e: Exception) {
                                            // Fallback
                                            webView.evaluateJavascript("document.documentElement.outerHTML") { rawHtml ->
                                                val clean = rawHtml.trim('"')
                                                    .replace("\\u003C", "<")
                                                    .replace("\\u003E", ">")
                                                    .replace("\\n", "\n")
                                                    .replace("\\\"", "\"")
                                                    .replace("\\\\", "\\")
                                                viewModel.savePage(webView.url ?: currentUrl, webView.title ?: "Страница FLEXAI", clean)
                                                Toast.makeText(context, "Страница сохранена (резервный метод)!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            },
                            containerColor = CyberPrimary,
                            contentColor = Color.Black,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(20.dp)
                                .testTag("save_offline_page_fab")
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Сохранить для оффлайн")
                            }
                        }
                    }
                }
            }

            // Animated slide-up/fade Control Center Menu Overlay
            AnimatedVisibility(
                visible = showMenu,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                ControlDeckPanel(
                    viewModel = viewModel,
                    savedPages = savedPages,
                    onDismiss = {
                        triggerVibe()
                        showMenu = false
                    },
                    onLoadOfflinePage = { savedPage ->
                        triggerVibe()
                        webViewInstance?.let { webView ->
                            // Load saved HTML directly with the original base URL so stylesheets, images run elegantly if cached
                            webView.loadDataWithBaseURL(
                                savedPage.url,
                                savedPage.htmlContent,
                                "text/html",
                                "UTF-8",
                                null
                            )
                            currentUrl = savedPage.url
                            pageTitle = savedPage.title
                        }
                        showMenu = false
                        Toast.makeText(context, "Отображается сохраненная копия от ${java.text.SimpleDateFormat("dd.MM.HH:mm").format(java.util.Date(savedPage.savedAt))}", Toast.LENGTH_LONG).show()
                    }
                )
            }

            // Modern, Animated, Minimalist GIGA-themed Splash Cover Overlay
            AnimatedVisibility(
                visible = showSplash,
                exit = shrinkOut(shrinkTowards = Alignment.Center) + fadeOut(animationSpec = tween(durationMillis = 800))
            ) {
                GigaCoverSplash()
            }

            // Temporary alert message HUD at bottom
            if (alarmMessage.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(CyberSecondary.copy(alpha = 0.95f), CyberPrimary.copy(alpha = 0.95f))))
                        .border(1.dp, CyberPrimary, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = alarmMessage,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun configureWebViewSettings(webView: WebView, isOnline: Boolean) {
    val settings = webView.settings
    settings.javaScriptEnabled = true
    settings.domStorageEnabled = true
    settings.databaseEnabled = true
    settings.allowFileAccess = true
    settings.allowContentAccess = true
    settings.loadWithOverviewMode = true
    settings.useWideViewPort = true
    settings.javaScriptCanOpenWindowsAutomatically = true
    settings.mediaPlaybackRequiresUserGesture = false
    
    // User Agent tweak for smooth rendering of modern web frameworks
    settings.userAgentString = settings.userAgentString + " FLEXAI-Android-Applet"

    // Configure client-side caches
    settings.cacheMode = if (isOnline) {
        WebSettings.LOAD_DEFAULT
    } else {
        WebSettings.LOAD_CACHE_ELSE_NETWORK
    }
}

@Composable
fun TopCyberBar(
    currentUrl: String,
    isOnline: Boolean,
    isWebLoading: Boolean,
    webProgress: Float,
    onToggleSource: () -> Unit,
    onRefresh: () -> Unit,
    onToggleMenu: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberDarkBg)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branded Logo with animated pulsating indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onToggleMenu() }
            ) {
                // Interactive breathing indicator
                val transition = rememberInfiniteTransition(label = "pulse")
                val alphaPulse by transition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .alpha(if (isOnline) alphaPulse else 1f)
                        .background(if (isOnline) CyberPrimary else CyberAlarmRed, CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "FLEXAI",
                    color = CyberPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    modifier = Modifier.testTag("app_title_text")
                )
            }

            // Web Url Display Box & Source Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurface)
                    .border(0.5.dp, CyberPrimary.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (currentUrl.contains("lovable")) Icons.Default.Home else Icons.Default.Info,
                    contentDescription = "Провайдер",
                    tint = CyberPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                
                val currentHost = if (currentUrl.contains("lovable")) "FLEXAI [Основной]" else "FLEXAI [Резерв]"
                Text(
                    text = currentHost,
                    color = CyberTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Quick toggle host button
                Box(
                    modifier = Modifier
                        .clickable { onToggleSource() }
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "СМЕНИТЬ",
                        color = CyberPrimary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Utility Navigation Controllers
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(28.dp).testTag("refresh_webview_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Обновить страницы",
                        tint = CyberTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onToggleMenu,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyberPrimary.copy(alpha = 0.15f))
                        .border(0.5.dp, CyberPrimary, RoundedCornerShape(6.dp))
                        .testTag("open_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Режимы и оффлайн",
                        tint = CyberPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = if (isOnline) CyberPrimary.copy(alpha = 0.15f) else CyberAlarmRed.copy(alpha = 0.15f)
        )

        // Precision loading bar running below headers
        AnimatedVisibility(
            visible = isWebLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                progress = { webProgress },
                color = CyberPrimary,
                trackColor = CyberSurface,
                modifier = Modifier.fillMaxWidth().height(2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlDeckPanel(
    viewModel: FlexViewModel,
    savedPages: List<SavedPage>,
    onDismiss: () -> Unit,
    onLoadOfflinePage: (SavedPage) -> Unit
) {
    val sleepEnabled by viewModel.sleepEnabled.collectAsState()
    val sleepHour by viewModel.sleepHour.collectAsState()
    val sleepMinute by viewModel.sleepMinute.collectAsState()

    val restEnabled by viewModel.restEnabled.collectAsState()
    val restInterval by viewModel.restInterval.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onDismiss() } // Tap outside dismisses
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(CyberSurface)
                .border(2.dp, Brush.verticalGradient(listOf(CyberPrimary, Color.Transparent)), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .clickable(enabled = false) {} // block clickthrough
                .padding(20.dp)
        ) {
            // Drag handle / Panel header line
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .background(CyberPrimary.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ПАНЕЛЬ УПРАВЛЕНИЯ FLEXAI",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Embedded tabs inside a simple scrolling area
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION: HEALTH & REST SCHEDULER
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberDarkBg)
                            .border(0.5.dp, CyberPrimary.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Пуши", tint = CyberPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ЗДОРОВЬЕ И ТАЙМЕРЫ ОТДЫХА", color = CyberPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // SLEEP REMINDER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Напоминание о сне 🌌", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Сигнал, что пора ложиться спать", color = CyberTextSecondary, fontSize = 11.sp)
                            }

                            Switch(
                                checked = sleepEnabled,
                                onCheckedChange = { viewModel.toggleSleepReminder(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = CyberPrimary,
                                    uncheckedThumbColor = CyberTextSecondary,
                                    uncheckedTrackColor = CyberSurface
                                )
                            )
                        }

                        if (sleepEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            // Custom Time slider selector for Hour
                            Text(
                                text = "Время сна: ${String.format("%02d", sleepHour)}:${String.format("%02d", sleepMinute)}",
                                color = CyberPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = sleepHour.toFloat(),
                                onValueChange = { viewModel.toggleSleepReminder(true, hour = it.toInt()) },
                                valueRange = 18f..24f,
                                steps = 6,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberPrimary,
                                    activeTrackColor = CyberPrimary,
                                    inactiveTrackColor = CyberSurface
                                )
                            )
                        }

                        HorizontalDivider(color = CyberSurface, modifier = Modifier.padding(vertical = 12.dp))

                        // PERIODIC DURATION BREAK TIMERS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Перерывы на отдых 🧘", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Периодическое напоминание отдохнуть", color = CyberTextSecondary, fontSize = 11.sp)
                            }

                            Switch(
                                checked = restEnabled,
                                onCheckedChange = { viewModel.toggleRestReminder(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = CyberPrimary,
                                    uncheckedThumbColor = CyberTextSecondary,
                                    uncheckedTrackColor = CyberSurface
                                )
                            )
                        }

                        if (restEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Интервал напоминания: каждый(е) $restInterval минут(ы)",
                                color = CyberPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Slider(
                                value = restInterval.toFloat(),
                                onValueChange = { viewModel.toggleRestReminder(true, intervalMinutes = it.toInt()) },
                                valueRange = 15f..120f,
                                steps = 7,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberPrimary,
                                    activeTrackColor = CyberPrimary,
                                    inactiveTrackColor = CyberSurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Testing simulator actions
                        Text("ТЕСТ СИСТЕМЫ УВЕДОМЛЕНИЙ ПРЯМО СЕЙЧАС:", color = CyberTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerSimulatedNotification("sleep") },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberSecondary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("ТС Сон", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }

                            Button(
                                onClick = { viewModel.triggerSimulatedNotification("rest") },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberAccent.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                border = ButtonDefaults.outlinedButtonBorder,
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("ТС Отдых", color = CyberAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }

                            Button(
                                onClick = { viewModel.triggerSimulatedNotification("site") },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("ТС Сайт", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // SECTION: OFFLINE DATABASE PAGES
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberDarkBg)
                            .border(0.5.dp, CyberPrimary.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.List, contentDescription = "Кэш", tint = CyberPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ОФФЛАЙН КАТАЛОГ СТРАНИЦ", color = CyberPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${savedPages.size} СТР.", color = CyberPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (savedPages.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Оффлайн каталог пока пуст.\nНажмите зеленую кнопку (иконка скачивания) на панели внизу при просмотре сайта, чтобы сохранить любую страницу.",
                                    color = CyberTextSecondary,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = FontFamily.SansSerif,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                savedPages.forEach { page ->
                                    SavedPageItemRow(
                                        page = page,
                                        onLoad = { onLoadOfflinePage(page) },
                                        onDelete = { viewModel.deletePage(page.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedPageItemRow(
    page: SavedPage,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CyberSurface)
            .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = page.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = page.url,
                color = CyberTextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clickable { onLoad() }
                    .clip(RoundedCornerShape(6.dp))
                    .background(CyberPrimary)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ОТКРЫТЬ",
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить страницу",
                    tint = CyberAlarmRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// Sleek Giga minimalist cover / Launch animation
@Composable
fun GigaCoverSplash() {
    val transition = rememberInfiniteTransition(label = "rotation")
    
    // Rotating geometric dials
    val rotationAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing pulse for neon logo center
    val breathingLogoSize by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberDarkBg),
        contentAlignment = Alignment.Center
    ) {
        // High fidelity techno star grid background
        StarfieldCanvas()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Minimalist Emblem Ring / Central Core (GIGA cover concept)
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(breathingLogoSize),
                contentAlignment = Alignment.Center
            ) {
                // outer ring matching the background
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotationAngle)
                ) {
                    drawArc(
                        color = CyberPrimary.copy(alpha = 0.15f),
                        startAngle = 0f,
                        sweepAngle = 120f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = CyberAccent.copy(alpha = 0.25f),
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner core glowing cylinder Vector Shape
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(CyberPrimary.copy(alpha = 0.2f), Color.Transparent)))
                        .border(1.5.dp, CyberPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Stylized technological GIGA letter representations 'F'
                    Text(
                        text = "F",
                        color = CyberPrimary,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(x = (-2).dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Subdued cyber logo
            Text(
                text = "FLEXAI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.offset(x = 5.dp) // center visual balance for letter-spacing offset
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tech specs status lines
            Text(
                text = "ARTIFICIAL COGNITIVE WEB INTERFACE",
                fontSize = 9.sp,
                color = CyberPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Minimal futuristic loader sequence
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    color = CyberPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "ESTABLISHING SECURE CONNECTION...",
                    fontSize = 8.sp,
                    color = CyberTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// An elegant particle/star Canvas drawing representation for that epic futuristic feel
@Composable
fun StarfieldCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stars"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Let's seed static coordinates for stars representing clean space field
        val seedCoordinates = listOf(
            Offset(width * 0.15f, height * 0.22f),
            Offset(width * 0.85f, height * 0.14f),
            Offset(width * 0.45f, height * 0.35f),
            Offset(width * 0.72f, height * 0.48f),
            Offset(width * 0.25f, height * 0.65f),
            Offset(width * 0.90f, height * 0.78f),
            Offset(width * 0.55f, height * 0.88f),
            Offset(width * 0.10f, height * 0.52f),
            Offset(width * 0.62f, height * 0.24f),
            Offset(width * 0.38f, height * 0.76f)
        )

        // Draw small micro stars
        seedCoordinates.forEach { offset ->
            drawCircle(
                color = CyberPrimary.copy(alpha = starAlpha * 0.3f),
                radius = 2.dp.toPx(),
                center = offset
            )
            drawCircle(
                color = Color.White.copy(alpha = starAlpha * 0.6f),
                radius = 1.dp.toPx(),
                center = offset
            )
        }
    }
}
