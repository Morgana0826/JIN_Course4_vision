package com.example.mybmicanlandar

class ReportActivity : ReportScreenActivity() {
    private lateinit var viewModel: ReportViewModel
    private val subscriptions = mutableListOf<() -> Unit>()
    // 中文 · 订阅报告时会立刻收到当前值。null 表示无法生成报告，Activity 调用 finish() 返回。
    // EN · The report subscription immediately delivers its current value. A null report makes the Activity finish.
    override fun connect(profile: BmiProfile?) {
        viewModel = lastNonConfigurationInstance as? ReportViewModel ?: ReportViewModel(profile)
        subscriptions += viewModel.report.observe { if (it == null) finish() else renderReport(it) }
        subscriptions += viewModel.effects.observe { if (it == UiEffect.Close) finish() }
    }
    // 中文 · 页面把返回请求交给 ViewModel，再从事件通道接收 Close。
    // EN · The view forwards Back to the ViewModel and listens for Close on the effects channel.
    override fun closeReport() = viewModel.back()
    override fun onRetainNonConfigurationInstance(): Any = viewModel
    override fun disconnect() { subscriptions.forEach { it() }; subscriptions.clear() }
}
