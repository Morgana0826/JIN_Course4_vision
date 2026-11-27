package com.example.mybmicanlandar

interface ReportView {
    fun displayReport(report: BmiReport)
    fun close()
}
class ReportPresenter(private val model: BmiModel = BmiModel()) {
    private var view: ReportView? = null
    // 中文 · 收到资料就向 Model 请求报告。资料无效时关闭页面，有结果时通过 ReportView 回调绘制。
    // EN · Ask the model for a report on attachment. An invalid profile closes the view, while a valid report is passed to its display callback.
    fun attach(view: ReportView, profile: BmiProfile?) {
        this.view = view
        val report = model.report(profile)
        if (report == null) view.close() else view.displayReport(report)
    }
    // 中文 · “再次计算”关闭当前报告，Android 返回栈会显示原输入页。
    // EN · Calculate Again closes the current report so the Android back stack reveals the input screen.
    fun back() { view?.close() }
    fun detach() { view = null }
}
