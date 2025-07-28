package com.icuxika.taskbar_manager

import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.kdroid.composetray.tray.api.Tray
import java.awt.Desktop
import java.awt.GraphicsEnvironment
import java.awt.Toolkit
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

fun main() {
    application {
        var isVisible by remember { mutableStateOf(true) }
        var windowPosition by remember { mutableStateOf(Offset.Zero) }

        val bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds
        val screenInsets = Toolkit.getDefaultToolkit()
            .getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration)

        val workAreaWidth = bounds.width - screenInsets.left - screenInsets.right
        val workAreaHeight = bounds.height - screenInsets.top - screenInsets.bottom

        val x = screenInsets.left + workAreaWidth - 480 - 16
        val y = screenInsets.top + workAreaHeight - 720 - 16
        windowPosition = Offset(x.toFloat(), y.toFloat())

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
                    if (!System.getProperty("os.name").lowercase().contains("win")) {
                        Desktop.getDesktop().requestForeground(true)
                    }
                }
            }
        }

        Tray(
            iconContent = { Icon(TrayIcon, contentDescription = "", tint = Color(0xFFFFA500)) },
            tooltip = "Taskbar Manager",
            primaryAction = {
                isVisible = true
            },
            primaryActionLabel = "打开 Taskbar Manager"
        ) {
            Item(label = "打开 Taskbar Manager") {
                isVisible = true
            }
            Item(label = "关闭 Taskbar Manager") {
                exitApplication()
            }
        }
    }
}

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color(0xFFFFA500))
    }
}