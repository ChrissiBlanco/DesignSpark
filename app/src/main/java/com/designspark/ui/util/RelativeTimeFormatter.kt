package com.designspark.ui.util

import android.content.Context
import android.text.format.DateUtils

fun formatRelativeCreationTime(context: Context, createdAtMillis: Long): String =
    DateUtils.getRelativeTimeSpanString(
        createdAtMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()
