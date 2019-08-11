package org.persianbms.andromeda

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Uri.startViewIntent(ctx: Context) {
    val intent = Intent(Intent.ACTION_VIEW, this)
    ctx.startActivity(intent)
}