package com.icuxika.taskbar_manager

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.Desktop
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

fun main() = application {
    var isVisible by remember { mutableStateOf(true) }
    var windowPosition by remember { mutableStateOf(Offset.Zero) }

    Window(
        onCloseRequest = { isVisible = false },
        visible = isVisible,
        title = "Taskbar Manager",
        icon = TrayIcon,
        state = rememberWindowState(
            position = WindowPosition(windowPosition.x.dp, windowPosition.y.dp),
            size = DpSize(480.dp, 720.dp)
        ),
        undecorated = true,
        transparent = true,
        alwaysOnTop = true,
        resizable = false
    ) {
        this.window.addWindowFocusListener(object : WindowFocusListener {
            override fun windowGainedFocus(e: WindowEvent?) {
            }

            override fun windowLostFocus(e: WindowEvent?) {
                isVisible = false
            }
        })
        App()
        LaunchedEffect(isVisible) {
            if (isVisible) {
                Desktop.getDesktop().requestForeground(true)
            }
        }
    }

    Tray(
        TrayIcon,
        tooltip = "Taskbar Manager",
        onAction = {
            isVisible = true
        },
        menu = {
            Item("打开 Taskbar Manager", onClick = {
                isVisible = true
            })
            Item("退出 Taskbar Manager", onClick = ::exitApplication)
        },
    )
}

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}