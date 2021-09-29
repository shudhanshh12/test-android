package tech.okcredit.account_chat_ui.message_list_layout

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import tech.okcredit.account_chat_ui.R

class MessageViewHolder(v: View?) : RecyclerView.ViewHolder(v!!) {
    var materialContainer: MaterialCardView
    var unreadMessageBand: TextView
    var dateContainer: MaterialCardView
    var date: TextView
    var sync: ImageView
    var tx_date: TextView
    val ll: LinearLayout
    var messageTextView: TextView

    init {
        messageTextView =
            itemView.findViewById(R.id.messageTextView) as TextView
        tx_date =
            itemView.findViewById(R.id.tx_date) as TextView
        ll = itemView.findViewById(R.id.ll) as LinearLayout
        sync = itemView.findViewById(R.id.sync) as ImageView
        date = itemView.findViewById(R.id.date) as TextView
        materialContainer = itemView.findViewById(R.id.material_container) as MaterialCardView
        dateContainer = itemView.findViewById(R.id.chat_date_container) as MaterialCardView
        unreadMessageBand = itemView.findViewById(R.id.unread_message_band) as TextView
    }
}
