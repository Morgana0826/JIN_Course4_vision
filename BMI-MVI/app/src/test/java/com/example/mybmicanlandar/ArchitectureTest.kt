package com.example.mybmicanlandar
import org.junit.Assert.*
import org.junit.Test

class ArchitectureTest {
    // 中文 · 同一状态和意图应产生相同转换。再看下面的 Store 测试，核对命令真正执行后的保存和导航。
    // EN · The same state and intent should yield the same transition. The store test below then checks actual persistence and navigation.
    @Test fun reducerIsDeterministicAndDoesNotMutateInput() {
        val state = MainState(BmiForm("170", "60", "20", BmiCalculator.Gender.MALE))
        val first = MainReducer.reduce(state, MainIntent.Calculate)
        assertEquals(first, MainReducer.reduce(state, MainIntent.Calculate))
        assertEquals(listOf(MainCommand.Validate(state.form)), first.commands)
        assertTrue(first.effects.isEmpty())
        assertNull(state.error)
    }
    @Test fun storeRunsValidationSaveAndNavigationInOrder() {
        val repository = MemoryProfiles()
        val store = MainStore(repository)
        val effects = mutableListOf<UiEffect>()
        store.effects.observe {
            if (it is UiEffect.OpenReport) assertEquals(1, repository.writes)
            effects += it
        }
        store.dispatch(MainIntent.Calculate)
        assertEquals(0, repository.writes)
        assertEquals(InputError.MISSING_MEASUREMENTS, store.state.value.error)
        store.dispatch(MainIntent.FormChanged(BmiForm("170", "60", "20", BmiCalculator.Gender.MALE)))
        store.dispatch(MainIntent.Calculate)
        assertNull(store.state.value.error)
        assertTrue(effects.last() is UiEffect.OpenReport)
        val oldCount = effects.size
        store.state.observe { }
        assertEquals(oldCount, effects.size)
    }
    @Test fun reportStoreComputesAndHandlesBackIntent() {
        val store = ReportStore()
        var closed = false
        store.effects.observe { closed = it == UiEffect.Close }
        store.dispatch(ReportIntent.Load(BmiProfile(170.0, 60.0, 20, BmiCalculator.Gender.MALE)))
        assertEquals(20.76, store.state.value.report!!.bmi, 0.01)
        store.dispatch(ReportIntent.Back)
        assertTrue(closed)
    }
}
