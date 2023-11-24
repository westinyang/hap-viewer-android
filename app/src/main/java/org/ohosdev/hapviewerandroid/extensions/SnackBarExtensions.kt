package org.ohosdev.hapviewerandroid.extensions

import android.annotation.SuppressLint
import com.google.android.material.R
import com.google.android.material.motion.MotionUtils
import com.google.android.material.snackbar.BaseTransientBottomBar

private const val DEFAULT_SLIDE_ANIMATION_DURATION = 250
private const val DEFAULT_ANIMATION_FADE_IN_DURATION = 150
private const val DEFAULT_ANIMATION_FADE_OUT_DURATION = 75

@Suppress("UNCHECKED_CAST")
@SuppressLint("PrivateResource")
fun <T : BaseTransientBottomBar<T>> BaseTransientBottomBar<T>.overrideAnimationDurationIfNotMd3() =
    apply {
        context.theme.obtainStyledAttributes(intArrayOf(R.attr.isMaterial3Theme)).also {
            if (it.getBoolean(0, false)) return@apply
        }.recycle()
        BaseTransientBottomBar::class.java.apply {
            getDeclaredField("animationSlideDuration").apply {
                isAccessible = true
                set(
                    this@overrideAnimationDurationIfNotMd3, MotionUtils.resolveThemeDuration(
                        context, R.attr.motionDurationMedium2, DEFAULT_SLIDE_ANIMATION_DURATION
                    )
                )
            }
            getDeclaredField("animationFadeInDuration").apply {
                isAccessible = true
                set(
                    this@overrideAnimationDurationIfNotMd3, MotionUtils.resolveThemeDuration(
                        context, R.attr.motionDurationShort2, DEFAULT_ANIMATION_FADE_IN_DURATION
                    )
                )
            }
            getDeclaredField("animationFadeOutDuration").apply {
                isAccessible = true
                set(
                    this@overrideAnimationDurationIfNotMd3, MotionUtils.resolveThemeDuration(
                        context, R.attr.motionDurationShort1, DEFAULT_ANIMATION_FADE_OUT_DURATION
                    )
                )
            }
        }
    } as T