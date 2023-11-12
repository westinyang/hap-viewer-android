package org.ohosdev.hapviewerandroid.util.event

abstract class BaseEvent {
    private var consumed = false
    fun consume(onConsume: (() -> Unit)? = null): Boolean {
        if (consumed)
            return false
        consumed = true
        onConsume?.invoke()
        return true
    }
}