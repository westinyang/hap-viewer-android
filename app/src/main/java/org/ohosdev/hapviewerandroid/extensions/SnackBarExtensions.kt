package org.ohosdev.hapviewerandroid.extensions

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.material.R
import com.google.android.material.motion.MotionUtils.resolveThemeDuration
import com.google.android.material.snackbar.BaseTransientBottomBar

private const val DEFAULT_SLIDE_ANIMATION_DURATION = 250
private const val DEFAULT_ANIMATION_FADE_IN_DURATION = 150
private const val DEFAULT_ANIMATION_FADE_OUT_DURATION = 75

private inline fun <reified T> Any.setDeclaredField(name: String, value: Any) {
    T::class.java.getDeclaredField(name).apply {
        isAccessible = true
        set(this@setDeclaredField, value)
    }
}

/**
 * 谷歌更新 MD3 之后，修改了 SnackBar 的过渡时间，导致 MD2 的动画时间较长。
 *
 * 这里重写了动画时间，使得动画效果与先前一致。
 *
 * 注意：该方法不会因为修改失败而抛出异常。
 *
 * */
@Suppress("UNCHECKED_CAST")
@SuppressLint("PrivateResource")
fun <T : BaseTransientBottomBar<T>> BaseTransientBottomBar<T>.overrideAnimationDurationIfNeeded() =
    apply {
        val isMaterial3Theme: Boolean
        context.theme.obtainStyledAttributes(intArrayOf(R.attr.isMaterial3Theme)).also {
            isMaterial3Theme = it.getBoolean(0, false)
        }.recycle()
        Log.d("TAG", "overrideAnimationDurationIfNeeded: $isMaterial3Theme")
        if (isMaterial3Theme) return@apply
        runCatching {
            setDeclaredField<BaseTransientBottomBar<T>>(
                "animationSlideDuration",
                resolveThemeDuration(
                    context, R.attr.motionDurationMedium2, DEFAULT_SLIDE_ANIMATION_DURATION
                )
            )
            setDeclaredField<BaseTransientBottomBar<T>>(
                "animationFadeInDuration",
                resolveThemeDuration(
                    context, R.attr.motionDurationShort2, DEFAULT_ANIMATION_FADE_IN_DURATION
                )
            )
            setDeclaredField<BaseTransientBottomBar<T>>(
                "animationFadeOutDuration",
                resolveThemeDuration(
                    context, R.attr.motionDurationShort1, DEFAULT_ANIMATION_FADE_OUT_DURATION
                )
            )
        }.onFailure { it.printStackTrace() }
    } as T