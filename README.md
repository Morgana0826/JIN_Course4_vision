# Assignment 4 · BMI for Android

三个可独立运行的 Kotlin / XML 工程实现相同的 BMI 输入页和报告页。每段新增源码注释先用中文解释，再给出英文。初读建议选 MVVM，跟踪一次「计算」点击，再比较 MVP 和 MVI 如何处理同一事件。

Three standalone Kotlin / XML projects implement the same BMI form and report. Each added source note pairs Chinese with English. Start with MVVM, trace one press of **Calculate**, then compare the same interaction in MVP and MVI.

## 考试改编清单

Follow steps 1–9 in order when adapting the MVVM project. For a smaller change, use the table to jump to the relevant step. Source links open the file or declaration used in this repository.

以下按机房里从新建项目到交 APK 的顺序排列，以 **BMI-MVVM** 为底稿。每个文件名都可点击，有行号的链接会直接定位到对应函数或控件。在 Android Studio 双击 `Shift` 可以按文件名搜索。按题目选做，例如只改分类门槛，直接看第 4 步，再做第 9 步的边界测试。

| 要找什么 | 看哪一步 |
|---|---|
| 从零新建、复制源码、统一包名 | 1 |
| 文案和翻译 / 输入项和校验 / 公式和分类 | 2 / 3 / 4 |
| 控件布局和按钮 / 保存输入和跳转 / 报告显示 | 5 / 6 / 7 |
| 配色、圆角、图片、横屏和菜单 / 测试与 APK | 8 / 9 |

**1. 新建并跑通底稿 · Set up**

- 在 `Phone and Tablet` 选生成 XML 布局的 `Empty Activity`（部分版本叫 `Empty Views Activity`），语言选 Kotlin，包名按题目填写。先 Sync、Run，确认空白首页能启动。
- 从 [旧包目录](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/) 复制里面的 **11 个 .kt 文件**到新项目放 `MainActivity.kt` 的包目录，覆盖模板首页。用[整套 res](BMI-MVVM/app/src/main/res/) 替换空白模板的 `app/src/main/res`，把 [AndroidManifest.xml](BMI-MVVM/app/src/main/AndroidManifest.xml) 复制到新项目 `app/src/main/`，这样两个页面、主题和图标都能对应。
- 在新项目模块的 `build.gradle` 或 `build.gradle.kts` 查看 `namespace`。在复制过来的代码中将 `package com.example.mybmicanlandar` 改为这个包名，确认 Manifest 中仍有 `.MainActivity` 和 `.ReportActivity`。**第一次检查**：输入 170 cm、60 kg、20 岁，选择性别，能打开报告并看到 **20.76**。

**2. 改题目文案和单位 · Labels and units**

打开 [默认英文 strings.xml](BMI-MVVM/app/src/main/res/values/strings.xml)、[简体中文](BMI-MVVM/app/src/main/res/values-zh/strings.xml)、[繁体中文](BMI-MVVM/app/src/main/res/values-zh-rTW/strings.xml)。桌面应用名和工具栏标题找 `app_name`，首页大标题找 `main_title`，输入标签和占位文字找 `height_label`、`height_hint`，按钮找 `calculate`，错误找 `input_*`，结果建议找 `advice_*`。只改措辞时，保留资源名和译文中的格式参数。改变物理单位时继续第 3、4 步，核对输入和公式使用同一单位。

**3. 定义输入、输出与校验 · Data and validation**

