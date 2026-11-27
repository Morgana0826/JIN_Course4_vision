package com.example.mybmicanlandar

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.text.DecimalFormat

abstract class ReportScreenActivity : Activity() {
    protected abstract fun connect(profile: BmiProfile?)
    protected abstract fun closeReport()
    protected abstract fun disconnect()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        findViewById<Button>(R.id.calculateAgainButton).setOnClickListener { closeReport() }
        // 中文 · 从启动 Intent 中取回资料。缺少或类型错误时传入 null，由各架构的报告逻辑处理。
        // EN · Read the profile from the launch Intent. Missing or mismatched data becomes null for the report logic to handle.
        connect(intent.getSerializableExtra("profile") as? BmiProfile)
    }
    // 中文 · 报告模型已经包含数值和分类。此处只按当前语言和屏幕控件显示结果。
    // EN · The report already contains the numeric result and category. This method binds them to localized screen widgets.
    protected fun renderReport(report: BmiReport) {
        val category = report.category
        val gender = getString(if (report.profile.gender == BmiCalculator.Gender.MALE)
            R.string.gender_male else R.string.gender_female)
        // 中文 · 保留两位小数是显示规则；分类使用模型中尚未舍入的值。
        // EN · Two decimal places are a display choice. Classification uses the unrounded model value.
        findViewById<TextView>(R.id.bmiValue).text = DecimalFormat("0.00").format(report.bmi)
        findViewById<TextView>(R.id.profileSummary).text = getString(R.string.profile_summary, report.profile.age, gender)
        findViewById<TextView>(R.id.assessmentBasis).setText(if (report.profile.age <= 18)
            R.string.basis_child else R.string.basis_adult)
        // 中文 · 同一分类同时决定文本、颜色、图片和警告。新增分类时要在这里补齐对应资源。
        // EN · Each category selects text, colour, artwork and warning visibility together. Add the matching resources here when introducing a category.
        val presentation = when (category) {
            BmiCalculator.Category.SEVERELY_UNDERWEIGHT -> StatusPresentation(
                R.string.category_severely_underweight,
                R.string.advice_severely_underweight,
                R.drawable.ic_status_thin,
                R.color.warning,
                true
            )
            BmiCalculator.Category.UNDERWEIGHT -> StatusPresentation(
                R.string.category_underweight,
                R.string.advice_underweight,
                R.drawable.ic_status_thin,
                R.color.thin,
                false
            )
            BmiCalculator.Category.ACCEPTABLE -> StatusPresentation(
                R.string.category_acceptable,
                R.string.advice_acceptable,
                R.drawable.ic_status_fit,
                R.color.fit,
                false
            )
            BmiCalculator.Category.OVERWEIGHT -> StatusPresentation(
                R.string.category_overweight,
                R.string.advice_overweight,
                R.drawable.ic_status_fat,
                R.color.overweight,
                false
            )
            BmiCalculator.Category.OBESE -> StatusPresentation(
                R.string.category_obese,
                R.string.advice_obese,
                R.drawable.ic_status_fat,
                R.color.fat,
                false
            )
            BmiCalculator.Category.SEVERELY_OVERWEIGHT -> StatusPresentation(
                R.string.category_severely_overweight,
                R.string.advice_severely_overweight,
                R.drawable.ic_status_fat,
                R.color.warning,
                true
            )
        }

        findViewById<TextView>(R.id.categoryValue).apply {
            setText(presentation.categoryText)
            setTextColor(resources.getColor(presentation.color))
        }
        findViewById<TextView>(R.id.adviceText).setText(presentation.adviceText)
        findViewById<ImageView>(R.id.statusImage).setImageResource(presentation.image)
        findViewById<TextView>(R.id.warningBanner).visibility =
            if (presentation.isWarning) View.VISIBLE else View.GONE

    }
    private data class StatusPresentation(
        val categoryText: Int,
        val adviceText: Int,
        val image: Int,
        val color: Int,
        val isWarning: Boolean
    )


    override fun onDestroy() { disconnect(); super.onDestroy() }
}
