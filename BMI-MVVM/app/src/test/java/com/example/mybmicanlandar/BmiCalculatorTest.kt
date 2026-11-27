package com.example.mybmicanlandar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

// 中文 · 先看公式样例，再看成人分界和儿童表两端年龄。改比较符号时，这些断言能指出边界落错档。
// EN · Start with the formula example, then read the adult boundaries and both ends of the child table. These assertions expose a changed comparison at a cutoff.
class BmiCalculatorTest {
    @Test
    fun calculatesBmiFromCentimetresAndKilograms() {
        assertEquals(20.76, BmiCalculator.calculate(170.0, 60.0), 0.01)
    }

    @Test
    fun followsAdultCategoryBoundariesFromAgeNineteen() {
        val gender = BmiCalculator.Gender.MALE
        assertEquals(BmiCalculator.Category.UNDERWEIGHT, BmiCalculator.categoryFor(19, gender, 18.49))
        assertEquals(BmiCalculator.Category.ACCEPTABLE, BmiCalculator.categoryFor(19, gender, 18.5))
        assertEquals(BmiCalculator.Category.ACCEPTABLE, BmiCalculator.categoryFor(19, gender, 22.9))
        assertEquals(BmiCalculator.Category.OVERWEIGHT, BmiCalculator.categoryFor(19, gender, 23.0))
        assertEquals(BmiCalculator.Category.OVERWEIGHT, BmiCalculator.categoryFor(19, gender, 24.99))
        assertEquals(BmiCalculator.Category.OBESE, BmiCalculator.categoryFor(19, gender, 25.0))
    }

    @Test
    fun followsBoysChartAtAgeSix() {
        val gender = BmiCalculator.Gender.MALE
        assertEquals(BmiCalculator.Category.SEVERELY_UNDERWEIGHT, BmiCalculator.categoryFor(6, gender, 12.8))
        assertEquals(BmiCalculator.Category.UNDERWEIGHT, BmiCalculator.categoryFor(6, gender, 12.9))
        assertEquals(BmiCalculator.Category.ACCEPTABLE, BmiCalculator.categoryFor(6, gender, 13.2))
        assertEquals(BmiCalculator.Category.OVERWEIGHT, BmiCalculator.categoryFor(6, gender, 18.9))
        assertEquals(BmiCalculator.Category.SEVERELY_OVERWEIGHT, BmiCalculator.categoryFor(6, gender, 21.5))
    }

    @Test
    fun followsGirlsChartAtAgeEighteen() {
        val gender = BmiCalculator.Gender.FEMALE
        assertEquals(BmiCalculator.Category.SEVERELY_UNDERWEIGHT, BmiCalculator.categoryFor(18, gender, 16.1))
        assertEquals(BmiCalculator.Category.UNDERWEIGHT, BmiCalculator.categoryFor(18, gender, 16.2))
        assertEquals(BmiCalculator.Category.ACCEPTABLE, BmiCalculator.categoryFor(18, gender, 16.6))
        assertEquals(BmiCalculator.Category.OVERWEIGHT, BmiCalculator.categoryFor(18, gender, 26.0))
        assertEquals(BmiCalculator.Category.SEVERELY_OVERWEIGHT, BmiCalculator.categoryFor(18, gender, 30.4))
    }

    @Test
    fun childAssessmentUsesGenderSpecificChart() {
        assertEquals(
            BmiCalculator.Category.ACCEPTABLE,
            BmiCalculator.categoryFor(6, BmiCalculator.Gender.MALE, 18.5)
        )
        assertEquals(
            BmiCalculator.Category.OVERWEIGHT,
            BmiCalculator.categoryFor(6, BmiCalculator.Gender.FEMALE, 18.5)
        )
    }

    @Test
    fun rejectsNonPositiveMeasurements() {
        assertThrows(IllegalArgumentException::class.java) {
            BmiCalculator.calculate(0.0, 60.0)
        }
    }

    @Test
    fun rejectsAgeOutsideAvailableCharts() {
        assertThrows(IllegalArgumentException::class.java) {
            BmiCalculator.categoryFor(5, BmiCalculator.Gender.FEMALE, 16.0)
        }
    }
}
