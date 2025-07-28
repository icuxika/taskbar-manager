# Taskbar Manager

Taskbar Manager 是一个基于 Kotlin Multiplatform 和 Compose Multiplatform 构建的 Windows
桌面应用程序，用于显示当前运行的窗口列表，并允许您在它们之间快速切换。该应用程序提供了一个简洁、现代的界面来管理您的打开窗口。

## 功能特性

- 实时显示运行中的 Windows 应用程序
- 按进程名或窗口标题搜索和过滤功能
- 为可能需要管理员权限的窗口提供视觉指示器
- 一键激活窗口
- 每10秒自动刷新
- 现代化的 Material Design 3 UI，带有流畅动画

## 工作原理

该应用程序使用 Java Foreign Function & Memory (FFM) API 通过 jextract 生成的绑定直接调用 Windows API 函数。它使用
`EnumWindows` 枚举可见窗口，获取进程信息，并在可滚动列表中显示它们。

对于进程名为空的窗口（通常表示需要管理员权限才能访问），应用程序会显示特殊的图标和指示文本。

## 环境要求

- Windows 10 或更高版本
- JDK 22 或更高版本
- Gradle

## 构建项目

克隆仓库：

```
git clone https://github.com/icuxika/taskbar-manager.git
cd taskbar-manager
```

使用 jextract 生成 Windows API 绑定：

```
.\jextract\run_extract.ps1
```

## 运行应用

以开发模式运行应用程序：

```
.\gradlew.bat run
```

## 打包发布

创建可分发的安装包：

```
.\gradlew.bat createDistributable
```

这将在 `composeApp/build/compose/binaries/main/app` 目录中生成 Windows 安装程序。

## 技术架构

- **Kotlin Multiplatform**：用于跨平台开发的核心语言
- **Compose Multiplatform**：用于构建桌面界面的 UI 框架
- **Java FFM API**：用于调用原生 Windows API
- **jextract**：用于从 Windows API 头文件生成 Java 绑定的工具

## 核心组件

- `App.kt`：主 Compose 应用程序，包含 UI 组件
- `WindowManager.kt`：处理 Windows API 交互和窗口管理
- `jextract/`：包含生成 Windows API 绑定的配置

## Windows API 集成

该应用程序使用以下 Windows API 函数：

- `EnumWindows`：枚举所有顶级窗口
- `SetForegroundWindow`：将窗口置于前台
- `ShowWindow`：控制窗口可见性
- `GetWindowTextW`：获取窗口标题
- `GetWindowThreadProcessId`：获取窗口的进程 ID
- `OpenProcess`：打开进程以查询信息
- 以及更多...

这些函数通过 jextract 生成的绑定访问，使 Kotlin 应用程序能够直接与 Windows 操作系统交互。