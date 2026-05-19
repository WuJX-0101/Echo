# Echo

回应用户心情、记录每日情绪的轻量级 Android 应用。

## 技术栈

- **语言**: Kotlin 2.0.21
- **UI**: Jetpack Compose + Material 3
- **架构**: MVVM + Repository + Koin DI
- **数据库**: Room + DataStore Preferences
- **构建**: Gradle KTS + AGP 8.3.0
- **最低 SDK**: 32 / **目标 SDK**: 35

## 功能

- 快速记录心情（选择 emoji + 标签）
- 日历月视图回顾
- 趋势统计
- 桌面 Widget
- 分享卡片导出
- 定时提醒通知

## 构建

```bash
./gradlew assembleDebug
```

## 许可

MIT
