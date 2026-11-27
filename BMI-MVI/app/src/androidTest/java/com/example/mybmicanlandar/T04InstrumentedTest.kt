package com.example.mybmicanlandar

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
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
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class T04InstrumentedTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @Test fun aboutActionShowsExplanation() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.menu_about)).perform(click())
            onView(withText(R.string.about_message)).check(matches(isDisplayed()))
            onView(withId(android.R.id.button1)).perform(click())
        }
    }

    @Test fun longPressLogoOpensContextMenuAndAboutDialog() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.bmiLogo)).perform(longClick())
            onView(withText(R.string.menu_wiki)).check(matches(isDisplayed()))
            onView(withText(R.string.about_button)).perform(click())
            onView(withText(R.string.about_message)).check(matches(isDisplayed()))
            onView(withId(android.R.id.button1)).perform(click())
        }
    }

    // 中文 · 用 ActivityMonitor 拦截浏览器 Intent，检查发送次数，测试不会依赖浏览器实际加载网页。
    // EN · Intercept the browser intent with an ActivityMonitor and count launches without relying on the website loading.
    @Test fun wikiMenuOpensBrowserIntent() {
        val filter = IntentFilter(Intent.ACTION_VIEW).apply { addDataScheme("https") }
        val monitor = instrumentation.addMonitor(filter, Instrumentation.ActivityResult(Activity.RESULT_OK, null), true)
        try {
            ActivityScenario.launch(MainActivity::class.java).use {
                openActionBarOverflowOrOptionsMenu(context)
                onView(withText(R.string.menu_wiki)).perform(click())
                assertEquals(1, monitor.hits)
            }
        } finally { instrumentation.removeMonitor(monitor) }
    }

    @Test fun exitMenuFinishesMainActivity() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            openActionBarOverflowOrOptionsMenu(context)
            onView(withText(R.string.menu_exit)).perform(click())
            // finish() is asynchronous; allow the window transition to complete.
            val deadline = android.os.SystemClock.uptimeMillis() + 10000
            while (scenario.state != androidx.lifecycle.Lifecycle.State.DESTROYED &&
                android.os.SystemClock.uptimeMillis() < deadline) {
                instrumentation.waitForIdleSync()
                android.os.SystemClock.sleep(100)
            }
            assertEquals(androidx.lifecycle.Lifecycle.State.DESTROYED, scenario.state)
        }
    }

    // 中文 · 强制重建同一界面，检查输入框和单选按钮是否仍显示同一份草稿。
    // EN · Force Activity recreation and check that the text fields and radio selection still show the draft.
    @Test fun formSurvivesRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity {
                it.findViewById<EditText>(R.id.heightInput).setText("170")
                it.findViewById<EditText>(R.id.weightInput).setText("60")
                it.findViewById<EditText>(R.id.ageInput).setText("20")
                it.findViewById<RadioGroup>(R.id.genderGroup).check(R.id.genderFemale)
            }
            scenario.recreate()
            scenario.onActivity {
                assertEquals("170", it.findViewById<EditText>(R.id.heightInput).text.toString())
                assertEquals("60", it.findViewById<EditText>(R.id.weightInput).text.toString())
                assertEquals(R.id.genderFemale, it.findViewById<RadioGroup>(R.id.genderGroup).checkedRadioButtonId)
            }
        }
    }

    // 中文 · 按语言和方向组合创建资源上下文，验证翻译和两套布局的关键控件。
    // EN · Create resource contexts for language and orientation combinations to verify translations and the required widgets in both layouts.
    @Test fun languageResourcesAndBothOrientationsResolve() {
        val locales = listOf("en-US" to "About BMI", "zh-CN" to "关于 BMI", "zh-TW" to "關於 BMI")
        instrumentation.runOnMainSync {
            for ((tag, about) in locales) {
                for (orientation in listOf(Configuration.ORIENTATION_PORTRAIT, Configuration.ORIENTATION_LANDSCAPE)) {
                    val config = Configuration(context.resources.configuration).apply {
                        setLocale(Locale.forLanguageTag(tag))
                        this.orientation = orientation
                    }
                    val localized = context.createConfigurationContext(config)
                    assertEquals(about, localized.getString(R.string.about_button))
                    val root = LayoutInflater.from(localized).inflate(R.layout.activity_main, null)
                    assertNotNull(root.findViewById<View>(R.id.toolbar))
                    assertTrue(root.findViewById<View>(R.id.bmiLogo).isLongClickable)
                    assertNotNull(root.findViewById<View>(R.id.calculateButton))
                }
            }
        }
    }
}
