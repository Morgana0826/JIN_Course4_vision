package com.example.mybmicanlandar

// 中文 · 两个方向分别由接口约定：View 提供绘制和执行动作的方法，Presenter 接收编辑、计算和菜单事件。
// EN · The View contract describes rendering and UI actions. The Presenter contract accepts edits, calculation requests and menu selections.
interface MainContract {
    interface View {
        fun displayForm(form: BmiForm)
        fun perform(effect: UiEffect)
    }
    interface Presenter {
        fun attach(view: View, restored: BmiForm?)
        fun edit(form: BmiForm)
        fun calculate()
        fun menu(action: MenuAction)
        fun detach()
    }
}
class MainPresenter(private val repository: ProfileRepository, private val model: BmiModel = BmiModel()) : MainContract.Presenter {
    private var view: MainContract.View? = null
    private var form = BmiForm()
    override fun attach(view: MainContract.View, restored: BmiForm?) {
        this.view = view
        // 中文 · 系统恢复的草稿优先，可保留尚未提交的编辑；没有草稿时读取最近保存的有效表单。
        // EN · Prefer the restored draft, including unsubmitted edits. Otherwise read the last valid form from storage.
        form = restored ?: repository.load()
        view.displayForm(form)
    }
    override fun edit(form: BmiForm) { this.form = form }
    // 中文 · 一次点击只有两个出口：校验失败就通知 View 提示错误，成功才保存并通知 View 打开报告。
    // EN · A failed validation tells the view to show an error. A successful one saves the form before telling the view to open the report.
    override fun calculate() {
        when (val result = model.validate(form)) {
            is Validation.Invalid -> view?.perform(UiEffect.Message(result.error))
            is Validation.Valid -> {
                repository.save(form)
                view?.perform(UiEffect.OpenReport(result.profile))
            }
        }
    }
    override fun menu(action: MenuAction) { view?.perform(action.effect()) }
    // 中文 · 解除引用后，延迟到来的菜单或点击回调无法再触达这个页面。
    // EN · Clearing this reference prevents later callbacks from reaching the detached view.
    override fun detach() { view = null }
}
