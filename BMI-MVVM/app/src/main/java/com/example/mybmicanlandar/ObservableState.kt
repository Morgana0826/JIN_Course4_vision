package com.example.mybmicanlandar

/** Small synchronous observable for this form; observers detach with the Activity lifecycle. */
interface ObservableState<T> {
    val value: T
    fun observe(observer: (T) -> Unit): () -> Unit
}
class StateCell<T>(initial: T) : ObservableState<T> {
    private val observers = linkedSetOf<(T) -> Unit>()
    override var value: T = initial
        private set
    // 中文 · 先保存新状态，再通知当时的订阅者。复制观察者列表允许回调期间解除订阅。
    // EN · Store the value before notifying a snapshot of observers. The snapshot allows callbacks to unsubscribe during delivery.
    fun update(value: T) {
        this.value = value
        observers.toList().forEach { it(value) }
    }
    override fun observe(observer: (T) -> Unit): () -> Unit {
        observers.add(observer)
        // 中文 · 新页面订阅时立即收到最新状态，因此旋转屏幕后可以马上绘制表单或报告。
        // EN · A new subscriber receives the current value immediately, so a recreated screen can render the form or report.
        observer(value)
        return { observers.remove(observer); Unit }
    }
}
/** Effects are delivered once and are not replayed when a screen rotates. */
class EffectChannel<T> {
    private val observers = linkedSetOf<(T) -> Unit>()
    fun observe(observer: (T) -> Unit): () -> Unit {
        observers.add(observer)
        return { observers.remove(observer); Unit }
    }
    // 中文 · 只通知当前订阅者，没有历史队列。旋转后重新订阅不会再次弹窗或打开旧报告。
    // EN · Only current subscribers receive the effect. No history is queued, so re-subscribing after rotation cannot repeat an old dialog or navigation.
    fun emit(effect: T) { observers.toList().forEach { it(effect) } }
}
