package com.icuxika.taskbar_manager

import java.lang.foreign.*

class NativeBridge {

    fun x() {
        val psapi = SymbolLookup.libraryLookup("Psapi.dll", Arena.ofAuto())
        val methodHandle = linker.downcallHandle(
            psapi.find("GetModuleBaseNameW").get(), FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_INT
            )
        )
    }

    companion object {
        private val linker = Linker.nativeLinker()
    }
}