package org.ohosdev.hapviewerandroid.util.event

abstract class BaseEvent {
    private var consumed = false
    fun consume(): Boolean {
        if (consumed)
            return false
        consumed = true
        return true
    }
}