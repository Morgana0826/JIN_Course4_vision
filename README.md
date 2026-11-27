# Assignment 4 · BMI for Android

三个可独立运行的 Kotlin / XML 工程实现相同的 BMI 输入页和报告页。每段新增源码注释先用中文解释，再给出英文。初读建议选 MVVM，跟踪一次「计算」点击，再比较 MVP 和 MVI 如何处理同一事件。

Three standalone Kotlin / XML projects implement the same BMI form and report. Each added source note pairs Chinese with English. Start with MVVM, trace one press of **Calculate**, then compare the same interaction in MVP and MVI.

## 跟踪一次计算 · Trace a calculation

用身高 170 cm、体重 60 kg、年龄 20 和任一性别运行下面的流程。报告显示 **20.76**。把年龄改成 5 再试一次，输入页应显示错误，仓库也不会写入这次表单。

Use 170 cm, 60 kg, age 20 and either gender. The report displays **20.76**. Try age 5 next to follow the error path. The input screen stays open and the invalid form is never saved.

| 顺序 | 源码 | 阅读时留意 · What to follow |
|---|---|---|
| 1 | [activity_main.xml](BMI-MVVM/app/src/main/res/layout/activity_main.xml) → [MainScreenActivity.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainScreenActivity.kt) | 找到 `calculateButton` 的点击监听器和 `readForm()` 的输入快照。Find the click listener and the form snapshot built from the widgets. |
| 2 | [MainActivity.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainActivity.kt) → [MainViewModel.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainViewModel.kt) | 跟踪 `updateForm()` 和 `calculate()`。输入时更新草稿，成功计算后才写入资料。Editing updates the draft. A valid Calculate action saves it. |
| 3 | [BmiModel.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/BmiModel.kt) → [BmiCalculator.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/BmiCalculator.kt) | 看原始文字如何变为已校验的资料。比较 18 岁和 19 岁走哪条分类路径。Follow parsing and validation, then compare the category rules at ages 18 and 19. |
| 4 | [PreferencesProfileRepository.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/PreferencesProfileRepository.kt) → [MainScreenActivity.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainScreenActivity.kt) | 成功分支先保存，再发出 `OpenReport`。搜索 `putExtra("profile"` 找到跨页面传值处。The success branch saves first. Follow the effect to the Intent extra carrying the profile. |
| 5 | [ReportActivity.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/ReportActivity.kt) → [ReportViewModel.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/ReportViewModel.kt) → [ReportScreenActivity.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/ReportScreenActivity.kt) | 模型生成报告，`renderReport()` 把结果绑定到数值、图标和本地化文字。The model builds the report, then the renderer binds its value, icon and localized labels. |

## 同一次点击，三种架构 · Compare one click

| 工程 | 主要调用路径 · Calculation path | 用测试验证 · Inspect the test |
|---|---|---|
| [BMI-MVP](BMI-MVP/) | [MainPresenter](BMI-MVP/app/src/main/java/com/example/mybmicanlandar/MainPresenter.kt) 校验后直接调用 View 接口。The presenter calls the attached view after validation. | [FakeView 收到的提示和导航 · View callbacks](BMI-MVP/app/src/test/java/com/example/mybmicanlandar/ArchitectureTest.kt) |
| [BMI-MVVM](BMI-MVVM/) | [MainViewModel](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainViewModel.kt) 发布表单状态和一次性事件。The view subscribes to state for rendering and effects for navigation. | [重新订阅时的行为 · Rebinding](BMI-MVVM/app/src/test/java/com/example/mybmicanlandar/ArchitectureTest.kt) |
| [BMI-MVI](BMI-MVI/) | [MainReducer](BMI-MVI/app/src/main/java/com/example/mybmicanlandar/MainReducer.kt) 产生校验命令，[MainStore](BMI-MVI/app/src/main/java/com/example/mybmicanlandar/MainStore.kt) 执行并把结果送回 Reducer。The reducer requests validation and the store feeds the outcome back as an intent. | [状态转换与命令执行 · Transitions and commands](BMI-MVI/app/src/test/java/com/example/mybmicanlandar/ArchitectureTest.kt) |

## 换一道题时改哪里 · Adapt it to a new problem

| 需求 · Change | 文件与联动点 · Files to update together |
|---|---|
| 输入项 / Input fields | 改 `BmiForm`、`BmiProfile`、`validate()`、主页的 `readForm()` / `renderForm()`、两套主页布局和偏好键。Update form types, validation, widget binding, both main layouts and the preference keys. |
| 公式与分类 / Formula and categories | 改 `BmiCalculator`，同步更新 [边界测试 · Boundary tests](BMI-MVVM/app/src/test/java/com/example/mybmicanlandar/BmiCalculatorTest.kt)。若分类项改变，同时改报告的枚举映射和文字资源。Update the calculator and boundary cases. Changed categories also need report mappings and labels. |
| 中文 / 英文 / Languages | 默认英文、简体中文和繁体中文分别在 `res/values`、`values-zh`、`values-zh-rTW` 的 `strings.xml`。Keep the same keys and format arguments across all three translations. |
| 横屏与菜单 / Landscape and menus | [横屏布局](BMI-MVVM/app/src/main/res/layout-land/) 复用竖屏的控件 ID。[菜单 XML](BMI-MVVM/app/src/main/res/menu/) 的项目 ID 对应 `MainScreenActivity.selected()`。Keep widget IDs consistent across orientations and connect menu IDs to their selection handler. |

## 本机运行 · Run locally

1. 在 Android Studio 用 **Open** 打开其中一个 `BMI-*` 子目录，选含 `settings.gradle` 的那一层。Open one architecture subfolder as the project root.
2. 原项目配置为 JDK 11、Android SDK Platform 32、Gradle Wrapper 7.4。为 Gradle 选 JDK 11，配置本机 SDK，然后 Sync。Select JDK 11 for Gradle, configure the local SDK and sync.
3. 在模拟器或手机上选择 **Run**。要在 Windows 命令行编译并运行单元测试，在所选工程目录执行下列命令。Run on a device, or build and run the local JVM tests from that project directory on Windows.

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```

生成的 APK 位于所选工程的 `app/build/outputs/apk/debug/app-debug.apk`。原作业的 [四张运行截图 · Four assignment screenshots](Screenshots/) 可用于对照语言和屏幕方向。

The debug APK is written under the selected project. The four screenshots illustrate the original language and orientation requirements.
