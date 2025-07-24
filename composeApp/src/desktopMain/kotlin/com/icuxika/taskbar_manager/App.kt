package com.icuxika.taskbar_manager

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
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
                    color = MaterialTheme.colorScheme.primary
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (mainWindowList.isEmpty() && !isLoading) {
                Text(
                    text = "未获取到运行程序数据或运行出错",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(16.dp)
                )
            }

            val listState = rememberLazyListState()
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    items(mainWindowList) { mainWindowInfo ->
                        MainWindowItem(mainWindowInfo) {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    activateWindow(mainWindowInfo.mainWindowHandle)
                                }
                            }
                        }
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = listState
                    )
                )
            }

        }
    }
}

@Composable
fun MainWindowItem(mainWindowInfo: MainWindowInfo, onActivate: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActivate() },
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
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
                    text = "进程: ${mainWindowInfo.mainWindowTitle}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Text(
                    text = "PID: ${mainWindowInfo.mainWindowTitle}",
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
            "powershell", "-Command", $$"""
                Get-Process | Where-Object { $_.MainWindowTitle -ne "" } | Select-Object Id, ProcessName, MainWindowTitle, MainWindowHandle | ConvertTo-Csv -NoTypeInformation
            """.trimIndent()
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

fun activateWindow(mainWindowHandle: String) {
    try {
        val processBuilder = ProcessBuilder(
            "powershell", "-Command", $$"""
        Add-Type -TypeDefinition '
            using System;
            using System.Runtime.InteropServices;
            public class Win32 {
                [DllImport("user32.dll")]
                public static extern bool SetForegroundWindow(IntPtr hWnd);
                [DllImport("user32.dll")]
                public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
                [DllImport("user32.dll")]
                public static extern bool IsIconic(IntPtr hWnd);
            }
        ';
        $handle = [IntPtr]$$mainWindowHandle;
        if ([Win32]::IsIconic($handle)) {
            [Win32]::ShowWindow($handle, [Win32]::SW_RESTORE);
        }
        [Win32]::SetForegroundWindow($handle);
            """.trimIndent()
        )
        val process = processBuilder.start()
        process.waitFor()
    } catch (e: Exception) {
        println("激活窗口失败: ${e.message}")
    }
}

val mainWindowInfoList = listOf(
    MainWindowInfo("1", "chrome.exe", "Google Chrome", "0x12345678"),
    MainWindowInfo("2", "notepad.exe", "无标题 - 记事本", "0x87654321"),
    MainWindowInfo("3", "chrome.exe", "Google Chrome", "0x12345678"),
    MainWindowInfo("4", "notepad.exe", "无标题 - 记事本", "0x87654321"),
    MainWindowInfo("5", "chrome.exe", "Google Chrome", "0x12345678"),
    MainWindowInfo("6", "notepad.exe", "无标题 - 记事本", "0x87654321"),
    MainWindowInfo("7", "chrome.exe", "Google Chrome", "0x12345678"),
    MainWindowInfo("8", "notepad.exe", "无标题 - 记事本", "0x87654321"),
    MainWindowInfo("9", "chrome.exe", "Google Chrome", "0x12345678"),
    MainWindowInfo("10", "notepad.exe", "无标题 - 记事本", "0x87654321"),
    MainWindowInfo("11", "chrome.exe", "Google Chrome", "0x12345678"),
    MainWindowInfo("12", "notepad.exe", "无标题 - 记事本", "0x87654321"),
    MainWindowInfo("13", "chrome.exe", "Google Chrome", "0x12345678"),
    MainWindowInfo("14", "notepad.exe", "无标题 - 记事本", "0x87654321"),
    MainWindowInfo("15", "chrome.exe", "Google Chrome", "0x12345678"),
    MainWindowInfo("16", "notepad.exe", "无标题 - 记事本", "0x87654321"),
)
