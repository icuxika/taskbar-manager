package com.icuxika.taskbar_manager

import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppDesktopTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun getWindows() {
        val windowManager = WindowManager()
        windowManager.getWindows()
    }
}