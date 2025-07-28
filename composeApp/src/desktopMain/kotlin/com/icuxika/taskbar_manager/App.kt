package com.icuxika.taskbar_manager

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.AdminPanelSettings
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
import java.lang.foreign.MemorySegment
import java.util.Collections.emptyList
import kotlin.math.absoluteValue

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
                    windowManager.getWindows()
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
                            Color(0xFFE3F2FD), // 更浅的蓝色
                            Color(0xFFBBDEFB),
                            Color(0xFF90CAF9)
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 精简的标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Windows 任务栏程序列表",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "当前运行程序: ${mainWindowList.size} 个",
                        fontSize = 12.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                mainWindowList = withContext(Dispatchers.IO) {
                                    windowManager.getWindows()
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (isLoading) Color.Gray else MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(6.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            if (mainWindowList.isEmpty() && !isLoading) {
                Text(
                    text = "未获取到运行程序数据或运行出错",
                    fontSize = 14.sp,
                    color = Color(0xFFEF5350),
                    modifier = Modifier.padding(8.dp)
                )
            }

            AdvancedFilteredWindowList(mainWindowList, windowManager)
        }
    }
}

enum class FilterType {
    ALL, PROCESS_NAME, WINDOW_TITLE
}

// 生成唯一背景色的函数
fun generateUniqueColor(text: String): Color {
    val hash = text.hashCode().absoluteValue
    val hue = (hash % 360).toFloat()
    val saturation = 0.3f + (hash % 40) / 100f // 30%-70%
    val lightness = 0.85f + (hash % 15) / 100f // 85%-100%

    return Color.hsv(hue, saturation, lightness)
}

// 生成更深的颜色用于指示条
fun generateAccentColor(text: String): Color {
    val hash = text.hashCode().absoluteValue
    val hue = (hash % 360).toFloat()
    val saturation = 0.7f + (hash % 30) / 100f // 70%-100%
    val lightness = 0.5f + (hash % 30) / 100f // 50%-80%

    return Color.hsv(hue, saturation, lightness)
}

@Composable
fun AdvancedFilteredWindowList(
    mainWindowList: List<MainWindowInfo>,
    windowManager: WindowManager,
    lazyListState: LazyListState = rememberLazyListState()
) {
    var filterText by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf(FilterType.ALL) }
    val scope = rememberCoroutineScope()

    // 根据过滤条件筛选窗口列表
    val filteredList = remember(mainWindowList, filterText, filterType) {
        if (filterText.isBlank()) {
            mainWindowList
        } else {
            mainWindowList.filter { windowInfo ->
                when (filterType) {
                    FilterType.ALL ->
                        windowInfo.processName.contains(filterText, ignoreCase = true) ||
                                windowInfo.mainWindowTitle.contains(filterText, ignoreCase = true)

                    FilterType.PROCESS_NAME ->
                        windowInfo.processName.contains(filterText, ignoreCase = true)

                    FilterType.WINDOW_TITLE ->
                        windowInfo.mainWindowTitle.contains(filterText, ignoreCase = true)
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索框和过滤选项（恢复原始大小）
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                label = { Text("搜索窗口") },
                placeholder = { Text("按进程名或窗口标题搜索...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "搜索"
                    )
                },
                trailingIcon = {
                    if (filterText.isNotEmpty()) {
                        IconButton(onClick = { filterText = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "清除"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 过滤类型选择 - 使用更紧凑的布局
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    onClick = { filterType = FilterType.ALL },
                    label = { Text("全部") },
                    selected = filterType == FilterType.ALL,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    onClick = { filterType = FilterType.PROCESS_NAME },
                    label = { Text("进程名") },
                    selected = filterType == FilterType.PROCESS_NAME,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    onClick = { filterType = FilterType.WINDOW_TITLE },
                    label = { Text("窗口标题") },
                    selected = filterType == FilterType.WINDOW_TITLE,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 匹配项数量提示
        if (filterText.isNotEmpty()) {
            Text(
                text = "找到 ${filteredList.size} 个匹配项",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(filteredList, key = { it.id }) { mainWindowInfo ->
                    EnhancedMainWindowItem(mainWindowInfo) {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                windowManager.activateWindow(mainWindowInfo.hWnd)
                            }
                        }
                    }
                }

                if (filteredList.isEmpty() && filterText.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "未找到匹配的窗口",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                // 添加底部填充以确保最后的项目不会被遮挡
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = lazyListState)
            )
        }
    }
}

@Composable
fun EnhancedMainWindowItem(mainWindowInfo: MainWindowInfo, onActivate: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    // 为每个条目生成唯一的背景色
    val uniqueBackgroundColor = remember(mainWindowInfo.id, mainWindowInfo.processName) {
        generateUniqueColor("${mainWindowInfo.id}_${mainWindowInfo.processName}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp) // 增加高度以容纳更多信息
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true)
            ) { onActivate() },
        elevation = if (isHovered) 4.dp else 2.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = if (isHovered)
            uniqueBackgroundColor.copy(alpha = 0.9f)
        else
            uniqueBackgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧彩色指示条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        generateAccentColor("${mainWindowInfo.id}_${mainWindowInfo.processName}"),
                        RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 显示窗口标题和进程名
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mainWindowInfo.mainWindowTitle.ifEmpty { "无标题窗口" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.9f),
                    maxLines = 1
                )
                Text(
                    text = mainWindowInfo.processName.ifEmpty { "需要以管理员身份运行" },
                    fontSize = 12.sp,
                    color = if (mainWindowInfo.processName.isNotEmpty()) Color.Black.copy(alpha = 0.6f) else Color.Red,
                    maxLines = 1
                )
            }

            // 为processName为空的项添加特殊图标
            if (mainWindowInfo.processName.isEmpty()) {
                Icon(
                    Icons.Outlined.AdminPanelSettings, "可能需要重启应用", Modifier
                        .size(24.dp), MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 添加进程图标占位符
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            )
        }
    }
}

data class MainWindowInfo(
    val id: String,
    val processName: String,
    val mainWindowTitle: String,
    val hWnd: MemorySegment
)