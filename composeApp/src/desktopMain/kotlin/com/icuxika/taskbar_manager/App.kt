package com.icuxika.taskbar_manager

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Collections.emptyList

@Composable
@Preview
fun App() {
    MaterialTheme {
        var mainWindowList by remember { mutableStateOf<List<MainWindowInfo>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        val scope = MainScope()
        val lazyListState = rememberLazyListState()
        val windowManager = remember { WindowManager() }

        LaunchedEffect(Unit) {
            while (true) {
                mainWindowList = withContext(Dispatchers.IO) {
                    getWindowInfoList()
                }
                delay(10000)
            }
        }

        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF61d8f0),
                            Color(0xFF93b9e0),
                            Color(0xFFc3dcf5)
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Windows 任务栏程序列表",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                mainWindowList = withContext(Dispatchers.IO) {
                                    getWindowInfoList()
                                }
                                isLoading = false
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            }

            Text(
                text = "当前运行程序: ${mainWindowList.size} 个",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (mainWindowList.isEmpty() && !isLoading) {
                Text(
                    text = "未获取到运行程序数据或运行出错",
                    fontSize = 16.sp,
                    color = Color(0xFFEF5350),
                    modifier = Modifier.padding(16.dp)
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    items(mainWindowList) { mainWindowInfo ->
                        MainWindowItem(mainWindowInfo) {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    windowManager.activateWindow(mainWindowInfo.mainWindowHandle.toLong())
                                }
                            }
                        }
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = lazyListState
                    )
                )
            }

        }
    }
}

@Composable
fun MainWindowItem(mainWindowInfo: MainWindowInfo, onActivate: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true)
            ) { onActivate() },
        elevation = if (isHovered) 4.dp else 2.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = if (isHovered)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            // 窗口标题
            Text(
                text = mainWindowInfo.mainWindowTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 进程信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "进程: ${mainWindowInfo.processName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Text(
                    text = "PID: ${mainWindowInfo.id}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

data class MainWindowInfo(
    val id: String,
    val processName: String,
    val mainWindowTitle: String,
    val mainWindowHandle: String
)

fun getWindowInfoList(): MutableList<MainWindowInfo> {
    try {
        val windowInfoList = mutableListOf<MainWindowInfo>()

        val processBuilder = ProcessBuilder(
            "powershell",
            "-Command",
            $$"Get-Process | Where-Object { $_.MainWindowTitle -ne '' } | Select-Object Id, ProcessName, MainWindowTitle, MainWindowHandle | ConvertTo-Csv -NoTypeInformation",
        )
        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream, Charsets.UTF_8))
        var line = reader.readLine()
        while (reader.readLine()?.also { line = it } != null) {
            val parts = line.split(',').map { it.trim('"') }
            if (parts.size >= 4) {
                val windowInfo = MainWindowInfo(parts[0], parts[1], parts[2], parts[3])
                windowInfoList.add(windowInfo)
            }
        }
        process.waitFor()
        return windowInfoList
    } catch (e: Exception) {
        println("获取窗口信息失败: ${e.message}")
        return emptyList()
    }
}