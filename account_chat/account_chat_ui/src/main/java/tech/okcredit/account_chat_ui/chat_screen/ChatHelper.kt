package tech.okcredit.account_chat_ui.chat_screen

import android.util.DisplayMetrics
import androidx.fragment.app.FragmentActivity
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip
import tech.okcredit.account_chat_ui.R
import tech.okcredit.account_chat_ui.message_layout.SendMessageLayout
import tech.okcredit.android.base.utils.DimensionUtil

object ChatHelper {
    fun makeToolTip(
        requireActivity: FragmentActivity,
        sendMessageLayout: SendMessageLayout,
        accountName: String?,
    ): Tooltip {
        val displayMetrics = DisplayMetrics()
        requireActivity.windowManager.defaultDisplay
            .getMetrics(displayMetrics)
        val width: Int = displayMetrics.widthPixels
        return Tooltip.Builder(requireActivity)
            .anchor(sendMessageLayout, -width / 10, 0, false)
            .text(requireActivity.getString(R.string.now_you_can_chat_with_your_contacts_on_okcredit, accountName))
            .arrow(true)
            .maxWidth(DimensionUtil.dp2px(requireActivity, 258f).toInt())
            .overlay(false)
            .styleId(R.style.ToolTipAltStyle)
            .closePolicy(ClosePolicy.Builder().outside(true).build())
            .create()
    }
}
