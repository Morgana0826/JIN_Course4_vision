package com.example.mybmicanlandar

/** Persist language-independent values; also read profiles saved by older versions. */
object GenderStorage {
    // 中文 · 保存枚举名称，用户切换语言后仍能识别同一选项。
    // EN · Store the enum name so changing the display language preserves the selection.
    fun encode(gender: BmiCalculator.Gender): String = gender.name

    // 中文 · 兼容旧版保存的中英文显示值；未知值返回 null，让表单要求重新选择。
    // EN · Accept labels written by older English and Chinese versions. An unknown value returns null so the form can request a new selection.
    fun decode(value: String?): BmiCalculator.Gender? = when (value) {
        "MALE", "Male", "男" -> BmiCalculator.Gender.MALE
        "FEMALE", "Female", "女" -> BmiCalculator.Gender.FEMALE
        else -> null
    }
}
