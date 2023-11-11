package org.ohosdev.hapviewerandroid.extensions

import org.ohosdev.hapviewerandroid.util.ShizukuUtil

val ShizukuUtil.ShizukuStatus.isGranted get() = this == ShizukuUtil.ShizukuStatus.GRANTED