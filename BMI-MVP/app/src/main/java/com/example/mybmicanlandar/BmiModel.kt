package com.example.mybmicanlandar

import java.io.Serializable

// 中文 · 这里保存输入框中的原始文字，允许空值和只输入一半的数字。Bundle 可序列化这份草稿。
// EN · The form keeps raw text, including empty or partially typed numbers. Its serializable representation can travel in a Bundle.
data class BmiForm(val height: String = "", val weight: String = "", val age: String = "",
                   val gender: BmiCalculator.Gender? = null) : Serializable
// 中文 · 校验成功后才构造数值资料。主页把它作为名为 profile 的 Intent extra 传给报告页。
// EN · Successful validation constructs this numeric profile. The main screen sends it to the report in the Intent extra named profile.
data class BmiProfile(val heightCm: Double, val weightKg: Double, val age: Int,
                      val gender: BmiCalculator.Gender) : Serializable
data class BmiReport(val profile: BmiProfile, val bmi: Double, val category: BmiCalculator.Category)
enum class InputError { MISSING_MEASUREMENTS, INVALID_NUMBER, NON_POSITIVE, INVALID_AGE, MISSING_GENDER }
// 中文 · 调用方必须处理成功和失败两个分支；错误枚举让界面按系统语言选择提示文字。
// EN · Callers handle either a typed profile or an error. The UI translates the error enum using the active language resources.
sealed class Validation {
    data class Valid(val profile: BmiProfile) : Validation()
    data class Invalid(val error: InputError) : Validation()
}

/** Domain rules have no Activity, View, resource or persistence dependencies. */
class BmiModel {
    // 中文 · 先检查必填和格式，再检查数值范围。toDoubleOrNull 处理格式错误，isFinite 另外排除 NaN 和无穷。
    // EN · Check presence and syntax before numeric ranges. Parsing catches malformed text, while isFinite also rejects NaN and infinity.
    fun validate(form: BmiForm): Validation {
        if (form.height.isBlank() || form.weight.isBlank())
            return Validation.Invalid(InputError.MISSING_MEASUREMENTS)
        val height = form.height.trim().toDoubleOrNull()
        val weight = form.weight.trim().toDoubleOrNull()
        if (height == null || weight == null || !height.isFinite() || !weight.isFinite())
            return Validation.Invalid(InputError.INVALID_NUMBER)
        if (height <= 0 || weight <= 0) return Validation.Invalid(InputError.NON_POSITIVE)
        val age = form.age.trim().toIntOrNull()
        if (age == null || age !in 6..120) return Validation.Invalid(InputError.INVALID_AGE)
        val gender = form.gender ?: return Validation.Invalid(InputError.MISSING_GENDER)
        val bmi = BmiCalculator.calculate(height, weight)
        if (!bmi.isFinite() || bmi <= 0) return Validation.Invalid(InputError.INVALID_NUMBER)
        return Validation.Valid(BmiProfile(height, weight, age, gender))
    }

    // 中文 · 报告页可能收到空资料或不合法的数据；复用同一套校验，成功后才计算数值和分类。
    // EN · The report may receive a missing or invalid profile. Reuse validation before computing the value and category.
    fun report(profile: BmiProfile?): BmiReport? {
        if (profile == null) return null
        val checked = validate(BmiForm(profile.heightCm.toString(), profile.weightKg.toString(),
            profile.age.toString(), profile.gender))
        if (checked !is Validation.Valid) return null
        val bmi = BmiCalculator.calculate(profile.heightCm, profile.weightKg)
        return BmiReport(profile, bmi, BmiCalculator.categoryFor(profile.age, profile.gender, bmi))
    }
}

// 中文 · 界面逻辑只依赖读写表单的契约。正式运行用 SharedPreferences，单元测试用内存实现。
// EN · Presentation code depends on this form storage contract. The app supplies SharedPreferences and unit tests supply an in-memory implementation.
interface ProfileRepository {
    fun load(): BmiForm
    fun save(form: BmiForm)
}

enum class MenuAction { ABOUT, WIKI, EXIT }
// 中文 · 这些操作需要界面执行，例如打开报告或显示提示；它们与可反复绘制的表单值分开。
// EN · The view executes these actions, such as opening a report or showing a message. The form values can be rendered repeatedly.
sealed class UiEffect {
    object About : UiEffect()
    object Wiki : UiEffect()
    object Close : UiEffect()
    data class Message(val error: InputError) : UiEffect()
    data class OpenReport(val profile: BmiProfile) : UiEffect()
}

fun MenuAction.effect(): UiEffect = when (this) {
    MenuAction.ABOUT -> UiEffect.About
    MenuAction.WIKI -> UiEffect.Wiki
    MenuAction.EXIT -> UiEffect.Close
}
