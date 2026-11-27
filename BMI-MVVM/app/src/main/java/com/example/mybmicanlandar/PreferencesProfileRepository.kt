package com.example.mybmicanlandar

import android.content.Context

class PreferencesProfileRepository(context: Context) : ProfileRepository {
    // 中文 · BMI 是本应用私有的偏好文件。三个版本的 applicationId 不同，因此各自拥有自己的数据。
    // EN · BMI names an app-private preferences file. Each architecture has a different application ID and its own saved profile.
    private val preferences = context.applicationContext.getSharedPreferences("BMI", Context.MODE_PRIVATE)
    // 中文 · 首次启动时缺失的键读成空表单。性别由 GenderStorage 统一解码。
    // EN · Missing keys produce an empty form on first launch. GenderStorage decodes the stored selection.
    override fun load() = BmiForm(preferences.getString("height", "").orEmpty(),
        preferences.getString("weight", "").orEmpty(), preferences.getString("age", "").orEmpty(),
        GenderStorage.decode(preferences.getString("gender", "")))
    // 中文 · T04 在计算校验成功后保存最近一次资料。apply() 立即更新内存，磁盘写入异步完成。
    // EN · T04 saves the latest valid form after Calculate. apply() updates memory immediately and schedules the disk write.
    override fun save(form: BmiForm) {
        preferences.edit().putString("height", form.height).putString("weight", form.weight)
            .putString("age", form.age).putString("gender", form.gender?.let(GenderStorage::encode)).apply()
    }
}
