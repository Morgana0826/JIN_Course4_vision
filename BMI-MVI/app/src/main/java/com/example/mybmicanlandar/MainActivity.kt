package com.example.mybmicanlandar

class MainActivity : MainScreenActivity() {
    private lateinit var store: MainStore
    private val subscriptions = mutableListOf<() -> Unit>()
    // 中文 · 屏幕观察 Store 的状态来显示表单，并单独观察界面动作；旋转时复用原 Store。
    // EN · Observe store state for form rendering and effects for UI actions. The store is retained across rotation.
    override fun connect(restored: BmiForm?) {
        store = lastNonConfigurationInstance as? MainStore ?: MainStore(PreferencesProfileRepository(this), restored)
        subscriptions += store.state.observe { renderForm(it.form) }
        subscriptions += store.effects.observe(::showEffect)
    }
    // 中文 · 三种入口都通过 dispatch 发送意图。追踪点击时先看 MainIntent.Calculate，再看 MainReducer。
    // EN · All three entry points dispatch intents. Trace a button press from MainIntent.Calculate into MainReducer.
    override fun formChanged(form: BmiForm) = store.dispatch(MainIntent.FormChanged(form))
    override fun calculate() = store.dispatch(MainIntent.Calculate)
    override fun menuAction(action: MenuAction) = store.dispatch(MainIntent.MenuSelected(action))
    override fun onRetainNonConfigurationInstance(): Any = store
    // 中文 · 旧 Activity 销毁时移除其观察者，新页面会自行订阅当前 Store。
    // EN · Remove observers when the old Activity is destroyed. Its replacement subscribes to the retained store.
    override fun disconnect() { subscriptions.forEach { it() }; subscriptions.clear() }
}
