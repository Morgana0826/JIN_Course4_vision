package com.example.mybmicanlandar

class MainActivity : MainScreenActivity() {
    private lateinit var viewModel: MainViewModel
    private val subscriptions = mutableListOf<() -> Unit>()
    // 中文 · 旋转时取回原 ViewModel，并在新 Activity 上重新订阅。普通首次启动才新建 ViewModel。
    // EN · Reuse the retained ViewModel after rotation and subscribe the new Activity. A fresh launch creates a new ViewModel.
    override fun connect(restored: BmiForm?) {
        viewModel = lastNonConfigurationInstance as? MainViewModel
            ?: MainViewModel(PreferencesProfileRepository(this), restored)
        subscriptions += viewModel.state.observe { renderForm(it.form) }
        // 中文 · 状态订阅会立即绘制表单；事件订阅只接收此后发生的导航或提示。
        // EN · The state subscription immediately renders the form. The effect subscription handles only future navigation and messages.
        subscriptions += viewModel.effects.observe(::showEffect)
    }
    override fun formChanged(form: BmiForm) = viewModel.updateForm(form)
    override fun calculate() = viewModel.calculate()
    override fun menuAction(action: MenuAction) = viewModel.menu(action)
    override fun onRetainNonConfigurationInstance(): Any = viewModel
    // 中文 · 每个订阅都返回解除函数。销毁旧 Activity 时逐一调用，以免一次事件触发两个页面回调。
    // EN · Each subscription returns a removal function. Invoke each on destruction to prevent an event reaching two Activity instances.
    override fun disconnect() { subscriptions.forEach { it() }; subscriptions.clear() }
}
