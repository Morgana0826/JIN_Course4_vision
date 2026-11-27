package com.example.mybmicanlandar

// 中文 · Activity 实现 View 合约。向下跟踪 calculate() 可看到点击交给 Presenter，再由 perform() 接回导航或错误提示。
// EN · The Activity implements the View contract. Follow calculate() into the Presenter and perform() back to the Android navigation or error message.
class MainActivity : MainScreenActivity(), MainContract.View {
    private lateinit var presenter: MainPresenter
    override fun connect(restored: BmiForm?) {
        presenter = MainPresenter(PreferencesProfileRepository(this))
        // 中文 · 把新页面与草稿交给 Presenter；它决定显示草稿还是从偏好文件恢复资料。
        // EN · Attach the new view and draft. The presenter chooses the draft or the profile from preferences.
        presenter.attach(this, restored)
    }
    override fun formChanged(form: BmiForm) = presenter.edit(form)
    override fun calculate() = presenter.calculate()
    override fun menuAction(action: MenuAction) = presenter.menu(action)
    override fun displayForm(form: BmiForm) = renderForm(form)
    override fun perform(effect: UiEffect) = showEffect(effect)
    // 中文 · 页面销毁时断开 View，避免 Presenter 再操作旧 Activity。
    // EN · Detach the view on destruction so the presenter cannot update the old Activity.
    override fun disconnect() = presenter.detach()
}
