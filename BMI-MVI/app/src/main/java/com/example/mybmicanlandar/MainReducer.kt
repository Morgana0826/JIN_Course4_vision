package com.example.mybmicanlandar

data class MainState(val form: BmiForm, val error: InputError? = null)
// 中文 · 既描述用户操作，也描述校验完成后的结果；这里的 Intent 是应用内的 Kotlin 类型。
// EN · These Kotlin intents represent user actions and completed validation results.
sealed class MainIntent {
    data class FormChanged(val form: BmiForm) : MainIntent()
    object Calculate : MainIntent()
    data class Validated(val form: BmiForm, val result: Validation) : MainIntent()
    data class MenuSelected(val action: MenuAction) : MainIntent()
}
// 中文 · Reducer 只描述要执行的工作。Store 收到 Validate 或 Save 后才调用模型或仓库。
// EN · The reducer describes the work. The store executes Validate and Save against the model or repository.
sealed class MainCommand {
    data class Validate(val form: BmiForm) : MainCommand()
    data class Save(val form: BmiForm) : MainCommand()
}
// 中文 · 一次转换可以同时产生新状态、待执行命令，以及由页面消费的事件。
// EN · A single transition carries the next state, commands to execute and effects for the view.
data class MainTransition(val state: MainState, val commands: List<MainCommand> = emptyList(),
                          val effects: List<UiEffect> = emptyList())

/** Pure reducer: no Android calls, persistence or UI callbacks. */
object MainReducer {
    fun reduce(state: MainState, intent: MainIntent): MainTransition = when (intent) {
        is MainIntent.FormChanged -> MainTransition(MainState(intent.form))
        // 中文 · 点击先产生 Validate 命令。收到 Validated 意图后，才知道该写入资料还是显示错误。
        // EN · A click first produces a Validate command. The later Validated intent determines whether to save or show an error.
        MainIntent.Calculate -> MainTransition(state.copy(error = null), listOf(MainCommand.Validate(state.form)))
        is MainIntent.MenuSelected -> MainTransition(state, effects = listOf(intent.action.effect()))
        is MainIntent.Validated -> when (val result = intent.result) {
            is Validation.Invalid -> MainTransition(state.copy(error = result.error),
                effects = listOf(UiEffect.Message(result.error)))
            // 中文 · 同一次转换同时要求保存和导航；先执行命令还是先显示报告，由 MainStore 的循环顺序保证。
            // EN · This transition requests both persistence and navigation. The MainStore loop executes the save before delivering the effect.
            is Validation.Valid -> MainTransition(state.copy(error = null), listOf(MainCommand.Save(intent.form)),
                listOf(UiEffect.OpenReport(result.profile)))
        }
    }
}
