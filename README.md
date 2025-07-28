# Taskbar Manager

Taskbar Manager is a Windows desktop application built with Kotlin Multiplatform and Compose Multiplatform that displays
a list of currently running windows and allows you to quickly switch between them. The application provides a clean,
modern interface for managing your open windows.

## Features

- Real-time display of running Windows applications
- Search and filter functionality by process name or window title
- Visual indicators for windows that may require administrator privileges
- One-click window activation
- Automatic refresh every 10 seconds
- Modern Material Design 3 UI with smooth animations

## How It Works

The application uses Java Foreign Function & Memory (FFM) API to directly call Windows API functions through
jextract-generated bindings. It enumerates visible windows using `EnumWindows`, retrieves process information, and
displays them in a scrollable list.

For windows with empty process names (which typically indicates they require administrator privileges to access), the
application shows a special icon and indicator text.

## Prerequisites

- Windows 10 or later
- JDK 22 or later
- Gradle

## Building

Clone the repository:

```
git clone https://github.com/icuxika/taskbar-manager.git
cd taskbar-manager
```

Generate Windows API bindings using jextract:

```
.\jextract\run_extract.ps1
```

## Running

To run the application in development mode:

```
.\gradlew.bat run
```

## Packaging

To create a distributable package:

```
.\gradlew.bat createDistributable
```

This will generate installers for Windows in the `composeApp/build/compose/binaries/main/app` directory.

## Architecture

- **Kotlin Multiplatform**: The core language for cross-platform development
- **Compose Multiplatform**: UI framework for building the desktop interface
- **Java FFM API**: For calling native Windows APIs
- **jextract**: Tool for generating Java bindings from Windows API headers

## Key Components

- `App.kt`: Main Compose application with UI components
- `WindowManager.kt`: Handles Windows API interactions and window management
- `jextract/`: Contains configuration for generating Windows API bindings

## Windows API Integration

The application uses the following Windows API functions:

- `EnumWindows`: Enumerate all top-level windows
- `SetForegroundWindow`: Bring a window to the foreground
- `ShowWindow`: Control window visibility
- `GetWindowTextW`: Retrieve window title
- `GetWindowThreadProcessId`: Get process ID for a window
- `OpenProcess`: Open a process to query information
- And more...

These functions are accessed through jextract-generated bindings, allowing the Kotlin application to directly interact
with the Windows operating system.