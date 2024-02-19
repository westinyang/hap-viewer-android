package org.ohosdev.hapviewerandroid.util.ohos

const val PREFIX_OHOS_PERMISSION = "ohos.permission."
fun String.getOhosPermSortName() =
    if (this.startsWith(PREFIX_OHOS_PERMISSION)) this.substring(PREFIX_OHOS_PERMISSION.length) else null