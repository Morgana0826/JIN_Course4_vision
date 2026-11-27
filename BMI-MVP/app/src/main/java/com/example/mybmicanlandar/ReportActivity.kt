package com.example.mybmicanlandar

// 中文 · 基类负责查找控件；本类把资料交给 Presenter，并把 displayReport 和 close 转成实际界面操作。
// EN · The base class binds the widgets. This adapter passes the profile to the presenter and turns displayReport and close into screen operations.
class ReportActivity : ReportScreenActivity(), ReportView {
    private val presenter = ReportPresenter()
    override fun connect(profile: BmiProfile?) = presenter.attach(this, profile)
    override fun closeReport() = presenter.back()
    override fun displayReport(report: BmiReport) = renderReport(report)
    override fun close() = finish()
    override fun disconnect() = presenter.detach()
}
