package com.icuxika.taskbar_manager

import com.icuxika.taskbar_manager.jextract.win32.WNDENUMPROC
import com.icuxika.taskbar_manager.jextract.win32.ffm_h.*
import java.lang.foreign.*
import java.nio.charset.StandardCharsets

class WindowManager {

    fun getWindows(): MutableList<MainWindowInfo> {
        val mainWindowInfoList = mutableListOf<MainWindowInfo>()
        Arena.ofConfined().use { arena ->
            EnumWindows(WNDENUMPROC.allocate({ hWnd, lParam ->
                if (IsWindowVisible(hWnd) > 0) {
                    val exStyle = GetWindowLongW(hWnd, GWL_EXSTYLE())
                    if (exStyle and WS_EX_TOOLWINDOW() == 0) {
                        // 排除子窗口
                        if (GetWindow(hWnd, GW_OWNER()) == MemorySegment.NULL) {
                            if (GetParent(hWnd) == MemorySegment.NULL) {
                                // 获取窗口类名
                                val classNameBuffer = arena.allocate(256 * 2)
                                val classNameLength = GetClassNameW(hWnd, classNameBuffer, 256)
                                var className = ""
                                if (classNameLength > 0) {
                                    className = classNameBuffer.getString(0, StandardCharsets.UTF_16LE)
                                }
                                // 排除一些系统窗口
                                if (!isExcludedClassName(className)) {
                                    // 获取窗口标题
                                    val length = GetWindowTextLengthW(hWnd)
                                    if (length > 0) {
                                        val windowTitleBuffer = arena.allocate((length + 1) * 2.toLong())
                                        val actualLength = GetWindowTextW(hWnd, windowTitleBuffer, length + 1)
                                        if (actualLength > 0) {
                                            val windowTitle = windowTitleBuffer.getString(0, StandardCharsets.UTF_16LE)

                                            // 获取进程id
                                            val processIdPtr = arena.allocate(ValueLayout.JAVA_INT)
                                            GetWindowThreadProcessId(hWnd, processIdPtr)
                                            val processId = processIdPtr.get(ValueLayout.JAVA_INT, 0)

                                            val processHandle = OpenProcess(
                                                PROCESS_QUERY_INFORMATION() or PROCESS_VM_READ(),
                                                0,
                                                processId
                                            )

                                            // 获取进程名称
                                            var processName = ""
                                            if (processHandle != MemorySegment.NULL) {
                                                // jextract工具无法生成psapi.h下的一些函数，手动调用
                                                val psapi = SymbolLookup.libraryLookup("psapi", arena)
                                                val getModuleBaseNameW = linker.downcallHandle(
                                                    psapi.find("GetModuleBaseNameW").get(), FunctionDescriptor.of(
                                                        ValueLayout.JAVA_INT,
                                                        ValueLayout.ADDRESS,
                                                        ValueLayout.ADDRESS,
                                                        ValueLayout.ADDRESS,
                                                        ValueLayout.JAVA_INT
                                                    )
                                                )
                                                val processNameBuffer = arena.allocate(256 * 2)
                                                val processNameLength = getModuleBaseNameW.invoke(
                                                    processHandle,
                                                    MemorySegment.NULL,
                                                    processNameBuffer,
                                                    256
                                                )
                                                if (processNameLength as Int > 0) {
                                                    processName =
                                                        processNameBuffer.getString(0, StandardCharsets.UTF_16LE)
                                                }
                                                CloseHandle(processHandle)
                                            }

                                            mainWindowInfoList.add(
                                                MainWindowInfo(
                                                    "${processId}_${hWnd.address()}",
                                                    processName,
                                                    windowTitle,
                                                    hWnd
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1
            }, arena), 0)
        }
        return mainWindowInfoList
    }

    fun activateWindow(mainWindowHandle: Long) {
        Arena.ofConfined().use { arena ->
            if (IsIconic(MemorySegment.ofAddress(mainWindowHandle)) > 0) {
                ShowWindow(MemorySegment.ofAddress(mainWindowHandle), 9)
            }
            SetForegroundWindow(MemorySegment.ofAddress(mainWindowHandle))
        }
    }

    fun activateWindow(hWnd: MemorySegment) {
        Arena.ofConfined().use { arena ->
            if (IsIconic(hWnd) > 0) {
                ShowWindow(hWnd, 9)
            }
            SetForegroundWindow(hWnd)
        }
    }

    private fun isExcludedClassName(className: String): Boolean {
        if (className == "") {
            return false
        }

        val excludedClassNameList = listOf("Windows.UI.Core.CoreWindow", "ApplicationFrameWindow")
        return excludedClassNameList.contains(className.trim())
    }

    companion object {
        private val linker = Linker.nativeLinker()
    }

}