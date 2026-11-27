package com.example.mybmicanlandar

/** Intent -> reducer -> state -> render; commands feed their results back as intents. */
class MainStore(private val repository: ProfileRepository, restored: BmiForm? = null,
                private val model: BmiModel = BmiModel()) {
    private val mutableState = StateCell(MainState(restored ?: repository.load()))
    val state: ObservableState<MainState> = mutableState
    val effects = EffectChannel<UiEffect>()
    // 中文 · 队列串行处理意图。处理过程中产生的新意图排在队尾，避免嵌套 dispatch 打断当前转换。
    // EN · Queue intents in order. An intent produced during delivery goes to the tail without interrupting the current transition.
    private val pending = java.util.ArrayDeque<MainIntent>()
    private var dispatching = false
    fun dispatch(intent: MainIntent) {
        pending.add(intent)
        if (dispatching) return
        dispatching = true
        try {
            while (pending.isNotEmpty()) {
                // 中文 · 把当前状态和队首意图交给纯 Reducer，然后先发布新状态，最后才发界面事件。
                // EN · Reduce the current state and head intent first. Publish the next state before commands and then UI effects.
                val transition = MainReducer.reduce(state.value, pending.removeFirst())
                mutableState.update(transition.state)
                transition.commands.forEach { command -> when (command) {
                    // 中文 · 把校验结果连同当时的表单放回队列；后续保存使用的就是这份已校验的输入。
                    // EN · Enqueue the validation result with its form snapshot so a later save uses the same validated input.
                    is MainCommand.Validate -> pending.add(MainIntent.Validated(command.form, model.validate(command.form)))
                    is MainCommand.Save -> repository.save(command.form)
                } }
                transition.effects.forEach(effects::emit)
            }
        } finally { dispatching = false }
    }
}
