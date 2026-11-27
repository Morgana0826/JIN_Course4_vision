package com.example.mybmicanlandar

data class ReportState(val report: BmiReport? = null)
sealed class ReportIntent {
    data class Load(val profile: BmiProfile?) : ReportIntent()
    data class Loaded(val report: BmiReport?) : ReportIntent()
    object Back : ReportIntent()
}
data class ReportCommand(val profile: BmiProfile?)
data class ReportTransition(val state: ReportState, val command: ReportCommand? = null,
                            val effects: List<UiEffect> = emptyList())
// 中文 · Load 产生计算命令，Loaded 携带模型返回的结果。空结果和用户返回都可产生 Close 事件。
// EN · Load requests computation and Loaded carries the model result. Both a missing result and Back can produce a Close effect.
object ReportReducer {
    fun reduce(state: ReportState, intent: ReportIntent): ReportTransition = when (intent) {
        is ReportIntent.Load -> ReportTransition(state, ReportCommand(intent.profile))
        is ReportIntent.Loaded -> ReportTransition(ReportState(intent.report),
            effects = if (intent.report == null) listOf(UiEffect.Close) else emptyList())
        ReportIntent.Back -> ReportTransition(state, effects = listOf(UiEffect.Close))
    }
}
class ReportStore(private val model: BmiModel = BmiModel()) {
    private val mutableState = StateCell(ReportState())
    val state: ObservableState<ReportState> = mutableState
    val effects = EffectChannel<UiEffect>()
    // 中文 · 发布新状态后执行计算，再以 Loaded 交回 Reducer；只有 Load 会生成这条计算命令。
    // EN · Publish the next state, execute the calculation and feed Loaded back to the reducer. Only Load produces a calculation command.
    fun dispatch(intent: ReportIntent) {
        val transition = ReportReducer.reduce(state.value, intent)
        mutableState.update(transition.state)
        transition.command?.let { dispatch(ReportIntent.Loaded(model.report(it.profile))) }
        transition.effects.forEach(effects::emit)
    }
}