打开 [BmiModel.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/BmiModel.kt#L7)。先改 `BmiForm`（输入框文字）、`BmiProfile`（通过校验的数值）和 `BmiReport`（报告输出），再改 [validate()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/BmiModel.kt#L26) 的必填、解析和范围判断，以及 [report()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/BmiModel.kt#L44) 生成结果的方式。例：若题目是平均速度，输入改为公里和分钟，分钟须大于 0。增加 `InputError` 时，到 [showEffect()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainScreenActivity.kt#L89) 给它配上第 2 步的错误文字。

**4. 改计算公式和分类门槛 · Calculation**

打开 [BmiCalculator.kt → calculate()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/BmiCalculator.kt#L60) 改公式。平均速度练习可以先写下预期值：`10 km ÷ (30 min / 60) = 20 km/h`。若仍做 BMI 但改分档，年龄分流在 [categoryFor()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/BmiCalculator.kt#L68)，成人阈值在 [adultCategory()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/BmiCalculator.kt#L76)，儿童阈值在 `boys`、`girls` 两张表，[childCategory()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/BmiCalculator.kt#L85) 决定取哪一行和怎样比较。改年龄范围时，也核对第 3 步的范围校验和数组索引。

**5. 改输入页，连接控件和按钮 · Input screen**

在 [activity_main.xml](BMI-MVVM/app/src/main/res/layout/activity_main.xml#L86) 增删输入框、设置 `inputType`、排列按钮。然后到 [MainScreenActivity.kt → onCreate()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainScreenActivity.kt#L34) 改 `findViewById`、输入监听器和 `setOnClickListener`。接着改 [readForm()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainScreenActivity.kt#L66) 把控件值读进新表单，改 [renderForm()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainScreenActivity.kt#L75) 把表单写回控件。若删去年龄或性别，也要清理对应的控件绑定与监听。**检查**：打开首页不闪退，编辑后的值不会被旧字段覆盖。

**6. 改点击后的流程、保存和跳转 · Save and navigate**

- [MainActivity.kt → calculate()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainActivity.kt#L17) 将点击转给 [MainViewModel.kt → calculate()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainViewModel.kt#L18)。在 ViewModel 中决定何时校验、保存和发出 `OpenReport`。本底稿的 `updateForm()` 更新内存草稿，有效计算后才持久保存；若题目要求「未计算也记住」，可在 `updateForm()` 中保存草稿。
- 字段变化时同步改 [PreferencesProfileRepository.kt → load() / save()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/PreferencesProfileRepository.kt#L11)，使每个字段的读写键一致。**检查**：用新数值完成题目规定的保存操作，关闭并重新启动，看它是否填回。
- 两页之间用同一个 `"profile"` 键。[MainScreenActivity.showEffect()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainScreenActivity.kt#L102) 发送，[ReportScreenActivity.onCreate()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/ReportScreenActivity.kt#L15) 接收。更换键名或数据类型时，两端一起改。

**7. 改报告页的布局、数值和提示 · Report screen**

在 [activity_report.xml](BMI-MVVM/app/src/main/res/layout/activity_report.xml#L59) 找 `bmiValue`、`categoryValue`、`statusImage`、`adviceText` 调整摆放。在 [ReportScreenActivity.kt → renderReport()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/ReportScreenActivity.kt#L25) 绑定新结果。保留一位小数就把 `DecimalFormat("0.00")` 改为 `DecimalFormat("0.0")`；[when (category)](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/ReportScreenActivity.kt#L37) 决定每一档的文字、颜色、图片和警示条。**检查**：报告的数值、单位和分类与手算一致，点击「再次计算」能回到原输入页。

**8. 按题目补样式、横屏和菜单 · Presentation**

| 改什么 | 文件 | 具体位置 |
|---|---|---|
| 字号、间距、卡片圆角 | [首页布局](BMI-MVVM/app/src/main/res/layout/activity_main.xml)、[报告布局](BMI-MVVM/app/src/main/res/layout/activity_report.xml)、[bg_card.xml](BMI-MVVM/app/src/main/res/drawable/bg_card.xml#L4) | 控件的 `android:textSize`、`padding`、`layout_margin*`；卡片的 `corners` → `android:radius` |
| 颜色、主题、图片 | [colors.xml](BMI-MVVM/app/src/main/res/values/colors.xml)、[themes.xml](BMI-MVVM/app/src/main/res/values/themes.xml)、[夜间主题](BMI-MVVM/app/src/main/res/values-night/themes.xml)、[drawable/](BMI-MVVM/app/src/main/res/drawable/) | `blue`、`surface`、`warning` 定义颜色；主题设置状态栏等系统颜色；状态图资源由第 7 步的分类映射选用 |
| 横屏 | [横屏首页](BMI-MVVM/app/src/main/res/layout-land/activity_main.xml)、[横屏报告](BMI-MVVM/app/src/main/res/layout-land/activity_report.xml) | 两个方向都要有 Kotlin 查找的控件 ID。旋转后分别试一次「计算」和「再次计算」 |
| 选项菜单、长按菜单 | [menu_options.xml](BMI-MVVM/app/src/main/res/menu/menu_options.xml)、[menu_context.xml](BMI-MVVM/app/src/main/res/menu/menu_context.xml)、[MainScreenActivity.kt](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/MainScreenActivity.kt#L119) | `onCreateOptionsMenu()`、`onCreateContextMenu()` 加载菜单；`selected()` 按项目 ID 分派。新增动作还要接入 [MenuAction / effect()](BMI-MVVM/app/src/main/java/com/example/mybmicanlandar/BmiModel.kt#L61) 和 `showEffect()` |

**9. 用最终题目验证，再生成 APK · Verify and submit**

- 用一个能手算的正常值跑通「输入 → 计算 → 报告 → 返回」。再测空值、0，以及恰好等于分档门槛的值。换公式时同步更新 [BmiCalculatorTest.kt](BMI-MVVM/app/src/test/java/com/example/mybmicanlandar/BmiCalculatorTest.kt)；换输入规则时同步更新 [BmiModelTest.kt](BMI-MVVM/app/src/test/java/com/example/mybmicanlandar/BmiModelTest.kt)。
- 在新工程根目录执行 `.\gradlew.bat testDebugUnitTest assembleDebug`。提交本次构建的 `app/build/outputs/apk/debug/app-debug.apk`。若要求截图，按指定语言和方向检查后再截图。

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

## 本机运行 · Run locally

1. 在 Android Studio 用 **Open** 打开其中一个 `BMI-*` 子目录，选含 `settings.gradle` 的那一层。Open one architecture subfolder as the project root.
2. 原项目配置为 JDK 11、Android SDK Platform 32、Gradle Wrapper 7.4。为 Gradle 选 JDK 11，配置本机 SDK，然后 Sync。Select JDK 11 for Gradle, configure the local SDK and sync.
3. 在模拟器或手机上选择 **Run**。要在 Windows 命令行编译并运行单元测试，在所选工程目录执行下列命令。Run on a device, or build and run the local JVM tests from that project directory on Windows.

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```

生成的 APK 位于所选工程的 `app/build/outputs/apk/debug/app-debug.apk`。原作业的 [四张运行截图 · Four assignment screenshots](Screenshots/) 可用于对照语言和屏幕方向。

The debug APK is written under the selected project. The four screenshots illustrate the original language and orientation requirements.
