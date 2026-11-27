package com.example.mybmicanlandar

import org.junit.Assert.*
import org.junit.Test

// 中文 · 用一份表单和写入计数代替 Android 存储，架构测试就能观察哪些输入真正触发保存。
// EN · A form and a write counter stand in for Android storage so architecture tests can observe which inputs actually trigger a save.
class MemoryProfiles(var form: BmiForm = BmiForm()) : ProfileRepository {
    var writes = 0
    override fun load() = form
    override fun save(form: BmiForm) { this.form = form; writes++ }
}
// 中文 · 分别测试文字解析、年龄与性别校验，以及合法资料生成报告的路径。
// EN · Exercise text parsing, age and gender checks, and report creation from a valid profile.
class BmiModelTest {
    private val model = BmiModel()
    @Test fun rejectsIncompleteAndNonFiniteInput() {
        assertEquals(Validation.Invalid(InputError.MISSING_MEASUREMENTS), model.validate(BmiForm()))
        assertEquals(Validation.Invalid(InputError.INVALID_NUMBER), model.validate(BmiForm("NaN", "60", "20", BmiCalculator.Gender.MALE)))
        assertEquals(Validation.Invalid(InputError.NON_POSITIVE), model.validate(BmiForm("0", "60", "20", BmiCalculator.Gender.MALE)))
    }
    @Test fun validatesAgeAndGender() {
        assertEquals(Validation.Invalid(InputError.INVALID_AGE), model.validate(BmiForm("170", "60", "5", BmiCalculator.Gender.MALE)))
        assertEquals(Validation.Invalid(InputError.MISSING_GENDER), model.validate(BmiForm("170", "60", "20")))
    }
    @Test fun producesExpectedReport() {
        val result = model.validate(BmiForm("170", "60", "20", BmiCalculator.Gender.MALE)) as Validation.Valid
        val report = model.report(result.profile)!!
        assertEquals(20.76, report.bmi, 0.01)
        assertEquals(BmiCalculator.Category.ACCEPTABLE, report.category)
        assertNull(model.report(null))
    }
}
