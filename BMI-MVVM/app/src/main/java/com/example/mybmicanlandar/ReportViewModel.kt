package com.example.mybmicanlandar

class ReportViewModel(profile: BmiProfile?, model: BmiModel = BmiModel()) {
    // 中文 · 在 ViewModel 创建时用 Model 计算一次。旋转后重用实例时，新观察者能获取已有报告。
    // EN · Compute the report when this ViewModel is created. If retained across rotation, it supplies that report to new observers.
    private val mutableReport = StateCell(model.report(profile))
    val report: ObservableState<BmiReport?> = mutableReport
    val effects = EffectChannel<UiEffect>()
    // 中文 · 关闭页面属于一次性事件，重新订阅报告数据时不应再次关闭。
    // EN · Closing is a one-time effect. A fresh report subscription must not close the screen again.
    fun back() = effects.emit(UiEffect.Close)
}
