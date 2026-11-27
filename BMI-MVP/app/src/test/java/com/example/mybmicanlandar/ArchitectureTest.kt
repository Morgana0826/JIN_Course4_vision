package com.example.mybmicanlandar
import org.junit.Assert.*
import org.junit.Test

class ArchitectureTest {
    // 中文 · 记录 Presenter 发给界面的动作，无需启动 Activity 即可断言最后收到的是提示还是导航。
    // EN · Record calls made by the presenter and assert whether the view received a message or navigation, without starting an Activity.
    private class FakeView : MainContract.View {
        var form = BmiForm()
        val effects = mutableListOf<UiEffect>()
        override fun displayForm(form: BmiForm) { this.form = form }
        override fun perform(effect: UiEffect) { effects += effect }
    }
    @Test fun presenterValidatesBeforeSavingAndNavigating() {
        val repository = MemoryProfiles()
        val presenter = MainPresenter(repository)
        val view = FakeView()
        presenter.attach(view, null)
        presenter.calculate()
        assertEquals(0, repository.writes)
        assertTrue(view.effects.last() is UiEffect.Message)
        presenter.edit(BmiForm("170", "60", "20", BmiCalculator.Gender.MALE))
        presenter.calculate()
        assertEquals(1, repository.writes)
        assertTrue(view.effects.last() is UiEffect.OpenReport)
    }
    @Test fun presenterDetachesViewAndRestoresDraft() {
        val presenter = MainPresenter(MemoryProfiles())
        val view = FakeView()
        val draft = BmiForm("175", "65", "21", BmiCalculator.Gender.FEMALE)
        presenter.attach(view, draft)
        assertEquals(draft, view.form)
        presenter.detach()
        presenter.menu(MenuAction.ABOUT)
        assertTrue(view.effects.isEmpty())
    }
    @Test fun reportPresenterUsesModel() {
        var result: BmiReport? = null
        var closed = false
        val view = object : ReportView {
            override fun displayReport(report: BmiReport) { result = report }
            override fun close() { closed = true }
        }
        val presenter = ReportPresenter()
        presenter.attach(view, BmiProfile(170.0, 60.0, 20, BmiCalculator.Gender.MALE))
        assertEquals(20.76, result!!.bmi, 0.01)
        presenter.back()
        assertTrue(closed)
    }
}
