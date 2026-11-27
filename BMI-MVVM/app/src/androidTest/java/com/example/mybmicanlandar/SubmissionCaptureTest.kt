package com.example.mybmicanlandar

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.SystemClock
import android.widget.EditText
import android.widget.RadioGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Assume.assumeNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Capture actual emulator pixels. Invoke only with -e screenshot report|main|menu|context. */
@RunWith(AndroidJUnit4::class)
class SubmissionCaptureTest {
    @Test fun captureRequestedScreen() {
        // 中文 · 截图模式由运行参数决定；普通测试运行没有 screenshot 参数时跳过本例。
        // EN · A runner argument selects the screen to capture. A normal run without the screenshot argument skips this case.
        val mode = InstrumentationRegistry.getArguments().getString("screenshot")
        assumeNotNull(mode)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val chinese = mode == "report" || mode == "menu"
        assertEquals(if (chinese) "zh" else "en", context.resources.configuration.locales[0].language)
        val landscape = mode == "main" || mode == "menu"
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity {
                it.requestedOrientation = if (landscape) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            val wanted = if (landscape) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT
            val deadline = SystemClock.uptimeMillis() + 10000
            var orientation = 0
            while (orientation != wanted && SystemClock.uptimeMillis() < deadline) {
                scenario.onActivity { orientation = it.resources.configuration.orientation }
                if (orientation != wanted) SystemClock.sleep(100)
            }
            assertEquals(wanted, orientation)
            scenario.onActivity {
                it.findViewById<EditText>(R.id.heightInput).setText("170")
                it.findViewById<EditText>(R.id.weightInput).setText("60")
                it.findViewById<EditText>(R.id.ageInput).setText("20")
                it.findViewById<RadioGroup>(R.id.genderGroup).check(R.id.genderMale)
            }
            androidx.test.espresso.Espresso.closeSoftKeyboard()
            // 中文 · 四个分支先操作出作业指定的画面，再把模拟器当时的像素保存为对应 PNG。
            // EN · Each branch navigates to one required screen before saving the emulator pixels to its corresponding PNG.
            val filename = when (mode) {
                "report" -> {
                    onView(withId(R.id.calculateButton)).perform(scrollTo(), click())
                    onView(withId(R.id.categoryValue)).check(matches(withText("体重正常")))
                    "01_Report_Chinese_Portrait.png"
                }
                "main" -> "02_MainAct_English_Landscape.png"
                "menu" -> {
                    openActionBarOverflowOrOptionsMenu(context)
                    onView(withText(R.string.menu_wiki)).check(matches(isDisplayed()))
                    "03_Menu_Chinese_Landscape.png"
                }
                "context" -> {
                    onView(withId(R.id.bmiLogo)).perform(longClick())
                    onView(withText("BMI Wiki")).check(matches(isDisplayed()))
                    "04_ContextMenu_English_Portrait.png"
                }
                else -> error("Unknown screenshot mode: $mode")
            }
            instrumentation.waitForIdleSync()
            SystemClock.sleep(500)
            val file = File(context.filesDir, "submission/$filename")
            file.parentFile!!.mkdirs()
            val bitmap = instrumentation.uiAutomation.takeScreenshot()
            requireNotNull(bitmap)
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            if (mode == "report") onView(withId(R.id.calculateAgainButton)).perform(scrollTo(), click())
            if (mode == "menu" || mode == "context") androidx.test.espresso.Espresso.pressBack()
        }
    }
}
