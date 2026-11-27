# BMI-MVI

在 Android Studio 中单独打开这个目录即可运行 BMI-MVI。Open this folder as a standalone Android Studio project.

[中英文源码导读 · Bilingual reading guide](../README.md)
应用 ID：`com.example.mybmicanlandar.mvi`，三个版本可同时安装。

## 架构

- `MainIntent` 表达编辑、计算、验证结果与菜单动作。
- `MainReducer` 为纯函数，输入旧状态和 Intent，输出不可变状态、Command 和 Effect。
- `MainStore` 顺序处理 Intent，执行验证/保存 Command，再把结果作为 Intent 交回 Reducer。
- `ReportStore` / `ReportReducer` 用同样方式处理报告与返回操作。
- Activity 只 dispatch Intent 并观察状态，副作用不会随状态重新渲染而重复执行。
- 数据流：View → Intent → Reducer → State → View；Store 执行 Command 并回送结果 Intent。

共同业务模型是 `BmiModel`、`BmiCalculator` 和 `ProfileRepository`；Android 持久化适配器为 `PreferencesProfileRepository`。
主页面与报告页的公共视图适配器为 `MainScreenActivity`、`ReportScreenActivity`，仅包含控件绑定、资源映射和 Android 导航。
业务/架构文件位于 `app/src/main/java/com/example/mybmicanlandar/`。

## 功能

- 英文、简体中文、繁体中文随系统语言选择。
- Options Menu：About BMI、BMI Wiki、Exit；Context Menu：长按 BMI 图标显示 About BMI、BMI Wiki。
- 主页面与报告页均有横竖屏布局。
- 成人及 6–18 岁儿童 BMI 分类；校验输入并保存资料，性别使用与语言无关的标识。

## 构建与测试

使用 JDK 11、Android SDK 32，Gradle Wrapper 7.4。
在 Android Studio 中打开本目录，并配置本机 Android SDK。

```text
gradlew.bat assembleDebug testDebugUnitTest lintDebug
gradlew.bat connectedDebugAndroidTest
```

构建输出：`app/build/outputs/apk/debug/app-debug.apk`。
单元测试覆盖 BMI 边界、输入校验、性别兼容和 MVI 行为；设备测试覆盖菜单、报告、错误输入、页面重建和语言/方向资源。
