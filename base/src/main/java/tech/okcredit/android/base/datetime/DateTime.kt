@file:JvmName("DateTimeUtils")
@file:JvmMultifileClass

package tech.okcredit.android.base.datetime

import org.joda.time.DateTime

fun toEpoch(dateTime: DateTime?): Long = if (dateTime == null) 0 else (dateTime.millis / 1000L)

fun fromEpoch(epoch: Long?): DateTime? = if (epoch == null || epoch <= 0) null else DateTime(epoch * 1000L)

val DateTime?.epoch: Long
    get() = toEpoch(this)
