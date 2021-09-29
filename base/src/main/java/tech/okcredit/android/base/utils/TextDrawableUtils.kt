package tech.okcredit.android.base.utils

import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator

object TextDrawableUtils {

    fun getRoundTextDrawable(text: String): TextDrawable =
        TextDrawable.builder().buildRound(text.first().toString().toUpperCase(), ColorGenerator.MATERIAL.getColor(text))
}
