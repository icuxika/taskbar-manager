package com.icuxika.taskbar_manager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import taskbar_manager.composeapp.generated.resources.Res
import taskbar_manager.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        val scope = MainScope()
        Column(
            modifier = Modifier
                .safeContentPadding()
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Windows 任务栏程序列表",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                delay(3000)
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
                text = "当前运行程序: 27 个",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val listState = rememberLazyListState()
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    items(mainWindowInfoList) { mainWindowInfo ->
                        MainWindowItem(mainWindowInfo) {}
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
    val mainWindowTitle: String,
    val mainWindowHandle: String
)

val mainWindowInfoList = listOf(
    MainWindowInfo("窗口1", "123"),
    MainWindowInfo("窗口2", "456"),
    MainWindowInfo("窗口3", "789"),
    MainWindowInfo("窗口4", "101112"),
    MainWindowInfo("窗口5", "131415"),
    MainWindowInfo("窗口6", "161718"),
    MainWindowInfo("窗口7", "192021"),
    MainWindowInfo("窗口8", "222324"),
    MainWindowInfo("窗口9", "252627"),
    MainWindowInfo("窗口10", "282930"),
    MainWindowInfo("窗口11", "313233"),
    MainWindowInfo("窗口12", "343536"),
    MainWindowInfo("窗口13", "373839"),
    MainWindowInfo("窗口14", "404142"),
    MainWindowInfo("窗口15", "434445"),
    MainWindowInfo("窗口16", "464748"),
    MainWindowInfo("窗口17", "495051"),
    MainWindowInfo("窗口18", "525354"),
    MainWindowInfo("窗口19", "555657"),
    MainWindowInfo("窗口20", "585960"),
    MainWindowInfo("窗口21", "616263"),
    MainWindowInfo("窗口22", "646566"),
    MainWindowInfo("窗口23", "676869"),
    MainWindowInfo("窗口24", "707172"),
    MainWindowInfo("窗口25", "737475"),
    MainWindowInfo("窗口26", "767778"),
    MainWindowInfo("窗口27", "798081"),
)
