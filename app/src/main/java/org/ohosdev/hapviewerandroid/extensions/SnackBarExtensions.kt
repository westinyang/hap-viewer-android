package org.ohosdev.hapviewerandroid.extensions

import android.annotation.SuppressLint
import com.google.android.material.R
import com.google.android.material.motion.MotionUtils.resolveThemeDuration
import com.google.android.material.snackbar.BaseTransientBottomBar

private const val DEFAULT_SLIDE_ANIMATION_DURATION = 250
private const val DEFAULT_ANIMATION_FADE_IN_DURATION = 150
private const val DEFAULT_ANIMATION_FADE_OUT_DURATION = 75


/**
 * 谷歌更新 MD3 之后，修改了 SnackBar 的过渡时间，导致 MD2 的动画时间较长。
 *
 * 这里重写了动画时间，使得动画效果与先前一致。
 *
 * 注意：该方法不会因为修改失败而抛出异常。
 *
 * */
@SuppressLint("PrivateResource")
fun <T : BaseTransientBottomBar<T>> T.overrideAnimationDurationIfNeeded() = apply {
    val isMaterial3Theme: Boolean
    context.theme.obtainStyledAttributes(intArrayOf(R.attr.isMaterial3Theme)).apply {
        isMaterial3Theme = getBoolean(0, false)
    }.recycle()
    if (isMaterial3Theme) return@apply
    runCatching<BaseTransientBottomBar<T>, Unit> {
        setDeclaredField(
            "animationSlideDuration",
            resolveThemeDuration(
                context, R.attr.motionDurationMedium2, DEFAULT_SLIDE_ANIMATION_DURATION
            )
        )
        setDeclaredField(
            "animationFadeInDuration",
            resolveThemeDuration(
                context, R.attr.motionDurationShort2, DEFAULT_ANIMATION_FADE_IN_DURATION
            )
        )
        setDeclaredField(
            "animationFadeOutDuration",
            resolveThemeDuration(
                context, R.attr.motionDurationShort1, DEFAULT_ANIMATION_FADE_OUT_DURATION
            )
        )
    }.onFailure { it.printStackTrace() }
}