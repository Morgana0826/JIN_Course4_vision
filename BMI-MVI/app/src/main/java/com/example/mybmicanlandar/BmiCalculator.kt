package com.example.mybmicanlandar

// 中文 · 三个架构共用这套作业规则。练习换公式时，先用一组可手算的输入写出预期结果。
// EN · All three versions use these assignment rules. Start a formula change with an input and an expected result you can calculate by hand.
object BmiCalculator {
    enum class Gender { MALE, FEMALE }

    enum class Category {
        SEVERELY_UNDERWEIGHT,
        UNDERWEIGHT,
        ACCEPTABLE,
        OVERWEIGHT,
        OBESE,
        SEVERELY_OVERWEIGHT
    }

    // 中文 · 每行是某一年龄的四个上界。数组按 6 至 18 岁排列，下标 0 对应 6 岁。
    // EN · Each row holds four upper bounds for one age. Rows run from age 6 to 18, with age 6 at index zero.
    private data class ChildThresholds(
        val severeUnderMax: Double,
        val underMax: Double,
        val acceptableMax: Double,
        val overweightMax: Double
    )

    private val boys = arrayOf(
        ChildThresholds(12.8, 13.1, 18.8, 21.4),
        ChildThresholds(13.0, 13.3, 19.8, 23.0),
        ChildThresholds(13.2, 13.6, 20.9, 24.6),
        ChildThresholds(13.5, 13.8, 21.8, 26.0),
        ChildThresholds(13.8, 14.1, 22.7, 27.3),
        ChildThresholds(14.1, 14.5, 23.6, 28.3),
        ChildThresholds(14.4, 14.8, 24.3, 29.2),
        ChildThresholds(14.7, 15.1, 25.0, 30.0),
        ChildThresholds(15.0, 15.4, 25.5, 30.6),
        ChildThresholds(15.3, 15.8, 26.1, 31.2),
        ChildThresholds(15.6, 16.1, 26.5, 31.7),
        ChildThresholds(15.9, 16.3, 27.0, 32.1),
        ChildThresholds(16.1, 16.6, 27.4, 32.4)
    )

    private val girls = arrayOf(
        ChildThresholds(12.6, 12.8, 18.3, 20.5),
        ChildThresholds(12.8, 13.1, 19.1, 21.8),
        ChildThresholds(13.1, 13.4, 20.1, 23.1),
        ChildThresholds(13.4, 13.7, 21.0, 24.4),
        ChildThresholds(13.7, 14.1, 21.9, 25.6),
        ChildThresholds(14.1, 14.4, 22.7, 26.6),
        ChildThresholds(14.4, 14.8, 23.4, 27.5),
        ChildThresholds(14.8, 15.2, 24.0, 28.3),
        ChildThresholds(15.1, 15.5, 24.6, 28.9),
        ChildThresholds(15.4, 15.8, 25.0, 29.4),
        ChildThresholds(15.7, 16.1, 25.4, 29.7),
        ChildThresholds(15.9, 16.3, 25.7, 30.0),
        ChildThresholds(16.1, 16.5, 25.9, 30.3)
    )

    // 中文 · 输入单位是厘米和千克。先换算成米再平方，例如 170 cm、60 kg 得到约 20.76。
    // EN · The inputs use centimetres and kilograms. Convert height to metres before squaring it. For 170 cm and 60 kg the result is about 20.76.
    fun calculate(heightCm: Double, weightKg: Double): Double {
        require(heightCm > 0 && weightKg > 0) { "Height and weight must be positive" }
        val heightMetres = heightCm / 100.0
        return weightKg / (heightMetres * heightMetres)
    }

    // 中文 · 年龄校验保护下面的数组索引。18 岁使用儿童表，19 岁开始使用成人分支。
    // EN · The age check protects the table index below. Age 18 is the last child row and age 19 enters the adult branch.
    fun categoryFor(age: Int, gender: Gender, bmi: Double): Category {
        require(age in 6..120) { "Age must be from 6 to 120" }
        require(bmi > 0) { "BMI must be positive" }
        return if (age <= 18) childCategory(age, gender, bmi) else adultCategory(bmi)
    }

    // 中文 · 从高到低比较下界，所以恰好 23 会进入 OVERWEIGHT；改区间时也要核对等号。
    // EN · Lower bounds are checked from highest to lowest, so exactly 23 maps to OVERWEIGHT. Keep boundary tests in sync with any change.
    private fun adultCategory(bmi: Double): Category = when {
        bmi >= 25.0 -> Category.OBESE
        bmi >= 23.0 -> Category.OVERWEIGHT
        bmi >= 18.5 -> Category.ACCEPTABLE
        else -> Category.UNDERWEIGHT
    }

    // 中文 · 先按性别和年龄取一行，再按从低到高的顺序比较。等于某个上界时归入该档。
    // EN · Select a row by gender and age, then compare upper bounds in ascending order. Equality stays in that band.
    private fun childCategory(age: Int, gender: Gender, bmi: Double): Category {
        val thresholds = if (gender == Gender.MALE) boys[age - 6] else girls[age - 6]
        return when {
            bmi <= thresholds.severeUnderMax -> Category.SEVERELY_UNDERWEIGHT
            bmi <= thresholds.underMax -> Category.UNDERWEIGHT
            bmi <= thresholds.acceptableMax -> Category.ACCEPTABLE
            bmi <= thresholds.overweightMax -> Category.OVERWEIGHT
            else -> Category.SEVERELY_OVERWEIGHT
        }
    }
}
