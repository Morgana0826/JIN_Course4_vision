package com.example.mybmicanlandar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// 中文 · 从旧的中英文性别值恢复，核对同一人的儿童分类不因切换语言而变化。
// EN · Restore gender from old localized values and verify that changing language preserves the child assessment.
class GenderStorageTest {
    @Test fun readsLegacyEnglishAndChineseProfiles() {
        listOf("MALE", "Male", "男").forEach {
            assertEquals(BmiCalculator.Gender.MALE, GenderStorage.decode(it))
        }
        listOf("FEMALE", "Female", "女").forEach {
            assertEquals(BmiCalculator.Gender.FEMALE, GenderStorage.decode(it))
        }
        assertNull(GenderStorage.decode(null))
        assertNull(GenderStorage.decode("unknown"))
    }

    @Test fun savedGenderPreservesChildAssessmentAcrossLanguages() {
        val male = GenderStorage.decode(GenderStorage.encode(BmiCalculator.Gender.MALE))!!
        val female = GenderStorage.decode(GenderStorage.encode(BmiCalculator.Gender.FEMALE))!!
        assertEquals(BmiCalculator.Category.ACCEPTABLE, BmiCalculator.categoryFor(6, male, 18.5))
        assertEquals(BmiCalculator.Category.OVERWEIGHT, BmiCalculator.categoryFor(6, female, 18.5))
    }
}
