package com.example.mybmicanlandar
import org.junit.Assert.*
import org.junit.Test

class ArchitectureTest {
    @Test fun viewModelPublishesValidationAndNavigation() {
        val repository = MemoryProfiles()
        val vm = MainViewModel(repository)
        val effects = mutableListOf<UiEffect>()
        vm.effects.observe { effects += it }
        vm.calculate()
        assertEquals(InputError.MISSING_MEASUREMENTS, vm.state.value.error)
        assertEquals(0, repository.writes)
        vm.updateForm(BmiForm("170", "60", "20", BmiCalculator.Gender.MALE))
        vm.calculate()
        assertNull(vm.state.value.error)
        assertEquals(1, repository.writes)
        assertTrue(effects.last() is UiEffect.OpenReport)
    }
    // 中文 · 模拟旋转后的重新订阅。新订阅者应拿到草稿，但已发生的导航只能计数一次。
    // EN · Simulate rebinding after rotation. The new subscriber receives the draft while the navigation count remains one.
    @Test fun rebindingRendersStateWithoutReplayingNavigation() {
        val draft = BmiForm("175", "65", "21", BmiCalculator.Gender.FEMALE)
        val vm = MainViewModel(MemoryProfiles(), draft)
        var events = 0
        val detach = vm.effects.observe { events++ }
        vm.calculate()
        detach()
        vm.effects.observe { events++ }
        var rendered: BmiForm? = null
        vm.state.observe { rendered = it.form }
        assertEquals(draft, rendered)
        assertEquals(1, events)
    }
    @Test fun reportViewModelExposesReportAndCloseEvent() {
        val vm = ReportViewModel(BmiProfile(170.0, 60.0, 20, BmiCalculator.Gender.MALE))
        assertEquals(20.76, vm.report.value!!.bmi, 0.01)
        var effect: UiEffect? = null
        vm.effects.observe { effect = it }
        vm.back()
        assertEquals(UiEffect.Close, effect)
    }
}
