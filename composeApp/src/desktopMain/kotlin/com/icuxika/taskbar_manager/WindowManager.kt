package com.icuxika.taskbar_manager

import com.icuxika.taskbar_manager.jextract.win32.ffm_h.*
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

class WindowManager {

    fun getWindows() {}

    fun activateWindow(mainWindowHandle: Long) {
        Arena.ofConfined().use { arena ->
            if (IsIconic(MemorySegment.ofAddress(mainWindowHandle)) > 0) {
                ShowWindow(MemorySegment.ofAddress(mainWindowHandle), 9)
            }
            SetForegroundWindow(MemorySegment.ofAddress(mainWindowHandle))
        }
    }

}