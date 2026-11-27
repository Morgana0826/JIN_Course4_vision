package com.example.mybmicanlandar

import android.widget.EditText
import android.widget.RadioGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportFlowTest {
    // 中文 · 从控件输入一路验证到报告和返回；这条测试会同时经过 XML 绑定、校验和跨页面传值。
    // EN · Enter data through the widgets, open the report and return. This path exercises layout binding, validation and data transfer between activities.
    @Test fun calculateReportAndReturnToProfile() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity {
                it.findViewById<EditText>(R.id.heightInput).setText("170")
                it.findViewById<EditText>(R.id.weightInput).setText("60")
                it.findViewById<EditText>(R.id.ageInput).setText("20")
                it.findViewById<RadioGroup>(R.id.genderGroup).check(R.id.genderMale)
            }
            onView(withId(R.id.calculateButton)).perform(scrollTo(), click())
            onView(withId(R.id.bmiValue)).check(matches(withText("20.76")))
            onView(withId(R.id.categoryValue)).check(matches(withText(R.string.category_acceptable)))
            onView(withId(R.id.calculateAgainButton)).perform(scrollTo(), click())
            onView(withId(R.id.heightInput)).check(matches(withText("170")))
            onView(withId(R.id.genderMale)).check(matches(isChecked()))
        }
    }

    // 中文 · 无效年龄必须让输入页继续可见；可与上面的成功路径对照阅读。
    // EN · Invalid age must leave the input screen visible. Compare this branch with the successful flow above.
    @Test fun invalidAgeKeepsUserOnInputScreen() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity {
                it.findViewById<EditText>(R.id.heightInput).setText("170")
                it.findViewById<EditText>(R.id.weightInput).setText("60")
                it.findViewById<EditText>(R.id.ageInput).setText("5")
                it.findViewById<RadioGroup>(R.id.genderGroup).check(R.id.genderMale)
            }
            onView(withId(R.id.calculateButton)).perform(scrollTo(), click())
            onView(withId(R.id.bmiValue)).check(doesNotExist())
            onView(withId(R.id.ageInput)).check(matches(withText("5")))
        }
    }
}
