package org.ohosdev.hapviewerandroid.extensions

inline fun <reified T> T.setDeclaredField(name: String, value: Any?) {
    T::class.java.getDeclaredField(name).apply {
        isAccessible = true
        set(this@setDeclaredField, value)
    }
}