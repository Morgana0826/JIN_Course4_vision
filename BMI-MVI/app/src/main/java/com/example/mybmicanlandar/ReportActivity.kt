package com.example.mybmicanlandar

class ReportActivity : ReportScreenActivity() {
    private lateinit var store: ReportStore
    private val subscriptions = mutableListOf<() -> Unit>()
    // 中文 · 先订阅状态和关闭事件，随后再请求加载。这样即使资料无效，页面也能收到关闭信号。
    // EN · Subscribe to state and close effects before requesting a load, so even an invalid launch can deliver Close.
    override fun connect(profile: BmiProfile?) {
        store = lastNonConfigurationInstance as? ReportStore ?: ReportStore()
        subscriptions += store.state.observe { it.report?.let(::renderReport) }
        subscriptions += store.effects.observe { if (it == UiEffect.Close) finish() }
        // 中文 · 仅在 Store 没有报告时发 Load；屏幕旋转后已经存在的结果可直接重绘。
        // EN · Dispatch Load only while no report exists. A retained result can be rendered immediately after rotation.
        if (store.state.value.report == null) store.dispatch(ReportIntent.Load(profile))
    }
    override fun closeReport() = store.dispatch(ReportIntent.Back)
    override fun onRetainNonConfigurationInstance(): Any = store
    override fun disconnect() { subscriptions.forEach { it() }; subscriptions.clear() }
}
