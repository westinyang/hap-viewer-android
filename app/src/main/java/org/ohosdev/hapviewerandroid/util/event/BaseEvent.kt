package org.ohosdev.hapviewerandroid.util.event

abstract class BaseEvent {
    private var consumed = false

    /**
     * 消费此事件。
     * @param onConsume 仅当第一次调用该方法时立即执行此回调
     * */
    fun consume(onConsume: (() -> Unit)? = null): Boolean {
        if (consumed)
            return false
        consumed = true
        onConsume?.invoke()
        return true
    }
}