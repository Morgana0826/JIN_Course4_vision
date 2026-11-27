package com.example.mybmicanlandar

// 中文 · 这是一份可供页面重绘的快照。error 记录当前校验错误，弹出提示的动作另走 effects。
// EN · A snapshot the screen can render. error records the validation failure, while effects delivers the request to show a message.
data class MainUiState(val form: BmiForm, val error: InputError? = null)

/** Plain Kotlin ViewModel: no reference to an Activity, View or Android Context. */
class MainViewModel(private val repository: ProfileRepository, restored: BmiForm? = null,
                    private val model: BmiModel = BmiModel()) {
    // 中文 · 只有 ViewModel 更新容器；对外的只读接口允许页面观察当前值。
    // EN · The ViewModel updates this container. The read-only interface below lets the view observe its value.
    private val mutableState = StateCell(MainUiState(restored ?: repository.load()))
    val state: ObservableState<MainUiState> = mutableState
    val effects = EffectChannel<UiEffect>()
    // 中文 · 编辑只更新内存草稿并清除旧错误；T04 的持久化发生在有效的 calculate() 分支。
    // EN · Edits update the in-memory draft and clear an old error. T04 persists it in the successful calculate() branch.
    fun updateForm(form: BmiForm) { mutableState.update(MainUiState(form)) }
    fun calculate() {
        val form = state.value.form
        when (val result = model.validate(form)) {
            // 中文 · 先更新错误状态，再发一次提示事件；此分支不会保存输入或打开报告。
            // EN · Publish the error state, then emit one message. This branch neither saves the form nor opens the report.
            is Validation.Invalid -> {
                mutableState.update(MainUiState(form, result.error))
                effects.emit(UiEffect.Message(result.error))
            }
            // 中文 · 按顺序写入偏好、清除错误、通知页面携带已校验资料打开报告。
            // EN · Save the profile, clear the error, then ask the screen to open the report with the validated data.
            is Validation.Valid -> {
                repository.save(form)
                mutableState.update(MainUiState(form))
                effects.emit(UiEffect.OpenReport(result.profile))
            }
        }
    }
    fun menu(action: MenuAction) = effects.emit(action.effect())
}
